package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Order;
import com.acamargo.arbitrage.dto.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Qualifier("okxService")
public class OkxServiceImpl implements OkxService, SymbolProvider {
    public final String BASE_URI = "https://www.okx.com/api/v5/market";

    @Override
    public List<Symbol> getSymbols() {

        RestClient defaultClient = RestClient.create();
        var symbolList = new ArrayList<Symbol>();

        try {
            String json = defaultClient
                    .get()
                    .uri(new URI(BASE_URI + "/tickers?instType=SPOT"))
                    .retrieve()
                    .body(String.class);

            var mapper = new ObjectMapper();
            var node = mapper.readTree(json);

            node = node.get("data");

            Iterator<JsonNode> i = node.elements();

            while (i.hasNext()) {
                JsonNode data = i.next();
                Symbol symbol = new Symbol(data.get("instId").textValue());
                symbolList.add(symbol);
            }

        } catch (URISyntaxException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return symbolList;


    }

    @Override
    public Book getOrderBook(String symbol, int count) {
        RestClient defaultClient = RestClient.create();

        List<Order> askOrders = new ArrayList<>();
        List<Order> bidOrders = new ArrayList<>();


        try {

            String json = defaultClient
                    .get()
                    .uri(new URI(BASE_URI + String.format("/books?sz=%s&instId=%s", count, symbol)))
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();

            var node = mapper.readTree(json);

            node = node.get("data");
            node = node.get(0);

            var nodeBids = node.get("bids");
            var nodeAsks = node.get("asks");

            nodeBids.forEach(b -> bidOrders.add(new Order(b.get(0).asDouble(), b.get(1).asDouble())));
            nodeAsks.forEach(b -> askOrders.add(new Order(b.get(0).asDouble(), b.get(1).asDouble())));

        } catch (JsonProcessingException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new Book(askOrders, bidOrders);

    }
}
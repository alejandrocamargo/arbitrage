package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Order;
import com.acamargo.arbitrage.dto.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Qualifier("bitfinexService")
@Service
@Slf4j
public class BitfinexServiceImpl implements SymbolProvider, BitfinexService {

    public static final String BASE_URI = "https://api-pub.bitfinex.com/v2";

    @Override
    public List<Symbol> getSymbols() {

        RestClient defaultClient = RestClient.create();
        var symbolList = new ArrayList<Symbol>();

        try {
            String json = defaultClient
                    .get()
                    .uri(new URI(BASE_URI + "/tickers?symbols=ALL"))
                    .retrieve()
                    .body(String.class);

            var mapper = new ObjectMapper();
            var node = mapper.readTree(json);

            Iterator<JsonNode> i = node.elements();

            while (i.hasNext()) {

                JsonNode next = i.next();
                symbolList.add(new Symbol(next.get(0).textValue()));

            }

            return symbolList;

        } catch (URISyntaxException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Book getOrderBook(String symbol, int count) {
        count = 1; //forced to 1
        RestClient defaultClient = RestClient.create();

        List<Order> askOrders = new ArrayList<>();
        List<Order> bidOrders = new ArrayList<>();

        try {
            String json = defaultClient
                    .get()
                    .uri(new URI(BASE_URI + String.format("/book/%s/P0?len=%s", symbol, count)))
                    .retrieve()
                    .body(String.class);

            var mapper = new ObjectMapper();
            var node = mapper.readTree(json);


            JsonNode asksNode = node.get(1);
            JsonNode bidsNode = node.get(0);

            askOrders.add(new Order(asksNode.get(0).asDouble(), asksNode.get(2).asDouble()));
            bidOrders.add(new Order(bidsNode.get(0).asDouble(), Math.abs(bidsNode.get(2).asDouble())));

            return new Book(bidOrders, askOrders);


        } catch (JsonProcessingException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

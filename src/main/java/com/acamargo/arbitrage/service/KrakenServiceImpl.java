package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Order;
import com.acamargo.arbitrage.dto.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Qualifier("krakenService")
@Slf4j
public class KrakenServiceImpl implements KrakenService, SymbolProvider {

    public static final String BASE_URI = "https://api.kraken.com/0/public/";

    @Override
    public Book getOrderBook(String symbol, int count) {

        RestClient defaultClient = RestClient.create();

        List<Order> askOrders = new ArrayList<>();
        List<Order> bidOrders = new ArrayList<>();

        try {
             String json = defaultClient
                    .get()
                    .uri(new URI(BASE_URI + String.format("Depth?count=%s&pair=%s", count, symbol)))
                    .retrieve()
                    .body(String.class);

             ObjectMapper mapper = new ObjectMapper();

             var node = mapper.readTree(json);

             node = node.get("result");

             Iterator<JsonNode> i = node.elements();

            while (i.hasNext()) {

                JsonNode next = i.next();

                JsonNode asksNode = next.get("asks");
                JsonNode bidsNode = next.get("bids");

                asksNode.forEach(e -> askOrders.add(new Order(e.get(0).asDouble(), e.get(1).asDouble())));
                bidsNode.forEach(e -> bidOrders.add(new Order(e.get(0).asDouble(), e.get(1).asDouble())));

            }

            return new Book(bidOrders, askOrders);


        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Symbol> getSymbols() {

        RestClient defaultClient = RestClient.create();
        var symbolList = new ArrayList<Symbol>();

        try {
            String json = defaultClient
                    .get()
                    .uri(new URI("https://api.kraken.com/0/public/AssetPairs"))
                    .retrieve()
                    .body(String.class);

            var mapper = new ObjectMapper();
            var node = mapper.readTree(json);

            node = node.get("result");

            Iterator<JsonNode> i = node.elements();

            while (i.hasNext()) {

                JsonNode next = i.next();
                symbolList.add(new Symbol(next.get("altname").textValue()));

            }

            return symbolList;

        } catch (URISyntaxException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

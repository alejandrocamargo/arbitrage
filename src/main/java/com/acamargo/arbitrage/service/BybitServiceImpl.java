package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Order;
import com.acamargo.arbitrage.dto.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Qualifier("bybitService")
@Service
@Slf4j
public class BybitServiceImpl implements BybitService, SymbolProvider {
    public final static String BASE_URI = "https://api.bybit.com/v5/market/";

    @Override
    public List<Symbol> getSymbols() {

        List<Symbol> symbols = new ArrayList<>();

        RestClient defaultClient = RestClient.create();

        try {
            String json = defaultClient
                    .get()
                    .uri(new URI(BASE_URI + "tickers?category=spot"))
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();

            var node = mapper.readTree(json);

            node = node.get("result").get("list");

            var i = node.elements();

            while (i.hasNext()) {
                var symbol = i.next().get("symbol").asText();
                symbols.add(new Symbol(symbol));
            }

            return symbols;

        } catch (URISyntaxException e) {
            log.warn("Cannot get exchange info", e);
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Book getOrderBook(String symbol, int count) {

        RestClient defaultClient = RestClient.create();

        List<Order> askOrders = new ArrayList<>();
        List<Order> bidOrders = new ArrayList<>();

        try {
            String json = defaultClient
                    .get()
                    .uri(new URI(String.format(BASE_URI + "orderbook?category=spot&limit=%s&symbol=%s", count, symbol)))
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();

            var node = mapper.readTree(json);

            var bidsNode = node.get("result").get("b");
            var asksNode = node.get("result").get("a");

            asksNode.forEach(e -> askOrders.add(new Order(e.get(0).asDouble(), e.get(1).asDouble())));
            bidsNode.forEach(e -> bidOrders.add(new Order(e.get(0).asDouble(), e.get(1).asDouble())));


            return new Book(bidOrders, askOrders);

        } catch (URISyntaxException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}

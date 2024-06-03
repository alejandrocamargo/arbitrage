package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Order;
import com.acamargo.arbitrage.dto.Symbol;
import com.acamargo.arbitrage.dto.binance.BinanceExchangeInfo;
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
@Qualifier("binanceService")
@Slf4j
public class BinanceServiceImpl implements BinanceService, SymbolProvider {

    public static final String BASE_URI = "https://api.binance.com/api/v3/";

    @Async("asyncTaskExecutor")
    @Override
    public CompletableFuture<List<Symbol>> getSymbols() {
        CompletableFuture<List<Symbol>> completableFuture = new CompletableFuture<>();

        RestClient defaultClient = RestClient.create();

        try {
            BinanceExchangeInfo info = defaultClient
                    .get()
                    .uri(new URI(BASE_URI + "exchangeInfo"))
                    .retrieve()
                    .body(BinanceExchangeInfo.class);

            completableFuture.complete(info.symbols());

        } catch (URISyntaxException e) {
            log.warn("Cannot get exchange info", e);
            throw new RuntimeException(e);
        }

        return completableFuture;
    }


    @Async("asyncTaskExecutor")
    @Override
    public CompletableFuture<Book> getOrderBook(String symbol, int count) {
        CompletableFuture<Book> completableFuture = new CompletableFuture<>();

        RestClient defaultClient = RestClient.create();

        List<Order> askOrders = new ArrayList<>();
        List<Order> bidOrders = new ArrayList<>();

        try {
            String json = defaultClient
                    .get()
                    .uri(new URI(String.format(BASE_URI + "depth?limit=%s&symbol=%s", count, symbol)))
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();

            var node = mapper.readTree(json);
            var bidsNode = node.get("bids");
            var asksNode = node.get("asks");

            asksNode.forEach(e -> askOrders.add(new Order(e.get(0).asDouble(), e.get(1).asDouble())));
            bidsNode.forEach(e -> bidOrders.add(new Order(e.get(0).asDouble(), e.get(1).asDouble())));


            completableFuture.complete(new Book(bidOrders, askOrders));

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return completableFuture;
    }

}

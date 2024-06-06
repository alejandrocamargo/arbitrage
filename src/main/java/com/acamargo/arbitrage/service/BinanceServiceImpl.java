package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Order;
import com.acamargo.arbitrage.dto.Symbol;
import com.acamargo.arbitrage.dto.binance.AssetPrice;
import com.acamargo.arbitrage.dto.binance.BinanceExchangeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
@Qualifier("binanceService")
@Slf4j
public class BinanceServiceImpl implements BinanceService, SymbolProvider {

    public static final String BASE_URI = "https://api.binance.com/api/v3/";

    @Override
    public List<Symbol> getSymbols() {

        RestClient defaultClient = RestClient.create();

        try {
            return defaultClient
                    .get()
                    .uri(new URI(BASE_URI + "exchangeInfo"))
                    .retrieve()
                    .body(BinanceExchangeInfo.class).symbols();

        } catch (URISyntaxException e) {
            log.warn("Cannot get exchange info", e);
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
                    .uri(new URI(String.format(BASE_URI + "depth?limit=%s&symbol=%s", count, symbol)))
                    .retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();

            var node = mapper.readTree(json);
            var bidsNode = node.get("bids");
            var asksNode = node.get("asks");

            asksNode.forEach(e -> askOrders.add(new Order(e.get(0).asDouble(), e.get(1).asDouble())));
            bidsNode.forEach(e -> bidOrders.add(new Order(e.get(0).asDouble(), e.get(1).asDouble())));


            return new Book(bidOrders, askOrders);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Cacheable("prices")
    @Override
    public AssetPrice getAssetPrice(String symbol) {
        //https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT

        log.info("Getting price from Binance for pair: {}", symbol);

        RestClient defaultClient = RestClient.create();

        try {
            return defaultClient
                    .get()
                    .uri(new URI(String.format(BASE_URI + "ticker/price?symbol=%s", symbol)))
                    .retrieve()
                    .body(AssetPrice.class);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
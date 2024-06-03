package com.acamargo.arbitrage.controller;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Symbol;
import com.acamargo.arbitrage.service.SymbolProvider;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class OrderBookController {

    private enum Exchange {
        KRAKEN, BINANCE;
    }

    private SymbolProvider binanceService;
    private SymbolProvider krakenService;

    public OrderBookController(@Qualifier("binanceService") SymbolProvider binanceService,
                               @Qualifier("krakenService") SymbolProvider krakenService) {
        this.binanceService = binanceService;
        this.krakenService = krakenService;
    }
    @GetMapping("symbols")
    public List<Symbol> getSymbols() throws ExecutionException, InterruptedException {

        List<Symbol> krakenSymbols = krakenService.getSymbols().get();
        List<Symbol> binanceSymbols = binanceService.getSymbols().get();

        return krakenSymbols
                .parallelStream()
                .filter(binanceSymbols::contains)
                .collect(Collectors.toList());

    }

    @GetMapping("orderbook")
    public Book getOrderBook(@PathParam("symbol") String symbol, @PathParam("count") int count,
                             @PathParam("exchange") Exchange exchange)
            throws ExecutionException, InterruptedException {


        switch (exchange) {
            case KRAKEN:
                return krakenService.getOrderBook(symbol, count).get();
            case BINANCE:
                return binanceService.getOrderBook(symbol, count).get();
        }

        return null;

    }



}

package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.ExchangeEnum;
import com.acamargo.arbitrage.dto.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ArbitrageServiceImpl implements ArbitrageService {

    private final ExchangeFacade exchangeFacade;

    public ArbitrageServiceImpl(ExchangeFacade exchangeFacade) {
        this.exchangeFacade = exchangeFacade;
    }

    @Async
    @Override
    public CompletableFuture<List<Arbitrage>> findArbitrage(ExchangeEnum a, ExchangeEnum b) throws ExecutionException, InterruptedException {

        var arbitrageList = new ArrayList<Arbitrage>();

        log.info("Find arbitrage for {} and {}", a, b);

        List<Symbol> aSymbols = exchangeFacade.getSymbols(a);
        List<Symbol> bSymbols = exchangeFacade.getSymbols(b);

        List<Symbol> commonSymbols =  aSymbols
                .parallelStream()
                .filter(bSymbols::contains)
                .toList();

        log.info("Symbols retrieved for {} and {}", a, b);

        commonSymbols
                .parallelStream()
                .forEach(symbol -> {

                    try {
                        var aOrders = exchangeFacade.getOrderBook(a, 1, symbol);
                        var bOrders = exchangeFacade.getOrderBook(b, 1, symbol);

                        log.info("Retrieved orderbooks for {}", symbol);

                        if (!aOrders.bids().isEmpty()
                                && !aOrders.asks().isEmpty()
                                && !bOrders.asks().isEmpty()
                                && !bOrders.bids().isEmpty()) {

                            if (aOrders.asks().getFirst().price() < bOrders.bids().getFirst().price()) {

                                arbitrageList.add(
                                        Arbitrage.builder()
                                                .symbol(symbol)
                                                .buyExchange(a)
                                                .buyPrice(aOrders.asks().getFirst().price())
                                                .buyQuantity(aOrders.asks().getFirst().quantity())
                                                .sellExchange(b)
                                                .sellPrice(bOrders.bids().getFirst().price())
                                                .sellQuantity(bOrders.bids().getFirst().quantity())
                                                .build()
                                );


                            }

                        }

                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                });

        log.info("Finish find arbitrage for {} and {}", a, b);

        return CompletableFuture.completedFuture(arbitrageList);
    }



}

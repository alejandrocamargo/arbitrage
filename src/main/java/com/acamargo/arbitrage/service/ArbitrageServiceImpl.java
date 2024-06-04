package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.ExchangeEnum;
import com.acamargo.arbitrage.dto.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Slf4j
public class ArbitrageServiceImpl implements ArbitrageService {

    private final SymbolProvider binanceService;
    private final SymbolProvider krakenService;
    private final SymbolProvider bybitService;

    public ArbitrageServiceImpl(@Qualifier("binanceService") SymbolProvider binanceService,
                                @Qualifier("krakenService") SymbolProvider krakenService,
                                @Qualifier("bybitService") SymbolProvider bybitService) {
        this.binanceService = binanceService;
        this.krakenService = krakenService;
        this.bybitService = bybitService;
    }

    @Override
    public List<Arbitrage> findArbitrage(ExchangeEnum a, ExchangeEnum b) throws ExecutionException, InterruptedException {
        var arbitrageList = new ArrayList<Arbitrage>();

        List<Symbol> aSymbols = getSymbols(a);
        List<Symbol> bSymbols = getSymbols(b);

        List<Symbol> commonSymbols =  aSymbols
                .parallelStream()
                .filter(bSymbols::contains)
                .toList();

        commonSymbols
                .parallelStream()
                .forEach(symbol -> {

                    try {
                        var aOrders = getOrderBook(a, 1, symbol);
                        var bOrders = getOrderBook(b, 1, symbol);

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


        return arbitrageList;
    }

    private List<Symbol> getSymbols(ExchangeEnum exchange) throws ExecutionException, InterruptedException {

        return switch (exchange) {
            case BYBIT -> bybitService.getSymbols().get();
            case KRAKEN -> krakenService.getSymbols().get();
            case BINANCE -> binanceService.getSymbols().get();
        };
    }

    private Book getOrderBook(ExchangeEnum exchange, int count, Symbol symbol) throws ExecutionException, InterruptedException {

        return switch (exchange) {
            case BYBIT -> bybitService.getOrderBook(symbol.symbol(), count).get();
            case KRAKEN -> krakenService.getOrderBook(symbol.symbol(), count).get();
            case BINANCE -> binanceService.getOrderBook(symbol.symbol(), count).get();
        };
    }

    public List<Symbol> getSymbols() throws ExecutionException, InterruptedException {
        List<Symbol> krakenSymbols = krakenService.getSymbols().get();
        List<Symbol> binanceSymbols = binanceService.getSymbols().get();

        return krakenSymbols
                .parallelStream()
                .filter(binanceSymbols::contains)
                .collect(Collectors.toList());

    }

//    @Override
//    public void findArbitrage() throws ExecutionException, InterruptedException {
//
//        this.getSymbols()
//                .parallelStream()
//                .forEach(symbol -> {
//
//                    try {
//                        var binanceOrders = binanceService.getOrderBook(symbol.symbol(), 1).get();
//                        var krakenOrders = krakenService.getOrderBook(symbol.symbol(), 1).get();
//
//                        if (binanceOrders.bids().size() > 0
//                                && krakenOrders.bids().size() > 0
//                                && krakenOrders.asks().size() > 0
//                                && krakenOrders.asks().size() > 0) {
//
//                            if (binanceOrders.asks().get(0).price() < krakenOrders.bids().get(0).price()) {
//
//
//                                log.info("Arbitrage BUY {} ( Binance ) ask={} q={} SELL ( Kraken ) bid={} q={} profit={}%", symbol.symbol(),
//                                        binanceOrders.asks().get(0).price(), binanceOrders.asks().get(0).quantity(),
//                                        krakenOrders.bids().get(0).price(), krakenOrders.bids().get(0).quantity(),
//                                        format("%.02f", (krakenOrders.bids().get(0).price()/binanceOrders.asks().get(0).price()*100)-100));
//                            }
//
//                        }
//
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    } catch (ExecutionException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                });
//
//
//    }
}

package com.acamargo.arbitrage.controller;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.ExchangeEnum;
import com.acamargo.arbitrage.dto.Symbol;
import com.acamargo.arbitrage.service.ArbitrageService;
import com.acamargo.arbitrage.service.SymbolProvider;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class OrderBookController {

    private final SymbolProvider binanceService;
    private final SymbolProvider krakenService;
    private final SymbolProvider bybitService;
    private final ArbitrageService arbitrageService;

    public OrderBookController(@Qualifier("binanceService") SymbolProvider binanceService,
                               @Qualifier("krakenService") SymbolProvider krakenService,
                               @Qualifier("bybitService") SymbolProvider bybitService,
                               ArbitrageService arbitrageService) {
        this.binanceService = binanceService;
        this.krakenService = krakenService;
        this.arbitrageService = arbitrageService;
        this.bybitService = bybitService;
    }
    @GetMapping("symbols")
    public List<Symbol> getSymbols() throws ExecutionException, InterruptedException {

        return bybitService.getSymbols().get();

    }

    @GetMapping("orderbook")
    public Book getOrderBook(@PathParam("symbol") String symbol, @PathParam("count") int count,
                             @PathParam("exchange") ExchangeEnum exchange)
            throws ExecutionException, InterruptedException {

        return switch (exchange) {
            case KRAKEN -> krakenService.getOrderBook(symbol, count).get();
            case BINANCE -> binanceService.getOrderBook(symbol, count).get();
            case BYBIT -> bybitService.getOrderBook(symbol, count).get();
        };

    }

    @GetMapping("arbitrage")
    public List<Arbitrage> doArbitrage() throws ExecutionException, InterruptedException {
        List<Arbitrage> ls = arbitrageService.findArbitrage(ExchangeEnum.KRAKEN, ExchangeEnum.BYBIT);
        ls.addAll(arbitrageService.findArbitrage(ExchangeEnum.BYBIT, ExchangeEnum.KRAKEN));
        ls.addAll(arbitrageService.findArbitrage(ExchangeEnum.BYBIT, ExchangeEnum.BINANCE));
        ls.addAll(arbitrageService.findArbitrage(ExchangeEnum.BINANCE, ExchangeEnum.BYBIT));
        ls.addAll(arbitrageService.findArbitrage(ExchangeEnum.BINANCE, ExchangeEnum.KRAKEN));
        ls.addAll(arbitrageService.findArbitrage(ExchangeEnum.KRAKEN, ExchangeEnum.BINANCE));

        ls.sort(new Arbitrage.ArbitrageProfitabilityComparator());

        return ls;

    }



}

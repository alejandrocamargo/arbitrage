package com.acamargo.arbitrage.controller;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.ExchangeEnum;
import com.acamargo.arbitrage.dto.Symbol;
import com.acamargo.arbitrage.service.ArbitrageService;
import com.acamargo.arbitrage.service.ProfitCalculatorService;
import com.acamargo.arbitrage.service.SymbolProvider;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class OrderBookController {

    private final SymbolProvider binanceService;
    private final SymbolProvider krakenService;
    private final SymbolProvider bybitService;
    private final ArbitrageService arbitrageService;
    private final ProfitCalculatorService profitCalculatorService;

    public OrderBookController(@Qualifier("binanceService") SymbolProvider binanceService,
                               @Qualifier("krakenService") SymbolProvider krakenService,
                               @Qualifier("bybitService") SymbolProvider bybitService,
                               ArbitrageService arbitrageService,
                               ProfitCalculatorService profitCalculatorService) {
        this.binanceService = binanceService;
        this.krakenService = krakenService;
        this.arbitrageService = arbitrageService;
        this.bybitService = bybitService;
        this.profitCalculatorService = profitCalculatorService;
    }
    @GetMapping("symbols")
    public List<Symbol> getSymbols() {

        return bybitService.getSymbols();

    }

    @GetMapping("orderbook")
    public Book getOrderBook(@PathParam("symbol") String symbol, @PathParam("count") int count,
                             @PathParam("exchange") ExchangeEnum exchange) {

        return switch (exchange) {
            case KRAKEN -> krakenService.getOrderBook(symbol, count);
            case BINANCE -> binanceService.getOrderBook(symbol, count);
            case BYBIT -> bybitService.getOrderBook(symbol, count);
        };

    }

    @GetMapping("arbitrage")
    public List<Arbitrage> doArbitrage() throws ExecutionException, InterruptedException {
        var f1 = arbitrageService.findArbitrage(ExchangeEnum.KRAKEN, ExchangeEnum.BYBIT);
        var f2 = arbitrageService.findArbitrage(ExchangeEnum.BYBIT, ExchangeEnum.KRAKEN);
        var f3 = arbitrageService.findArbitrage(ExchangeEnum.BYBIT, ExchangeEnum.BINANCE);
        var f4 = arbitrageService.findArbitrage(ExchangeEnum.BINANCE, ExchangeEnum.BYBIT);
        var f5 = arbitrageService.findArbitrage(ExchangeEnum.BINANCE, ExchangeEnum.KRAKEN);
        var f6 = arbitrageService.findArbitrage(ExchangeEnum.KRAKEN, ExchangeEnum.BINANCE);

        CompletableFuture.allOf(f1, f2, f3, f4, f5, f6).join();

        var ls = f1.get();
        ls.addAll(f2.get());
        ls.addAll(f3.get());
        ls.addAll(f4.get());
        ls.addAll(f5.get());
        ls.addAll(f6.get());

        ls.forEach(profitCalculatorService::calculateProfit);

        ls.sort(new Arbitrage.ArbitrageProfitabilityComparator());

        return ls;

    }



}

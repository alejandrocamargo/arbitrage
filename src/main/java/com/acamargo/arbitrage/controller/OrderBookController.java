package com.acamargo.arbitrage.controller;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.ExchangeEnum;
import com.acamargo.arbitrage.dto.Symbol;
import com.acamargo.arbitrage.service.ArbitrageAggregatorService;
import com.acamargo.arbitrage.service.SymbolProvider;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@Slf4j
public class OrderBookController {

    private final SymbolProvider binanceService;
    private final SymbolProvider krakenService;
    private final SymbolProvider bybitService;
    private final SymbolProvider okxService;
    private final SymbolProvider bitfinexService;
    private final ArbitrageAggregatorService arbitrageAggregatorService;


    public OrderBookController(@Qualifier("binanceService") SymbolProvider binanceService,
                               @Qualifier("krakenService") SymbolProvider krakenService,
                               @Qualifier("bybitService") SymbolProvider bybitService,
                               @Qualifier("okxService") SymbolProvider okxService,
                               @Qualifier("bitfinexService") SymbolProvider bitfinexService,
                               ArbitrageAggregatorService arbitrageAggregatorService) {
        this.binanceService = binanceService;
        this.krakenService = krakenService;
        this.bybitService = bybitService;
        this.okxService = okxService;
        this.bitfinexService = bitfinexService;
        this.arbitrageAggregatorService = arbitrageAggregatorService;
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
            case OKX -> okxService.getOrderBook(symbol, count);
            case BITFINEX -> bitfinexService.getOrderBook(symbol, count);
        };

    }

    @GetMapping("arbitrage")
    public List<Arbitrage> doArbitrage() {

        log.info("Request received for arbitrage");
        return arbitrageAggregatorService.getArbitrages();

    }



}

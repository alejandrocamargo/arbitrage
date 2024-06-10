package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.ExchangeEnum;
import com.acamargo.arbitrage.dto.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ExchangeFacade {

    private final SymbolProvider binanceService;
    private final SymbolProvider krakenService;
    private final SymbolProvider bybitService;

    public ExchangeFacade(@Qualifier("binanceService") SymbolProvider binanceService,
                          @Qualifier("krakenService") SymbolProvider krakenService,
                          @Qualifier("bybitService") SymbolProvider bybitService) {
        this.binanceService = binanceService;
        this.krakenService = krakenService;
        this.bybitService = bybitService;
    }

    public List<Symbol> getSymbols(ExchangeEnum exchange) throws ExecutionException, InterruptedException {

        log.info("Getting symbols for {}", exchange);
        var ls = switch (exchange) {
            case BYBIT -> bybitService.getSymbols();
            case KRAKEN -> krakenService.getSymbols();
            case BINANCE -> binanceService.getSymbols();
        };

        log.info("Found {} symbols", ls);

        return ls;

    }

    @Cacheable("book")
    public Book getOrderBook(ExchangeEnum exchange, int count, Symbol symbol) throws ExecutionException, InterruptedException {

        return switch (exchange) {
            case BYBIT -> bybitService.getOrderBook(symbol.symbol(), count);
            case KRAKEN -> krakenService.getOrderBook(symbol.symbol(), count);
            case BINANCE -> binanceService.getOrderBook(symbol.symbol(), count);
        };
    }

    @CacheEvict(value = "book", allEntries = true)
    @Scheduled(fixedRateString = "60000")
    public void emptyBookCache() {
        log.info("emptying book cache");
    }
}

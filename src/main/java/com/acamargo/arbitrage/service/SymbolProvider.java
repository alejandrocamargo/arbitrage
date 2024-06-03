package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Symbol;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SymbolProvider {
    CompletableFuture<List<Symbol>> getSymbols();
    CompletableFuture<Book> getOrderBook(String symbol, int count);
}

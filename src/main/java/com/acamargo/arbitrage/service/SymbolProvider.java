package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.Symbol;

import java.util.List;

public interface SymbolProvider {
    List<Symbol> getSymbols();
    Book getOrderBook(String symbol, int count);
}

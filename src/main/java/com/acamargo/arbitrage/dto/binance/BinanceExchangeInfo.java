package com.acamargo.arbitrage.dto.binance;

import com.acamargo.arbitrage.dto.Symbol;

import java.util.List;

public record BinanceExchangeInfo(List<Symbol> symbols) {
}

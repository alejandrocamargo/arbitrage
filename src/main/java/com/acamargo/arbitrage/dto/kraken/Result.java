package com.acamargo.arbitrage.dto.kraken;

import com.acamargo.arbitrage.dto.Symbol;

import java.util.List;

public record Result(List<Symbol> symbols) {
}

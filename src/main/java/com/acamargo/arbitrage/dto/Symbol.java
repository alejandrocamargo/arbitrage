package com.acamargo.arbitrage.dto;

public record Symbol(String symbol) {
    public String getSanitizedSymbol() {
        return symbol
                .replace("XBT", "BTC")
                .replace("-", "")
                .replace("t", "");
    }

    @Override
    public boolean equals(Object obj) {
        Symbol other = (Symbol) obj;
        return this.getSanitizedSymbol().equalsIgnoreCase(other.getSanitizedSymbol());
    }
}

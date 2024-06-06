package com.acamargo.arbitrage.dto.binance;

public record AssetPrice(String symbol, String price) {

    public double getDoublePrice() {
        return Double.parseDouble(price());
    }
}

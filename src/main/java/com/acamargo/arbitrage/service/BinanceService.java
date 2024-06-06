package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import com.acamargo.arbitrage.dto.binance.AssetPrice;

import java.util.concurrent.CompletableFuture;

public interface BinanceService {
    AssetPrice getAssetPrice(String symbol);
}

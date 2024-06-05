package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.ExchangeEnum;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface ArbitrageService {

    CompletableFuture<List<Arbitrage>> findArbitrage(ExchangeEnum a, ExchangeEnum b) throws ExecutionException, InterruptedException;
}

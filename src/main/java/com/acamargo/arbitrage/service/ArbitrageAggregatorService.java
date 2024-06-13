package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;

import java.util.List;

public interface ArbitrageAggregatorService {
    void aggregate();
    List<Arbitrage> getArbitrages();

}

package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;

public interface ProfitCalculatorService {
    void calculateProfit(Arbitrage arbitrage);
}

package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.Symbol;
import com.acamargo.arbitrage.dto.binance.AssetPrice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitCalculatorServiceImplTest {

    @Mock
    BinanceService binanceService;


    Arbitrage arbitrage() throws Exception {

        return Arbitrage
                .builder()
                .symbol(new Symbol("ETCEUR"))
                .buyQuantity(10)
                .buyPrice(2500)
                .sellQuantity(20)
                .sellPrice(2600)
                .build();
    }

    @BeforeEach
    void setUp() {
        lenient().when(binanceService.getAssetPrice(any())).thenReturn(new AssetPrice("", "1.1"));
    }

    @Test
    void calculateProfitTest() throws Exception {
        ProfitCalculatorServiceImpl profitCalculatorService = new ProfitCalculatorServiceImpl(binanceService);

        Arbitrage arbitrage = arbitrage();

        profitCalculatorService.calculateProfit(arbitrage);

        assertEquals(1100, arbitrage.getProfitUsd());
    }

}
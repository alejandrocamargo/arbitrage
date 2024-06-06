package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.BaseAssetEnum;
import com.acamargo.arbitrage.dto.binance.AssetPrice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

@Service
@Slf4j
public class ProfitCalculatorServiceImpl implements ProfitCalculatorService {

    public static final Set<BaseAssetEnum> BASE_CURRENCIES = Set.of(BaseAssetEnum.USD,
            BaseAssetEnum.USDT, BaseAssetEnum.USDC);

    private final BinanceService binanceService;

    public ProfitCalculatorServiceImpl(BinanceService binanceService) {
        this.binanceService = binanceService;
    }

    public void calculateProfit(Arbitrage arbitrage) {
        double maxQuantity = Math.min(arbitrage.getBuyQuantity(), arbitrage.getSellQuantity());

        double buyTotal = arbitrage.getBuyPrice() * maxQuantity;
        double sellTotal = arbitrage.getSellPrice() * maxQuantity;

        double profit =  sellTotal - buyTotal;

        Arrays.stream(BaseAssetEnum.values())
                .filter(base -> arbitrage.getSymbol().symbol().endsWith(base.toString()))
                .findAny()
                .ifPresent(base -> {

                    if (BASE_CURRENCIES.contains(base)) {
                        arbitrage.setProfitUsd(profit);
                    } else {
                        AssetPrice assetPrice = binanceService.getAssetPrice(base + "USDT");
                        arbitrage.setProfitUsd(profit * assetPrice.getDoublePrice());
                    }

                });

    }


}

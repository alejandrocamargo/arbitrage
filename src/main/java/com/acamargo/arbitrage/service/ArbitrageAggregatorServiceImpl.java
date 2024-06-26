package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.ExchangeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArbitrageAggregatorServiceImpl implements ArbitrageAggregatorService {

    public static final double FILTER_PROFIT_PERCENTAGE_HIGH = 200d;
    public static final double FILTER_PROFIT_PERCENTAGE_LOW = 0.1d;

    private final ArbitrageService arbitrageService;
    private final ProfitCalculatorService profitCalculatorService;

    private final Map<Integer, Arbitrage> arbitrageMap = new HashMap<>();

    private final String blacklist;

    public ArbitrageAggregatorServiceImpl(ArbitrageService arbitrageService,
                                          ProfitCalculatorService profitCalculatorService,
                                          @Value("${assets.blacklist}") String blacklist) {

        this.arbitrageService = arbitrageService;
        this.profitCalculatorService = profitCalculatorService;
        this.blacklist = blacklist;
    }

    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    public void aggregate() {
        List<Arbitrage> ls;

        try {
            var f1 = arbitrageService.findArbitrage(ExchangeEnum.KRAKEN, ExchangeEnum.BYBIT);
            var f2 = arbitrageService.findArbitrage(ExchangeEnum.BYBIT, ExchangeEnum.KRAKEN);
            var f3 = arbitrageService.findArbitrage(ExchangeEnum.BYBIT, ExchangeEnum.BINANCE);
            var f4 = arbitrageService.findArbitrage(ExchangeEnum.BINANCE, ExchangeEnum.BYBIT);
            var f5 = arbitrageService.findArbitrage(ExchangeEnum.BINANCE, ExchangeEnum.KRAKEN);
            var f6 = arbitrageService.findArbitrage(ExchangeEnum.KRAKEN, ExchangeEnum.BINANCE);

//            var f7 = arbitrageService.findArbitrage(ExchangeEnum.OKX, ExchangeEnum.BYBIT);
//            var f8 = arbitrageService.findArbitrage(ExchangeEnum.BYBIT, ExchangeEnum.OKX);
//            var f9 = arbitrageService.findArbitrage(ExchangeEnum.OKX, ExchangeEnum.KRAKEN);
//            var f10 = arbitrageService.findArbitrage(ExchangeEnum.KRAKEN, ExchangeEnum.OKX);
//            var f11 = arbitrageService.findArbitrage(ExchangeEnum.OKX, ExchangeEnum.BINANCE);
//            var f12 = arbitrageService.findArbitrage(ExchangeEnum.BINANCE, ExchangeEnum.OKX);

//              CompletableFuture.allOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12).join();

            var f13 = arbitrageService.findArbitrage(ExchangeEnum.BITFINEX, ExchangeEnum.BYBIT);
            var f14 = arbitrageService.findArbitrage(ExchangeEnum.BITFINEX, ExchangeEnum.BINANCE);
            var f15 = arbitrageService.findArbitrage(ExchangeEnum.BITFINEX, ExchangeEnum.KRAKEN);
            var f16 = arbitrageService.findArbitrage(ExchangeEnum.BYBIT, ExchangeEnum.BITFINEX);
            var f17 = arbitrageService.findArbitrage(ExchangeEnum.BINANCE, ExchangeEnum.BITFINEX);
            var f18 = arbitrageService.findArbitrage(ExchangeEnum.KRAKEN, ExchangeEnum.BITFINEX);

           // CompletableFuture.allOf(f1, f2, f3, f4, f5, f6).join();

            CompletableFuture.allOf(f1, f2, f3, f4, f5, f6, f13, f14, f15, f16, f17, f18).join();

            ls = f1.get();
            ls.addAll(f2.get());
            ls.addAll(f3.get());
            ls.addAll(f4.get());
            ls.addAll(f5.get());
            ls.addAll(f6.get());

            ls.addAll(f13.get());
            ls.addAll(f14.get());
            ls.addAll(f15.get());
            ls.addAll(f16.get());
            ls.addAll(f17.get());
            ls.addAll(f18.get());



//            ls.addAll(f7.get());
//            ls.addAll(f8.get());
//            ls.addAll(f9.get());
//            ls.addAll(f10.get());
//            ls.addAll(f11.get());
//            ls.addAll(f12.get());

            ls.forEach(a -> log.info(a.toString()));

            // keep only the arbitrages currently on going
            Set<Integer> intersectionSet = arbitrageMap.keySet();

            // intersect keyset from Map with hashes from new iteration
            intersectionSet.retainAll(ls.stream().map(Arbitrage::hashCode).collect(Collectors.toSet()));

            // remove arbitrages from the map that no longer exist (not contained in the intersection)
            intersectionSet.forEach(k -> {
                if (!intersectionSet.contains(k)) {
                    arbitrageMap.remove(k);
                }
            });

            ls
                    .forEach(a -> {

                        if (!arbitrageMap.containsKey(a.hashCode())) {
                            profitCalculatorService.calculateProfit(a);
                            arbitrageMap.put(a.hashCode(), a);
                        } else {
                            log.info("Collision ! {}", a.getSymbol().symbol());
                        }

                    });


        } catch (InterruptedException | ExecutionException e) {
            log.error("Cannot aggregate arbitrages", e);
        }

    }

    public List<Arbitrage> getArbitrages() {
        // return an ordered list from map

        return new ArrayList<>(arbitrageMap.values())
                .stream()
                .filter(e -> e.getPercentageProfit().doubleValue() < FILTER_PROFIT_PERCENTAGE_HIGH)
                .filter(e -> e.getPercentageProfit().doubleValue() > FILTER_PROFIT_PERCENTAGE_LOW)
                .filter(e -> !List.of(blacklist.split(",")).contains(e.getSymbol().symbol()) )
                .sorted(new Arbitrage.ArbitrageProfitabilityComparator())
                .collect(Collectors.toList());
    }
}

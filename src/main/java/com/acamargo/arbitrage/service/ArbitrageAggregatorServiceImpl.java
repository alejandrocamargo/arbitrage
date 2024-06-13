package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Arbitrage;
import com.acamargo.arbitrage.dto.ExchangeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArbitrageAggregatorServiceImpl implements ArbitrageAggregatorService {

    private final ArbitrageService arbitrageService;
    private final ProfitCalculatorService profitCalculatorService;

    private final Map<Integer, Arbitrage> arbitrageMap = new HashMap<>();

    public ArbitrageAggregatorServiceImpl(ArbitrageService arbitrageService,
                                          ProfitCalculatorService profitCalculatorService) {

        this.arbitrageService = arbitrageService;
        this.profitCalculatorService = profitCalculatorService;
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

            CompletableFuture.allOf(f1, f2, f3, f4, f5, f6).join();

            ls = f1.get();
            ls.addAll(f2.get());
            ls.addAll(f3.get());
            ls.addAll(f4.get());
            ls.addAll(f5.get());
            ls.addAll(f6.get());

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
        List<Arbitrage> retList = new ArrayList<>(arbitrageMap.values());

        retList.sort(new Arbitrage.ArbitrageProfitabilityComparator());

        return retList;
    }
}

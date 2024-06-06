package com.acamargo.arbitrage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Comparator;

import static java.lang.String.format;

@Getter
@Setter
@Builder
public class Arbitrage {
    private Symbol symbol;
    private ExchangeEnum buyExchange;
    private ExchangeEnum sellExchange;

    private double buyPrice;
    private double buyQuantity;

    private double sellPrice;
    private double sellQuantity;

    private double profitUsd;

    public BigDecimal getPercentageProfit() {
        return BigDecimal.valueOf((sellPrice / buyPrice * 100) - 100);
    }

    @Override
    public String toString() {
        return format(
                "Arbitrage BUY {} ( {} ) ask={} q={} SELL ( {} ) bid={} q={} profit={}%",
                symbol.symbol(),
                buyExchange,
                buyPrice,
                buyQuantity,
                sellExchange,
                sellPrice,
                sellQuantity,
                getPercentageProfit()
        );
    }

    public static class ArbitrageProfitabilityComparator implements Comparator<Arbitrage> {
        @Override
        public int compare(Arbitrage o1, Arbitrage o2) {
            return o1.getProfitUsd() < o2.getProfitUsd() ? 1 : -1;
        }
    }
}

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

    private long timestamp;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Arbitrage arbitrage = (Arbitrage) o;
        return Double.compare(buyPrice, arbitrage.buyPrice) == 0 && Double.compare(buyQuantity, arbitrage.buyQuantity) == 0 && Double.compare(sellPrice, arbitrage.sellPrice) == 0 && Double.compare(sellQuantity, arbitrage.sellQuantity) == 0 && symbol.equals(arbitrage.symbol) && buyExchange == arbitrage.buyExchange && sellExchange == arbitrage.sellExchange;
    }

    @Override
    public int hashCode() {
        int result = symbol.hashCode();
        result = 31 * result + buyExchange.hashCode();
        result = 31 * result + sellExchange.hashCode();
        result = 31 * result + Double.hashCode(buyPrice);
        result = 31 * result + Double.hashCode(buyQuantity);
        result = 31 * result + Double.hashCode(sellPrice);
        result = 31 * result + Double.hashCode(sellQuantity);
        return result;
    }

    public static class ArbitrageProfitabilityComparator implements Comparator<Arbitrage> {
        @Override
        public int compare(Arbitrage o1, Arbitrage o2) {
            return o1.getProfitUsd() < o2.getProfitUsd() ? 1 : -1;
        }
    }
}

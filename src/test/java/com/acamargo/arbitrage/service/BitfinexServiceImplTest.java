package com.acamargo.arbitrage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BitfinexServiceImplTest {

    private BitfinexServiceImpl bitfinexService = new BitfinexServiceImpl();

    @Test
    public void testSymbols() {

        var symbolList = bitfinexService.getSymbols();

        Assertions.assertFalse(symbolList.isEmpty());

    }

    @Test
    public void testBook() {

        var book = bitfinexService.getOrderBook("tBTCUSD", 25);

        Assertions.assertEquals(1,book.bids().size());

    }

}
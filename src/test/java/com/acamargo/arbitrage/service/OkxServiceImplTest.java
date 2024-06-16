package com.acamargo.arbitrage.service;

import com.acamargo.arbitrage.dto.Book;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OkxServiceImplTest {
    private OkxServiceImpl okxService = new OkxServiceImpl();

    @Test
    public void orderBookTest() {

        Book book = okxService.getOrderBook("BTC-EUR", 1);

        Assert.assertTrue(book.asks().size() > 0);
    }

}

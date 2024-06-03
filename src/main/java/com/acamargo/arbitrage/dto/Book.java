package com.acamargo.arbitrage.dto;

import java.util.List;

public record Book(List<Order> bids, List<Order> asks) {

}

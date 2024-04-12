package com.corn.trade.model;

import java.time.LocalDateTime;

public record ExecutionData(String orderId, String assetName, LocalDateTime time, Double price, Double avgPrice, long quantity) {


}

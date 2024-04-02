package com.corn.trade.model;

import com.corn.trade.entity.Order;

public record BracketOrders (Order mainOrder, Order stopLossOrder, Order takeProfitOrder) {}

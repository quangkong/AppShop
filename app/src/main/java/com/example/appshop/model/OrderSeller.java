package com.example.appshop.model;

public class OrderSeller {
    private String orderId, orderTime, orderStatus
            , orderCost, deliveryFee, orderAddress, orderBy, orderTo;

    public OrderSeller() {
    }

    public OrderSeller(String orderId, String orderTime, String orderStatus
            , String orderCost, String deliveryFee
            , String orderAddress, String orderBy, String orderTo) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.orderStatus = orderStatus;
        this.orderCost = orderCost;
        this.deliveryFee = deliveryFee;
        this.orderAddress = orderAddress;
        this.orderBy = orderBy;
        this.orderTo = orderTo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(String orderCost) {
        this.orderCost = orderCost;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getOrderAddress() {
        return orderAddress;
    }

    public void setOrderAddress(String orderAddress) {
        this.orderAddress = orderAddress;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderTo() {
        return orderTo;
    }

    public void setOrderTo(String orderTo) {
        this.orderTo = orderTo;
    }
}

package com.nikos.retail.cart;

public enum CartStatus {
    ACTIVE, //Currently adding items
    HELD, //Cashier "saved" the cart
    CHECKED_OUT, //Converted into a sale/order
    ABANDONED //cancelled
}

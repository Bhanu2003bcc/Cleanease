package com.mrer.cleanease.entity;

public class Enums {

    public enum Role{
        CUSTOMER, ADMIN, STAFF
    }
    public enum OrderStatus{
        PENDING,
        CONFIRMED,
        IN_PROCESS,
        PICKED_UP,
        CLEANING,
        READY,
        OUT_FOR_DELIVERY,
        DELIVERED,
        COMPLETED,
        CANCELLED,
        FAILED
    }
    public enum ServiceCategory {
        DRY_CLEANING, LAUNDRY, PRESSING, ALTERATIONS, SPECIAL_CARE
    }
    public enum PaymentStatus{
        PENDING,
        PROCESSING,
        SUCCEEDED,
        FAILED,
        CANCELLED,
        REQUIRES_ACTION,
        REFUNDED
    }
    public enum PaymentMethod{
        CARD, DIGITAL_WALLET, CASH, UPI
    }
    public enum MessageStatus{
        SENT, DELIVERED, READ
    }
    public enum SenderType {
        USER, BOT
    }

}

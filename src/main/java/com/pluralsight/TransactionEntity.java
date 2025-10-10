package com.pluralsight;

import java.time.LocalDate;

public class TransactionEntity {
    private LocalDate date;
    private String time;
    private String description;
    private String vendor;
    private double amount;

    public TransactionEntity() {
    }

    public TransactionEntity(double amount, String vendor, String description, String time, LocalDate date) {
        this.amount = amount;
        this.vendor = vendor;
        this.description = description;
        this.time = time;
        this.date = date;
    }

    // Date could be wrong
    public void display(){
        System.out.printf("Vendor name: %s\nDescription: %s\nAmount: %.2f\nDate: %s", vendor, description, amount, date);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

package com.pluralsight;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TransactionDisplay {
    private final TransactionServices service;
    private final Scanner scanner = new Scanner(System.in);


    public TransactionDisplay(TransactionServices service) {
        this.service = service;
        service.readFile();
    }

    public void display(){
        System.out.println("\tWelcome to Financial Transaction\n");
        System.out.println("Please select the services provided: ");
        boolean hasExit = false;

        while(!hasExit){
            System.out.println("D) Add Deposit\nP) Make Payment(Debit)\nL) Ledger\nX) Exit\n");
            String input = scanner.nextLine();

            if(input.equalsIgnoreCase("D")){
                depositDisplay();
            } else if(input.equalsIgnoreCase("P")){
                displayPayment();
            } else if(input.equalsIgnoreCase("L")){

            } else if (input.equalsIgnoreCase("X")) {
                System.out.println("Leaving the app...");
                hasExit = true;
            } else {
                System.out.println("Wrong input. Please select a letter corresponding to the provided services\n");
            }

        }
    }

    public void depositDisplay(){
        boolean isValid = false;
        System.out.println("\nPlease enter your name: ");
        String name = scanner.nextLine();
        System.out.println("Please enter invoice: ");
        String invoice = scanner.nextLine();

        double depositAmount = 0;

        while(!isValid){
            try {

                System.out.println("Please enter deposit amount: ");
                depositAmount = scanner.nextDouble();
                scanner.nextLine();
                isValid = true;

            } catch (NumberFormatException e) {
                System.out.println("Wrong format. Please enter in numbers");
            }
        }
        service.saveToCSV(invoice, name, depositAmount, "deposit");
    }

    public void displayPayment(){
        System.out.println("Here is all payments you need to make\n");
        List<TransactionEntity> ongoingPayments = new ArrayList<>();

        // Loop through the entire list and only store if amounts is less than 0
        for(TransactionEntity transaction : service.getTransactionEntityList()){
            if(transaction.getAmount() < 0){
                ongoingPayments.add(transaction);
                transaction.display();
            }
        }

        boolean isValid = false;
        double amount = 0;
        String description = "";
        String vendorName = "";

        while(!isValid){
            try{
                System.out.println("\n\nPlease enter the vendor name owed: ");
                 vendorName = scanner.nextLine();
                System.out.println("Please enter description of payments: ");
                 description = scanner.nextLine();

                System.out.println("Please enter your payment amount: ");
                amount = scanner.nextDouble();
                scanner.nextLine();

                // Checks for validation of vendor name and amount
                for(TransactionEntity transaction : ongoingPayments){
                    if(transaction.getVendor().equalsIgnoreCase(vendorName)){
                        if (amount <= Math.abs(transaction.getAmount())){
                            amount += transaction.getAmount();
                            isValid = true;
                        } else {
                            System.out.println("\nYou have exceeded the amount owed! Please enter the correct amount");
                        }
                    } else {
                        System.out.println("\nVendor name not found! Please try again\n");
                    }
                }
            } catch (NumberFormatException e){
                System.out.println("Please enter a number");
            }
        }
        service.saveToCSV(description, vendorName, amount, amount == 0 ? "paid" : "");

    }
}

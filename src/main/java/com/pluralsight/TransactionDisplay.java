package com.pluralsight;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Scanner;

public class TransactionDisplay {
    private final TransactionServices service;
    private final Scanner scanner = new Scanner(System.in);


    public TransactionDisplay(TransactionServices service) {
        this.service = service;
    }

    public void display(){
        System.out.println("\tWelcome to Financial Transaction\n");
        System.out.println("Please select the services provided: ");
        boolean hasExit = false;

        while(!hasExit){
            System.out.println("D) Add Deposit\nP) Make Payment(Debit)\nL) Ledger\n X) Exit\n");
            String input = scanner.nextLine();

            if(input.equalsIgnoreCase("D")){
                depositDisplay();
            } else if(input.equalsIgnoreCase("P")){

            } else if(input.equalsIgnoreCase("L")){

            } else if (input.equalsIgnoreCase("X")) {
                System.out.println("Leaving the app...");
                hasExit = true;
            } else {
                System.out.println("Wrong input. Please select a letter corresponding to the provided services");
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
        service.saveToCSV(invoice, name, depositAmount);

    }
}

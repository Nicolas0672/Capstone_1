package com.pluralsight;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class TransactionDisplay {
    private final TransactionServices service;
    private final Scanner scanner = new Scanner(System.in);


    public TransactionDisplay(TransactionServices service) {
        this.service = service;
        service.readFile();
    }

    public void display() {
        System.out.println("\tWelcome to Financial Transaction\n");
        System.out.println("Please select the services provided: ");
        boolean hasExit = false;

        while (!hasExit) {
            System.out.println("D) Add Deposit\nP) Make Payment(Debit)\nL) Ledger\nX) Exit\n");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("D")) {
                depositDisplay();
            } else if (input.equalsIgnoreCase("P")) {
                displayPayment();
            } else if (input.equalsIgnoreCase("L")) {

            } else if (input.equalsIgnoreCase("X")) {
                System.out.println("Leaving the app...");
                hasExit = true;
            } else {
                System.out.println("Wrong input. Please select a letter corresponding to the provided services\n");
            }

        }
    }

    public void depositDisplay() {
        boolean isValid = false;
        System.out.println("\nPlease enter your name: ");
        String name = scanner.nextLine();
        System.out.println("Please enter invoice: ");
        String invoice = scanner.nextLine();

        double depositAmount = 0;

        while (!isValid) {
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

    public void displayPayment() {
        System.out.println("Here is all payments you need to make\n");
        List<TransactionEntity> ongoingPayments = new ArrayList<>();

        // Loop through the entire list and only store if amounts is less than 0
        for (TransactionEntity transaction : service.getTransactionEntityList()) {
            if (transaction.getAmount() < 0) {
                ongoingPayments.add(transaction);
                transaction.display();
            }
        }

        boolean isValid = false;
        double amount = 0;
        String description = "";
        String vendorName = "";

        while (!isValid) {
            try {
                System.out.println("\n\nPlease enter the vendor name owed: ");
                vendorName = scanner.nextLine();
                System.out.println("Please enter description of payments: ");
                description = scanner.nextLine();

                System.out.println("Please enter your payment amount: ");
                amount = scanner.nextDouble();
                scanner.nextLine();

                // Checks for validation of vendor name and amount
                for (TransactionEntity transaction : ongoingPayments) {
                    if (transaction.getVendor().equalsIgnoreCase(vendorName)) {
                        if (amount <= Math.abs(transaction.getAmount())) {
                            amount += transaction.getAmount();
                            isValid = true;
                        } else {
                            System.out.println("\nYou have exceeded the amount owed! Please enter the correct amount");
                        }
                    } else {
                        System.out.println("\nVendor name not found! Please try again\n");
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number");
            }
        }
        service.saveToCSV(description, vendorName, amount, amount == 0 ? "paid" : "payment");
    }

    public void displayLedger() {
        // read file to make sure it is up-to-date
        service.readFile();

        List<TransactionEntity> allTransactionEntityList = service.getTransactionEntityList();
        // Filter by newest entry first
        allTransactionEntityList.sort(Comparator.comparing(TransactionEntity::getDate));

        List<TransactionEntity> remainingTransactionPayments = new ArrayList<>();
        List<TransactionEntity> depositTransactionList = new ArrayList<>();


        // Checking for negative, ongoing payments or deposit payments
        for (TransactionEntity entity : allTransactionEntityList) {
            String description = entity.getDescription();
            // split the description and check if it is still an ongoing payment
            String[] parts = description.split(" ");
            if (entity.getAmount() < 0 || parts[parts.length - 1].equalsIgnoreCase("payment")) {
                remainingTransactionPayments.add(entity);
            } else if (entity.getAmount() > 0 && parts[parts.length - 1].equalsIgnoreCase("deposit")) {
                depositTransactionList.add(entity);
            }
        }

        boolean isValid = false;
        while (!isValid) {
            System.out.println("Please choose the services provided: \n");
            System.out.println("A) All\nD) Deposits\nP) Payments\nR) Reports\n");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("A")) {
                service.displayEntries(allTransactionEntityList);
                isValid = true;
            } else if (input.equalsIgnoreCase("D")) {
                service.displayEntries(depositTransactionList);
                isValid = true;
            } else if (input.equalsIgnoreCase("P")) {
                service.displayEntries(remainingTransactionPayments);
                isValid = true;
            } else if (input.equalsIgnoreCase("R")) {
                displayReports(allTransactionEntityList);
                isValid = true;
            } else if (input.equalsIgnoreCase("H")) {
                System.out.println("Going back home...");
                return;
            } else {
                System.out.println("Invalid options! Please try again");
            }
        }
    }

    public void displayReports(List<TransactionEntity> allTransactionList) {
        boolean isValid = false;

        while (!isValid) {
            try {
                System.out.println("Please select the provided services: ");
                System.out.println("1) Month To Date\n2)Previous Month\n3) Year To Date\n4) Previous Year\n5) Search by Vendor\n0) Back\n\n");
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        List<TransactionEntity> monthToDate = service.monthToDate();
                        displayList(monthToDate);
                        isValid = true;
                        break;
                    case 2:
                        List<TransactionEntity> prevMonth = service.previousMonth();
                        displayList(prevMonth);
                        isValid = true;
                        break;
                    case 3:
                        List<TransactionEntity> yearToDate = service.yearToDate();
                        displayList(yearToDate);
                        isValid = true;
                        break;
                    case 4:
                        List<TransactionEntity> prevYear = service.previousYear();
                        displayList(prevYear);
                        isValid = true;
                        break;
                    case 5:
                        displaySearchByVendor(allTransactionList);
                        isValid = true;
                        break;
                    case 6:
                        System.out.println("custom search");
                        isValid = true;
                        break;
                    default:
                        System.out.println("Incorrect options. Please try again! ");
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid options! Please try again");
            }
        }
    }

    public void displayList(List<TransactionEntity> entities) {
        for (TransactionEntity entity : entities) {
            entity.display();
        }
    }

    public void displaySearchByVendor(List<TransactionEntity> newestTransactionList) {
        boolean isValid = false;
        Map<String, List<TransactionEntity>> transactionMap = service.searchByVendor(newestTransactionList);
        List<TransactionEntity> searchedList = new ArrayList<>();
        while (!isValid) {
            System.out.println("Please enter vendor name you would like to search: ");
            String input = scanner.nextLine().toUpperCase();

            if (transactionMap.containsKey(input)) {
                searchedList.addAll(transactionMap.get(input));
                isValid = true;
            } else {
                System.out.println("Vendor name not found! Please try again\n");
            }
        }
        displayList(searchedList);
    }
}

package com.pluralsight;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import com.pluralsight.cli.console;

public class TransactionDisplay {
    private final TransactionServices service;
    private final Scanner scanner = new Scanner(System.in);
    private final List<TransactionEntity> transactionEntityList;


    // ------------------------------------------
    // Constructor
    // ------------------------------------------
    public TransactionDisplay(TransactionServices service) {
        this.service = service;
        this.transactionEntityList = service.readFile();
    }

    // ------------------------------------------
    // Main Menu
    // ------------------------------------------
    public void display() {
        System.out.println("\n==== Welcome to Financial Transaction ====\n");
        System.out.println("Please select the services provided: ");
        boolean hasExit = false;

        while (!hasExit) {
            console.Information("D) Add Deposit\nP) Make Payment(Debit)\nL) Ledger\nX) Exit\n");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("D")) {
                depositDisplay();
            } else if (input.equalsIgnoreCase("P")) {
                displayPayment();
            } else if (input.equalsIgnoreCase("L")) {
                displayLedger();
            } else if (input.equalsIgnoreCase("X")) {
                System.out.println("Leaving the app...");
                hasExit = true;
            } else {
                console.Deny("Wrong input. Please select a letter corresponding to the provided services\n");
            }
        }
    }

    // ------------------------------------------
    // Deposit Section
    // ------------------------------------------
    public void depositDisplay() {
        boolean isValid = false;
        System.out.println("\nPlease enter deposit name: ");
        String name = scanner.nextLine().trim();
        System.out.println("Please describe the purpose of this deposit:");
        String invoice = scanner.nextLine().trim();

        double depositAmount = 0;

        while (!isValid) {
            try {
                System.out.println("Please enter deposit amount: ");
                depositAmount = scanner.nextDouble();
                scanner.nextLine();
                if (depositAmount <= 0) {
                    console.Warning("Please enter a positive amount!");
                } else {
                    isValid = true;
                }

            } catch (InputMismatchException e) {
                console.Deny("Wrong format. Please enter in numbers");
                scanner.nextLine();
            }
        }
        service.saveToCSV(invoice, name, depositAmount, "deposit");
    }

    // ------------------------------------------
    // Payment Section
    // ------------------------------------------
    public void displayPayment() {
        console.Information("Here is all payments you need to make");
        List<TransactionEntity> ongoingPayments = new ArrayList<>();
        displayFormat();

        for (TransactionEntity transaction : transactionEntityList) {
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
                System.out.print("\nPlease enter the vendor name owed: ");
                vendorName = scanner.nextLine().trim();
                System.out.print("Please enter description of product owed: ");
                description = scanner.nextLine().trim();

                double totalPayment = service.totalPayment(ongoingPayments, vendorName, description);
                if (totalPayment == 0) {
                    console.Warning("Vendor name not found! Please try again");
                } else {
                    console.Information("Total amount owed for %s: %.2f", vendorName, totalPayment);
                    System.out.print("Please enter your payment: ");
                    amount = scanner.nextDouble();
                    scanner.nextLine();
                    if (totalPayment - amount < 0) {
                        console.Warning("You have exceeded total payment amount! Please try again");
                    } else {
                        console.Information("Remaining amount: %.2f", amount - totalPayment);
                        amount -= totalPayment;
                        isValid = true;
                    }
                }
            } catch (InputMismatchException e) {
                console.Deny("Please enter a number");
                scanner.nextLine();
            }
        }
        service.saveToCSV(description, vendorName, amount, amount == 0 ? "paid" : "payment");
    }

    // ------------------------------------------
    // Ledger Section
    // ------------------------------------------
    public void displayLedger() {
        List<TransactionEntity> allTransactionEntityList = service.readFile();
        // Sort it by newest date first
        allTransactionEntityList.sort(Comparator.comparing(TransactionEntity::getDate).reversed());

        List<TransactionEntity> remainingTransactionPayments = new ArrayList<>();
        List<TransactionEntity> depositTransactionList = new ArrayList<>();

        // Filtering which transaction is a payment or deposit from the last index
        for (TransactionEntity entity : allTransactionEntityList) {
            String description = entity.getDescription();
            String[] parts = description.split(" ");
            if (entity.getAmount() < 0 || parts[parts.length - 1].equalsIgnoreCase("payment")) {
                remainingTransactionPayments.add(entity);
            } else if (entity.getAmount() > 0 || parts[parts.length - 1].equalsIgnoreCase("deposit")) {
                depositTransactionList.add(entity);
            }
        }

        boolean isValid = false;
        while (!isValid) {
            System.out.println("Please choose the services provided: \n");
            console.Information("A) All\nD) Deposits\nP) Payments\nR) Reports\n");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("A")) {
                displayFormat();
                service.displayEntries(allTransactionEntityList);
                isValid = true;
            } else if (input.equalsIgnoreCase("D")) {
                displayFormat();
                service.displayEntries(depositTransactionList);
                isValid = true;
            } else if (input.equalsIgnoreCase("P")) {
                displayFormat();
                service.displayEntries(remainingTransactionPayments);
                isValid = true;
            } else if (input.equalsIgnoreCase("R")) {
                displayReports(allTransactionEntityList);
                isValid = true;
            } else if (input.equalsIgnoreCase("H")) {
                console.Information("Going back home...");
                return;
            } else {
                console.Deny("Invalid options! Please try again");
            }
        }
    }

    // ------------------------------------------
    // Reports Section
    // ------------------------------------------
    public void displayReports(List<TransactionEntity> allTransactionList) {
        boolean isValid = false;

        while (!isValid) {
            try {
                System.out.println("Please select the provided services: \n");
                console.Information(
                        "1) Month To Date\n2) Previous Month\n3) Year To Date\n4) Previous Year\n5) Search by Vendor\n6) Custom search\n0) Back\n"
                );
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        displayList(service.monthToDate(allTransactionList));
                        isValid = true;
                        break;
                    case 2:
                        displayList(service.previousMonth(allTransactionList));
                        isValid = true;
                        break;
                    case 3:
                        displayList(service.yearToDate(allTransactionList));
                        isValid = true;
                        break;
                    case 4:
                        displayList(service.previousYear(allTransactionList));
                        isValid = true;
                        break;
                    case 5:
                        displaySearchByVendor(allTransactionList);
                        isValid = true;
                        break;
                    case 6:
                        displayCustomSearch(allTransactionList);
                        isValid = true;
                        break;
                    case 0:
                        return;
                    default:
                        console.Deny("Incorrect options. Please try again! ");
                }

            } catch (InputMismatchException e) {
                console.Deny("Invalid options! Please try again");
                scanner.nextLine();
            }
        }
    }

    public void displayList(List<TransactionEntity> entities) {
        if (!entities.isEmpty()) {
            displayFormat();
            for (TransactionEntity entity : entities) {
                entity.display();
            }
        } else {
            console.Warning("No results were found");
        }
    }

    public void displaySearchByVendor(List<TransactionEntity> newestTransactionList) {
        boolean isValid = false;
        Map<String, List<TransactionEntity>> transactionMap = service.searchByVendor(newestTransactionList);
        List<TransactionEntity> searchedList = new ArrayList<>();
        while (!isValid) {
            System.out.println("Please enter vendor name you would like to search: ");
            String input = scanner.nextLine().toUpperCase().trim();

            if (transactionMap.containsKey(input)) {
                searchedList.addAll(transactionMap.get(input));
                isValid = true;
            } else {
                console.Warning("Vendor name not found! Please try again\n");
            }
        }
        displayList(searchedList);
    }

    public void displayCustomSearch(List<TransactionEntity> allTransactionList) {
        System.out.println("Please enter the fields for filtering\n");
        List<TransactionEntity> filteredList = allTransactionList;

        while (true) {
            System.out.println("Please enter start date (YYYY-MM-DD) or leave empty:");
            String startInput = scanner.nextLine().trim();
            try {
                // Check if it's parsable to enable throwing the correct error
                LocalDate startDate = parseDateOrNull(startInput);
                filteredList = service.customSearch(startInput, filteredList, "startDate");
                break;
            } catch (DateTimeParseException e) {
                console.Warning("Invalid date. Try again.");
            }
        }

        while (true) {
            System.out.println("Please enter end date (YYYY-MM-DD) or leave empty:");
            String endInput = scanner.nextLine().trim();
            try {
                LocalDate endDate = parseDateOrNull(endInput);
                filteredList = service.customSearch(endInput, filteredList, "endDate");
                break;
            } catch (DateTimeParseException e) {
                console.Warning("Invalid date. Try again.");
            }
        }

        System.out.println("Please enter description or leave empty:");
        String description = scanner.nextLine().trim();
        filteredList = service.customSearch(description, filteredList, "description");

        System.out.println("Please enter vendor name or leave empty:");
        String vendor = scanner.nextLine().trim();
        filteredList = service.customSearch(vendor, filteredList, "vendor");

        while (true) {
            System.out.println("Please enter amount or leave empty:");
            String amountInput = scanner.nextLine().trim();
            try {
                Double amount = parseAmountOrNull(amountInput);
                filteredList = service.customSearch(amountInput, filteredList, "amount");
                break;
            } catch (InputMismatchException e) {
                console.Warning("Invalid amount. Try again.");
            }
        }

        if (filteredList.isEmpty()) {
            console.Information("No search results were found!");
        } else {
            console.Information("Here are your custom search results");
            displayFormat();
            for (TransactionEntity entity : filteredList) {
                entity.display();
            }
        }
    }

    // ------------------------------------------
    // Helper Methods
    // ------------------------------------------
    public void displayFormat() {
        System.out.printf("\n%-20s %-30s %-12s %-15s\n", "Vendor", "Description", "Amount", "Date");
        System.out.println("--------------------------------------------------------------------------------");
    }

    private LocalDate parseDateOrNull(String input) {
        if (input == null || input.isEmpty()) return null;
        return LocalDate.parse(input);
    }

    private Double parseAmountOrNull(String input) {
        if (input == null || input.isEmpty()) return null;
        return Double.parseDouble(input);
    }
}

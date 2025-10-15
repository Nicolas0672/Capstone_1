package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.util.*;
import com.pluralsight.cli.console;

public class TransactionServices {

    private final String fileLocation = "data/transaction.csv";

    public void saveToCSV(String description, String vendor, double amount, String activity) {
        try {
            LocalDate today = LocalDate.now();
            LocalTime todayTime = LocalTime.now();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileLocation, true));
            // Format correctly
            bufferedWriter.write(String.format("\n%s|%s|%s %s|%s|%.2f", today, todayTime.withNano(0), description, activity, vendor, amount));
            console.Success("\nCongrats! Your request is completed\n");

            bufferedWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TransactionEntity> readFile() {
        List<TransactionEntity> entityList = new ArrayList<>();
        // Read data from CSV file
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileLocation));
            bufferedReader.readLine();
            String input = "";

            while ((input = bufferedReader.readLine()) != null) {
                String[] parts = input.split("\\|");
                if (parts.length == 5) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    double amount = Double.parseDouble(parts[4]);
                    TransactionEntity entity = new TransactionEntity(amount, parts[3], parts[2], parts[1], date);
                    entityList.add(entity);
                }
            }
        } catch (IOException e) {
            console.Deny("File not found");
        }
        return entityList;
    }

    public void displayEntries(List<TransactionEntity> entities) {
        for (TransactionEntity entity : entities) {
            entity.display();
        }
    }

    public List<TransactionEntity> monthToDate(List<TransactionEntity> transactionEntityList) {
        List<TransactionEntity> monthToDateList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = LocalDate.of(today.getYear(), today.getMonth(), 1);

        // Filtering based on the first day of month and now
        // Stores all valid entities within range
        for (TransactionEntity transaction : transactionEntityList) {
            LocalDate date = transaction.getDate();
            if (((date.isAfter(firstDayOfMonth)) || date.isEqual(firstDayOfMonth)) &&
                    date.isBefore(today) || date.isEqual(today)) {
                monthToDateList.add(transaction);
            }
        }
        return monthToDateList;
    }

    public List<TransactionEntity> yearToDate(List<TransactionEntity> transactionEntityList) {
        List<TransactionEntity> YearToDateList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfYear = LocalDate.of(today.getYear() - 1, today.getMonth(), 1);

        // Filtering based on the first day of month and now
        // Stores all valid entities within range
        for (TransactionEntity transaction : transactionEntityList) {
            LocalDate date = transaction.getDate();
            if (((date.isAfter(firstDayOfYear)) || date.isEqual(firstDayOfYear)) &&
                    date.isBefore(today) || date.isEqual(today)) {
                YearToDateList.add(transaction);
            }
        }
        return YearToDateList;
    }

    public List<TransactionEntity> previousMonth(List<TransactionEntity> transactionEntityList) {
        List<TransactionEntity> previousMonthList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Check if last month was december
        int year = today.getYear();
        Month month = today.getMonth().minus(-1);

        if (month == Month.DECEMBER) {
            year = year - 1;
        }

        LocalDate firstDayOfPrevMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfPrevMonth = firstDayOfPrevMonth.withDayOfMonth(firstDayOfPrevMonth.lengthOfMonth());

        for (TransactionEntity transaction : transactionEntityList) {
            LocalDate date = transaction.getDate();
            if (((date.isAfter(firstDayOfPrevMonth)) || date.isEqual(firstDayOfPrevMonth)) &&
                    date.isBefore(lastDayOfPrevMonth) || date.isEqual(lastDayOfPrevMonth)) {
                previousMonthList.add(transaction);
            }
        }
        return previousMonthList;
    }

    public List<TransactionEntity> previousYear(List<TransactionEntity> transactionEntityList) {
        List<TransactionEntity> previousYearList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Check if last month was december
        int year = today.getYear() - 1;

        LocalDate firstDayOfPrevYear = LocalDate.of(year, 1, 31);
        LocalDate lastDayOfPrevYear = LocalDate.of(year, 12, 31);

        for (TransactionEntity transaction : transactionEntityList) {
            LocalDate date = transaction.getDate();
            if (((date.isAfter(firstDayOfPrevYear)) || date.isEqual(firstDayOfPrevYear)) &&
                    date.isBefore(lastDayOfPrevYear) || date.isEqual(lastDayOfPrevYear)) {
                previousYearList.add(transaction);
            }
        }
        return previousYearList;
    }
    // Groups transactions by vendor using a HashMap for quick lookup.
    // Key: Vendor name (uppercase for case-insensitivity)
    // Value: List of transactions for that vendor.

    public Map<String, List<TransactionEntity>> searchByVendor(List<TransactionEntity> newestList) {
        Map<String, List<TransactionEntity>> transactionMap = new HashMap<>();

        // Populate map with vendor name as keys and entity as value
        for (TransactionEntity entity : newestList) {
            // Sensitive check
            String vendorName = entity.getVendor().toUpperCase();
            transactionMap.putIfAbsent(vendorName, new ArrayList<>());
        }

        for (TransactionEntity entity : newestList) {
            // Case sensitive check
            String vendorName = entity.getVendor().toUpperCase();
            if (transactionMap.containsKey(vendorName)) {
                transactionMap.get(vendorName).add(entity);
            }
        }
        return transactionMap;
    }
    // Core filtering logic for the "Custom Search" feature.
    // Depending on filterType, it applies appropriate checks:
    // - startDate / endDate → compare LocalDate
    // - description / vendor → substring match (case-insensitive)
    // - amount → numeric equality (within a small tolerance)
    // Returns a filtered list without modifying the original.

    public List<TransactionEntity> customSearch(String input, List<TransactionEntity> transactionEntityList, String filterType) {

        if (input == null || input.isEmpty()) {
            return transactionEntityList; // do not filter
        }

        List<TransactionEntity> filterFound = new ArrayList<>();

        // Parse once
        LocalDate date = isDate(input);
        Double amount = isAmount(input) ? Double.parseDouble(input) : null;

        for (TransactionEntity entity : transactionEntityList) {
            switch (filterType) {
                case "startDate":
                    if (date != null && (entity.getDate().isAfter(date) || entity.getDate().isEqual(date))) {
                        filterFound.add(entity);
                    }
                    break;
                case "endDate":
                    if (date != null && (entity.getDate().isBefore(date) || entity.getDate().isEqual(date))) {
                        filterFound.add(entity);
                    }
                    break;
                case "amount":
                    if (amount != null && Math.abs(entity.getAmount() - amount) < 0.01) {
                        filterFound.add(entity);
                    }
                    break;
                case "description":
                    if (entity.getDescription().toLowerCase().contains(input.toLowerCase())) {
                        filterFound.add(entity);
                    }
                    break;
                case "vendor":
                    if (entity.getVendor().toLowerCase().contains(input.toLowerCase())) {
                        filterFound.add(entity);
                    }
                    break;
            }
        }

        return filterFound;
    }

    private LocalDate isDate(String input) {
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isAmount(String input) {
        try {
            double amount = Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Calculates total outstanding payment owed to a specific vendor
    // by summing all matching negative transactions (case-insensitive).
    // Used to validate user payment inputs against actual owed amounts.

    public double totalPayment(List<TransactionEntity> ongoingPayments, String vendorName, String description) {
        double amount = 0;
        for (TransactionEntity transaction : ongoingPayments) {
            if (transaction.getVendor().equalsIgnoreCase(vendorName) && transaction.getDescription().toLowerCase().contains(description.toLowerCase())) {
                amount += Math.abs(transaction.getAmount());
            }
        }
        return amount;
    }


}

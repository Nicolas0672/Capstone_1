package com.pluralsight;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import com.pluralsight.cli.console;

public class TransactionServices {

    private final String fileLocation = "data/transaction.csv";

    // =============================================================
    // 1. CORE FILE I/O METHODS
    // =============================================================

    /**
     * Appends a new transaction record to the CSV file.
     * Format: date|time|description activity|vendor|amount
     */
    public void saveToCSV(String description, String vendor, double amount, String activity) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileLocation, true))) {
            LocalDate today = LocalDate.now();
            LocalTime todayTime = LocalTime.now();

            bufferedWriter.write(String.format(
                    "\n%s|%s|%s %s|%s|%.2f",
                    today,
                    todayTime.withNano(0),
                    description,
                    activity,
                    vendor,
                    amount
            ));

            console.Success("\nCongrats! Your request is completed\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads all transactions from the CSV file and returns them as TransactionEntity objects.
     */
    public List<TransactionEntity> readFile() {
        List<TransactionEntity> entityList = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileLocation))) {
            bufferedReader.readLine(); // Skip header if exists
            String input;

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

    // =============================================================
    // 2. DISPLAY UTILITIES
    // =============================================================

    /**
     * Displays all entities in the provided list.
     */
    public void displayEntries(List<TransactionEntity> entities) {
        for (TransactionEntity entity : entities) {
            entity.display();
        }
    }

    // =============================================================
    // 3. REPORT FILTERS (DATE-BASED)
    // =============================================================

    /**
     * Returns all transactions from the start of the current month to today.
     */
    public List<TransactionEntity> monthToDate(List<TransactionEntity> transactions) {
        List<TransactionEntity> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        for (TransactionEntity transaction : transactions) {
            LocalDate date = transaction.getDate();
            if ((date.isAfter(firstDayOfMonth) || date.isEqual(firstDayOfMonth))
                    && (date.isBefore(today) || date.isEqual(today))) {
                result.add(transaction);
            }
        }
        return result;
    }

    /**
     * Returns all transactions from the previous month.
     */
    public List<TransactionEntity> previousMonth(List<TransactionEntity> transactions) {
        List<TransactionEntity> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        Month prevMonth = today.getMonth().minus(1);
        int year = today.getYear();
        if (today.getMonth() == Month.JANUARY) {
            year -= 1;
            prevMonth = Month.DECEMBER;
        }

        LocalDate firstDayOfPrev = LocalDate.of(year, prevMonth, 1);
        LocalDate lastDayOfPrev = firstDayOfPrev.withDayOfMonth(firstDayOfPrev.lengthOfMonth());

        for (TransactionEntity t : transactions) {
            LocalDate date = t.getDate();
            if ((date.isAfter(firstDayOfPrev) || date.isEqual(firstDayOfPrev))
                    && (date.isBefore(lastDayOfPrev) || date.isEqual(lastDayOfPrev))) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Returns all transactions from the start of the current year to today.
     */
    public List<TransactionEntity> yearToDate(List<TransactionEntity> transactions) {
        List<TransactionEntity> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfYear = LocalDate.of(today.getYear(), 1, 1);

        for (TransactionEntity transaction : transactions) {
            LocalDate date = transaction.getDate();
            if ((date.isAfter(firstDayOfYear) || date.isEqual(firstDayOfYear))
                    && (date.isBefore(today) || date.isEqual(today))) {
                result.add(transaction);
            }
        }
        return result;
    }

    /**
     * Returns all transactions from the previous calendar year.
     */
    public List<TransactionEntity> previousYear(List<TransactionEntity> transactions) {
        List<TransactionEntity> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int prevYear = today.getYear() - 1;

        LocalDate firstDayOfPrevYear = LocalDate.of(prevYear, 1, 1);
        LocalDate lastDayOfPrevYear = LocalDate.of(prevYear, 12, 31);

        for (TransactionEntity t : transactions) {
            LocalDate date = t.getDate();
            if ((date.isAfter(firstDayOfPrevYear) || date.isEqual(firstDayOfPrevYear))
                    && (date.isBefore(lastDayOfPrevYear) || date.isEqual(lastDayOfPrevYear))) {
                result.add(t);
            }
        }
        return result;
    }

    // =============================================================
    // 4. SEARCH & CUSTOM FILTERS
    // =============================================================

    /**
     * Groups transactions by vendor name (case-insensitive).
     */
    public Map<String, List<TransactionEntity>> searchByVendor(List<TransactionEntity> newestList) {
        Map<String, List<TransactionEntity>> map = new HashMap<>();

        for (TransactionEntity entity : newestList) {
            String vendor = entity.getVendor().toUpperCase();
            map.putIfAbsent(vendor, new ArrayList<>());
            map.get(vendor).add(entity);
        }
        return map;
    }

    /**
     * Filters transactions dynamically by date, vendor, description, or amount.
     */
    public List<TransactionEntity> customSearch(String input, List<TransactionEntity> list, String filterType) {
        if (input == null || input.isEmpty()) return list;

        List<TransactionEntity> filtered = new ArrayList<>();
        LocalDate date = isDate(input);
        Double amount = isAmount(input) ? Double.parseDouble(input) : null;

        for (TransactionEntity entity : list) {
            switch (filterType) {
                case "startDate":
                    if (date != null && (entity.getDate().isAfter(date) || entity.getDate().isEqual(date)))
                        filtered.add(entity);
                    break;
                case "endDate":
                    if (date != null && (entity.getDate().isBefore(date) || entity.getDate().isEqual(date)))
                        filtered.add(entity);
                    break;
                case "amount":
                    if (amount != null && Math.abs(entity.getAmount() - amount) < 0.01)
                        filtered.add(entity);
                    break;
                case "description":
                    if (entity.getDescription().toLowerCase().contains(input.toLowerCase()))
                        filtered.add(entity);
                    break;
                case "vendor":
                    if (entity.getVendor().toLowerCase().contains(input.toLowerCase()))
                        filtered.add(entity);
                    break;
            }
        }

        return filtered;
    }

    // =============================================================
    // 5. PAYMENT CALCULATIONS
    // =============================================================

    /**
     * Calculates total amount owed to a vendor based on negative transactions.
     */
    public double totalPayment(List<TransactionEntity> ongoingPayments, String vendorName, String description) {
        double amount = 0;
        for (TransactionEntity t : ongoingPayments) {
            if (t.getVendor().equalsIgnoreCase(vendorName)
                    && t.getDescription().toLowerCase().contains(description.toLowerCase())) {
                amount += Math.abs(t.getAmount());
            }
        }
        return amount;
    }

    // =============================================================
    // 6. PRIVATE HELPER METHODS
    // =============================================================

    private LocalDate isDate(String input) {
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isAmount(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

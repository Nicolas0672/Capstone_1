package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;

public class TransactionServices {

    private final List<TransactionEntity> transactionEntityList = new ArrayList<>();
    private final String fileLocation = "data/transaction.csv";

    public List<TransactionEntity> getTransactionEntityList() {
        return transactionEntityList;
    }

    public void saveToCSV(String description, String vendor, double amount, String activity){
        try {
            LocalDate today = LocalDate.now();
            LocalTime todayTime = LocalTime.now();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileLocation, true));


            bufferedWriter.write(String.format("\n%s|%s|%s|%s %s|%.2f", today, todayTime.withNano(0), description, vendor, activity, amount));
            System.out.println("\nCongrats! Your request is completed\n");
            readFile();
            bufferedWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readFile(){
        // Read data from CSV file
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileLocation));
            bufferedReader.readLine();
            String input = "";

            while((input = bufferedReader.readLine()) != null){
                String[] parts = input.split("\\|");
                if(parts.length == 5){
                    LocalDate date = LocalDate.parse(parts[0]);
                    double amount = Double.parseDouble(parts[4]);
                    TransactionEntity entity = new TransactionEntity(amount, parts[3], parts[2], parts[1], date);
                    transactionEntityList.add(entity);
                }
            }
        } catch (IOException e) {
            System.out.println("File not found");
        }
    }

    public void displayEntries(List<TransactionEntity> entities){
        for(TransactionEntity entity : entities){
            entity.display();
        }
    }

    public List<TransactionEntity> monthToDate(){
        List<TransactionEntity> monthToDateList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = LocalDate.of(today.getYear(), today.getMonth(), 1);

        // Filtering based on the first day of month and now
        // Stores all valid entities within range
        for(TransactionEntity transaction : transactionEntityList){
            LocalDate date = transaction.getDate();
            if(((date.isAfter(firstDayOfMonth)) || date.isEqual(firstDayOfMonth)) &&
            date.isBefore(today) || date.isEqual(today)){
                monthToDateList.add(transaction);
            }
        }
        return  monthToDateList;
    }

    public List<TransactionEntity> yearToDate(){
        List<TransactionEntity> YearToDateList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfYear = LocalDate.of(today.getYear() - 1, today.getMonth(), 1);

        // Filtering based on the first day of month and now
        // Stores all valid entities within range
        for(TransactionEntity transaction : transactionEntityList){
            LocalDate date = transaction.getDate();
            if(((date.isAfter(firstDayOfYear)) || date.isEqual(firstDayOfYear)) &&
                    date.isBefore(today) || date.isEqual(today)){
                YearToDateList.add(transaction);
            }
        }
        return  YearToDateList;
    }

    public List<TransactionEntity> previousMonth(){
        List<TransactionEntity> previousMonthList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Check if last month was december
        int year = today.getYear();
        Month month = today.getMonth().minus(-1);

        if (month == Month.DECEMBER){
            year = year - 1;
        }

        LocalDate firstDayOfPrevMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfPrevMonth = firstDayOfPrevMonth.withDayOfMonth(firstDayOfPrevMonth.lengthOfMonth());

        for(TransactionEntity transaction : transactionEntityList){
            LocalDate date = transaction.getDate();
            if(((date.isAfter(firstDayOfPrevMonth)) || date.isEqual(firstDayOfPrevMonth)) &&
                    date.isBefore(lastDayOfPrevMonth) || date.isEqual(lastDayOfPrevMonth)){
                previousMonthList.add(transaction);
            }
        }
        return  previousMonthList;
    }

    public List<TransactionEntity> previousYear(){
        List<TransactionEntity> previousYearList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Check if last month was december
        int year = today.getYear() - 1;

        LocalDate firstDayOfPrevYear = LocalDate.of(year, 1, 31);
        LocalDate lastDayOfPrevYear = LocalDate.of(year, 12, 31);

        for(TransactionEntity transaction : transactionEntityList){
            LocalDate date = transaction.getDate();
            if(((date.isAfter(firstDayOfPrevYear)) || date.isEqual(firstDayOfPrevYear)) &&
                    date.isBefore(lastDayOfPrevYear) || date.isEqual(lastDayOfPrevYear)){
                previousYearList.add(transaction);
            }
        }
        return  previousYearList;
    }

    public Map<String, List<TransactionEntity>> searchByVendor(List<TransactionEntity> newestList){
        Map<String, List<TransactionEntity>> transactionMap = new HashMap<>();

        // Populate map with vendor name as keys and entity as value
        for (TransactionEntity entity : newestList){
            // Sensitive check
            String vendorName = entity.getVendor().toUpperCase();
            transactionMap.putIfAbsent(vendorName, new ArrayList<>());
        }

        for (TransactionEntity entity : newestList){
            // Case sensitive check
            String vendorName = entity.getVendor().toUpperCase();
            if(transactionMap.containsKey(vendorName)){
                transactionMap.get(vendorName).add(entity);
            }
        }
        return transactionMap;
    }

}

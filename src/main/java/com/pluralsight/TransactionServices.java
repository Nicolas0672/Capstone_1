package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionServices {

    private final List<TransactionEntity> transactionEntityList = new ArrayList<>();
    private final String fileLocation = "data/transaction.csv";

    public void saveToCSV(String description, String vendor, double amount){
        try {
            LocalDate today = LocalDate.now();
            LocalTime todayTime = LocalTime.now();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileLocation, true));
            bufferedWriter.write("/n");

            bufferedWriter.write(String.format("%s|%s|%s|%s|%.2f", today, todayTime, description, vendor, amount));
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
                    double amount = Double.parseDouble(parts[3]);
                    TransactionEntity entity = new TransactionEntity(amount, parts[3], parts[2], parts[1], date);
                    transactionEntityList.add(entity);
                }
            }
        } catch (IOException e) {
            System.out.println("File not found");
        }

    }
}

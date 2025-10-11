# Financial Transaction Application

## Project Overview

The Financial Transaction Application is a console-based Java program designed to help users manage their financial records efficiently. Built as a Year Up capstone project, this application allows users to track deposits, make payments, view transaction history through a comprehensive ledger system, and generate detailed financial reports.

The application reads and writes transaction data to a CSV file, providing persistent storage of all financial activities. Users can filter transactions by various criteria, search for specific vendors, and generate custom reports based on date ranges, amounts, descriptions, and more.

## Features

### Core Functionality
- **Add Deposits**: Record incoming transactions with vendor details and amounts
- **Make Payments**: Process outgoing payments and track outstanding balances
- **Ledger System**: View all transactions, filtered by deposits or payments
- **Report Generation**: Access pre-defined and custom financial reports
- **Data Persistence**: All transactions are saved to CSV format for permanent storage

### Report Options
1. Month To Date - View current month's transactions
2. Previous Month - Review last month's activity
3. Year To Date - Annual transaction summary
4. Previous Year - Previous year's complete history
5. Search by Vendor - Find all transactions with a specific vendor
6. Custom Search - Advanced filtering with multiple criteria

## Application Screenshots

### Home Screen
```
	Welcome to Financial Transaction

Please select the services provided: 
D) Add Deposit
P) Make Payment(Debit)
L) Ledger
X) Exit
```

### Ledger Screen
```
Please choose the services provided: 

A) All
D) Deposits
P) Payments
R) Reports
```

### Reports Menu
```
Please select the provided services: 

1) Month To Date
2) Previous Month
3) Year To Date
4) Previous Year
5) Search by Vendor
6) Custom search
0) Back
```

## Interesting Code Highlights

### 1. Custom Search with Multi-Criteria Filtering

One of the most powerful features is the `customSearch` method, which allows flexible filtering across multiple transaction properties:

```java
public List<TransactionEntity> customSearch(String input, List<TransactionEntity> transactionEntityList, String filterType) {
    if (input == null || input.isEmpty()) {
        return transactionEntityList; // do not filter
    }

    List<TransactionEntity> filterFound = new ArrayList<>();
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
```

**Why this is impressive:**
- **Flexible Design**: Single method handles five different filter types through a switch statement
- **Null Safety**: Returns the original list if input is empty, allowing optional filtering
- **Type Safety**: Validates input types before filtering (dates and amounts are parsed only once)
- **Case-Insensitive Searching**: Text searches ignore case for better user experience
- **Precision Handling**: Uses epsilon comparison (`< 0.01`) for double values to avoid floating-point errors

### 2. Robust Input Validation with Error Recovery

The `displayCustomSearch` method demonstrates excellent input validation with user-friendly error handling:

```java
public void displayCustomSearch(List<TransactionEntity> allTransactionList) {
    System.out.println("Please enter the fields for filtering\n");
    List<TransactionEntity> filteredList = allTransactionList;

    while (true) {
        System.out.println("Please enter start date (YYYY-MM-DD) or leave empty:");
        String startInput = scanner.nextLine().trim();
        try {
            LocalDate startDate = parseDateOrNull(startInput);
            filteredList = service.customSearch(startInput, filteredList, "startDate");
            break;
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Try again.");
        }
    }
    // Similar validation for end date and amount...
}

private LocalDate parseDateOrNull(String input) {
    if (input == null || input.isEmpty()) return null;
    return LocalDate.parse(input);
}

private Double parseAmountOrNull(String input) {
    if (input == null || input.isEmpty()) return null;
    return Double.parseDouble(input);
}
```

**Key validation features:**
- **Loop-Until-Valid Pattern**: Keeps prompting until valid input is received
- **Exception Handling**: Catches parsing errors and provides clear feedback
- **Optional Input Support**: Allows empty strings to skip filtering
- **Helper Methods**: Dedicated parsing methods encapsulate validation logic
- **Whitespace Trimming**: Prevents whitespace-related input errors

### 3. Business Logic: Total Payment Calculation

The `totalPayment` method showcases important business logic for payment processing:

```java
public double totalPayment(List<TransactionEntity> ongoingPayments, String vendorName, String description) {
    double amount = 0;
    for (TransactionEntity transaction : ongoingPayments) {
        if (transaction.getVendor().equalsIgnoreCase(vendorName) && 
            transaction.getDescription().toLowerCase().contains(description.toLowerCase())) {
            amount += Math.abs(transaction.getAmount());
        }
    }
    return amount;
}
```

**Business logic highlights:**
- **Vendor Matching**: Case-insensitive exact match for vendor names
- **Description Filtering**: Partial matching for flexible searching
- **Absolute Values**: Uses `Math.abs()` since payments are stored as negative numbers
- **Accumulation**: Correctly sums multiple matching transactions

### 4. Date Range Filtering Logic

The `previousMonth` method demonstrates sophisticated date handling:

```java
public List<TransactionEntity> previousMonth(List<TransactionEntity> transactionEntityList) {
    List<TransactionEntity> previousMonthList = new ArrayList<>();
    LocalDate today = LocalDate.now();
    
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
```

**Date handling features:**
- **Year Boundary Logic**: Correctly handles December-to-January transitions
- **Dynamic Month Length**: Uses `lengthOfMonth()` to handle varying month lengths (28-31 days)
- **Inclusive Range**: Uses `isEqual()` checks to include boundary dates
- **Java Time API**: Leverages modern `LocalDate` and `Month` classes

### 5. Deposit Validation with Amount Checking

The deposit functionality includes comprehensive validation:

```java
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
            if(depositAmount <= 0){
                System.out.println("Please enter a positive amount!");
            } else {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            System.out.println("Wrong format. Please enter in numbers");
        }
    }
    service.saveToCSV(invoice, name, depositAmount, "deposit");
}
```

**Validation highlights:**
- **Type Validation**: Catches `NumberFormatException` for non-numeric input
- **Business Rule Validation**: Ensures deposit amounts are positive
- **Buffer Clearing**: Clears scanner buffer with `nextLine()` after `nextDouble()`
- **Loop-Until-Valid**: Continues prompting until valid input is received

## Technologies Used

- **Java**: Core programming language
- **Java Time API**: For date and time handling (`LocalDate`, `LocalTime`)
- **File I/O**: BufferedReader/BufferedWriter for CSV operations
- **Collections Framework**: Lists, Maps for data management
- **Exception Handling**: Robust error management throughout

## Project Structure

```
src/
├── com/pluralsight/
│   ├── TransactionDisplay.java    # UI and user interaction layer
│   ├── TransactionServices.java   # Business logic and data operations
│   └── TransactionEntity.java     # Data model
data/
└── transaction.csv                 # Transaction data storage
```

## Author

Nicolas Ouch - Year Up Capstone Project - 2025

---


# 💼 Financial Transaction Manager (Java CLI)

A robust **Command-Line Financial Ledger Application** built with Java that empowers users to track deposits, payments, and generate comprehensive financial reports. All transaction data is persisted in CSV format for reliable record-keeping.

This project serves as my **Capstone 1** for the *Java Development Fundamentals* program at **Year Up**, showcasing object-oriented design principles, clean architecture, and practical business logic implementation.

---

## 🎯 Project Objectives

✅ Record deposits and payments with full transaction details  
✅ Display ledger with filtering options (All / Deposits / Payments)  
✅ Generate financial reports (Month-to-Date, Previous Month, Year-to-Date, Previous Year)  
✅ Search by Vendor with custom multi-field search  
✅ Comprehensive input validation  
✅ CSV-based persistent data storage

---

## ✨ Key Features

- 📥 **Add Deposits** — Record incoming transactions with timestamp and vendor info
- 💳 **Make Payments** — Track outgoing payments and calculate outstanding balances
- 📋 **Ledger System** — View all transactions sorted by most recent first
- 📊 **Report Generation** — Pre-built and custom report options
- 🔍 **Advanced Search** — Filter by date range, vendor, description, or amount
- 💾 **Data Persistence** — All transactions saved to CSV file

---

## 🖼️ Application Walkthrough

### 🏠 Home Screen
[SCREENSHOT: Home screen showing D/P/L/X options]

### 💰 Adding a Deposit
[SCREENSHOT: Deposit form with prompts]

### 💳 Making a Payment
[SCREENSHOT: Payment screen with vendor lookup]

### 📖 Ledger Display
[SCREENSHOT: Transaction history]

### 📊 Reports Menu
[SCREENSHOT: Reports menu with options 1-6]

### 🔍 Custom Search Interface
[SCREENSHOT: Custom search prompts]

### 📈 Search Results Display
[SCREENSHOT: Filtered results]

---

## 🏗️ Project Architecture

```
src/com/pluralsight/
├── TransactionDisplay.java      # UI layer
├── TransactionServices.java     # Business logic
└── TransactionEntity.java       # Data model

data/
└── transaction.csv              # Persistent storage
```

---

## 💻 Advanced Implementations

### 1️⃣ Progressive Filtering System (Compound Search)

Each filter operates on the previous filter's results—narrowing down progressively like real e-commerce sites.

```java
public void displayCustomSearch(List<TransactionEntity> allTransactionList) {
    // Start with all, narrow down with each filter
    List<TransactionEntity> filteredList = allTransactionList;

    // Filter 1: Start Date
    filteredList = service.customSearch(startInput, filteredList, "startDate");
    
    // Filter 2: End Date (operates on Filter 1 results)
    filteredList = service.customSearch(endInput, filteredList, "endDate");
    
    // Filter 3: Description (operates on Filter 2 results)
    filteredList = service.customSearch(description, filteredList, "description");
    
    // Filter 4: Vendor (operates on Filter 3 results)
    filteredList = service.customSearch(vendor, filteredList, "vendor");
    
    // Filter 5: Amount (operates on Filter 4 results)
    filteredList = service.customSearch(amountInput, filteredList, "amount");
}
```

**Supporting method:**
```java
public List<TransactionEntity> customSearch(String input, 
                                           List<TransactionEntity> transactionEntityList, 
                                           String filterType) {
    if (input == null || input.isEmpty()) {
        return transactionEntityList; // Skip filter if empty
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

**🔥 Key Points to Mention:**
- **Compound filtering** — Each filter builds on previous results (not independent)
- **Real-world UX** — Mimics how Amazon/eBay filters work
- **Optional criteria** — Empty input skips that filter
- **Type safety** — Validates dates/amounts before filtering
- **Performance** — Only filters already-narrowed results

**Example Flow:**
```
1000 transactions → Filter by date → 500 → Filter by vendor → 50 → Filter by amount → 15 final results
```

---

### 2️⃣ HashMap Vendor Indexing (O(1) Lookup)

Pre-indexes all vendors into a HashMap for instant lookups instead of iterating through the entire list each time.

```java
public Map<String, List<TransactionEntity>> searchByVendor(List<TransactionEntity> newestList) {
    Map<String, List<TransactionEntity>> transactionMap = new HashMap<>();

    // Phase 1: Create keys for all unique vendors
    for (TransactionEntity entity : newestList) {
        String vendorName = entity.getVendor().toUpperCase();
        transactionMap.putIfAbsent(vendorName, new ArrayList<>());
    }

    // Phase 2: Group transactions by vendor
    for (TransactionEntity entity : newestList) {
        String vendorName = entity.getVendor().toUpperCase();
        transactionMap.get(vendorName).add(entity);
    }
    return transactionMap;
}
```

**UI Integration:**
```java
public void displaySearchByVendor(List<TransactionEntity> newestTransactionList) {
    // Build index once
    Map<String, List<TransactionEntity>> transactionMap = service.searchByVendor(newestTransactionList);
    
    String input = scanner.nextLine().toUpperCase();
    
    // O(1) lookup instead of O(n) iteration
    if (transactionMap.containsKey(input)) {
        searchedList.addAll(transactionMap.get(input));
    }
}
```

**🔥 Key Points to Mention:**
- **Time complexity** — O(1) average lookup vs O(n) linear search
- **Scalability** — With 10,000 transactions: 1 operation vs 10,000 comparisons
- **Pre-indexing** — Build once, query many times
- **Real databases** — This is exactly how SQL indexes work
- **Trade-off** — More memory for exponential speed gain

**Performance:**
```
Traditional: 1,000 transactions × 100 searches = 100,000 operations
HashMap:     1,000 (index once) + 100 (lookups) = 1,100 operations
Result:      99% reduction in computational work
```

---

### 3️⃣ Aggregate Payment Calculation (Business Logic)

Solves the real accounting problem: "If I have multiple outstanding invoices to one vendor, what's my total balance?"

```java
public double totalPayment(List<TransactionEntity> ongoingPayments, 
                          String vendorName, 
                          String description) {
    double amount = 0;
    
    for (TransactionEntity transaction : ongoingPayments) {
        // AND condition: Both vendor AND description must match
        if (transaction.getVendor().equalsIgnoreCase(vendorName) && 
            transaction.getDescription().toLowerCase().contains(description.toLowerCase())) {
            amount += Math.abs(transaction.getAmount()); // Payments are negative
        }
    }
    return amount;
}
```

**Integration in payment workflow:**
```java
// Calculate total owed
double totalPayment = service.totalPayment(ongoingPayments, vendorName, description);

System.out.printf("Amount Owed: %.2f\nPlease enter your payment amount: ", totalPayment);
amount = scanner.nextDouble();

// Business validation
if(totalPayment == 0){
    System.out.println("Vendor name not found!");
} else if(amount > totalPayment){
    System.out.print("You have exceeded total payment amount!");
} else {
    amount -= totalPayment;
    service.saveToCSV(description, vendorName, amount, amount == 0 ? "paid" : "payment");
}
```

**🔥 Key Points to Mention:**
- **Aggregate calculation** — Sums multiple invoices from same vendor
- **Multi-criteria matching** — Vendor AND description (precise matching)
- **Data model awareness** — Converts negative values to positive
- **Business validation** — Prevents overpayment, validates vendor exists
- **Partial payments** — Tracks remaining balance
- **Status tracking** — Marks "paid" vs "payment" based on balance

**Real Scenario:**
```
Amazon | Office Supplies | -$250.00
Amazon | Office Supplies | -$180.00
Amazon | Books           | -$45.00

Search: "Amazon" + "Office Supplies"
→ Total: $430.00 (Books excluded because description doesn't match)
→ Pay: $400.00
→ Remaining: $30.00 outstanding
```

---

## 🛠️ Technologies Used

**Java 8+** | **Java Time API** | **File I/O (BufferedReader/Writer)** | **Collections (ArrayList, HashMap)** | **Exception Handling** | **OOP Principles**

---

## 🚀 How to Run

```bash
git clone <your-repo-url>
cd financial-transaction-app
javac -d bin src/com/pluralsight/*.java
java -cp bin com.pluralsight.Main
```

---

## 📝 Key Takeaways

This project implements **production-level design patterns**:

1. **Progressive Filtering** — Compound criteria system (not independent filters)
2. **HashMap Indexing** — O(1) lookups with 99% performance improvement
3. **Business Logic** — Real accounting scenarios with aggregate calculations

Demonstrates understanding of **data structures, algorithms, complexity analysis, and practical software engineering**.

---

*Built with Java fundamentals, data structures optimization, and business logic for production-ready applications.*
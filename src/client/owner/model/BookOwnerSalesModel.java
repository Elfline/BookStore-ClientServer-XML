/**
 Algorithm for the BookOwnerSalesReportModel class:
 1. Initialize variables to store the transaction and total revenue.
 2. Create a constructor method for an empty transaction list.
 3. Create a setter and getter method for the transactions.
 4. Create a method to get the total revenue by calculating the sum of the total amounts.
 */

package client.bookowner.model;

import utilities.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookOwnerSalesModel {
    private List<Transaction> transactions;
    client.bookowner.model.BookOwnerModel model;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Map<String, Double> revenueByMonth = new HashMap<>();


    public BookOwnerSalesModel(client.bookowner.model.BookOwnerModel model) {
        transactions = new ArrayList<>();
        this.model = model;
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            socket = new Socket("localhost", 2000);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fetchTransactions() {
        try {
            outputStream.writeObject("FETCH_SALES_REPORT");
            transactions = (List<Transaction>) inputStream.readObject();
            revenueByMonth = (Map<String, Double>) inputStream.readObject(); // Receive revenue data
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Double> getRevenueByMonth() {
        return revenueByMonth;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public double getTotalRevenue() {
        return transactions.stream().mapToDouble(Transaction::getTotalAmount).sum();
    }
}

package client.owner.model;

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
    BookOwnerModel model;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Map<String, Double> revenueByMonth = new HashMap<>();


    public BookOwnerSalesModel(BookOwnerModel model) {
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

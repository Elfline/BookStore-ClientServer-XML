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
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2000;
    private Map<String, Double> revenueByMonth = new HashMap<>();


    public BookOwnerSalesModel(client.bookowner.model.BookOwnerModel model) {
        transactions = new ArrayList<>();
        this.model = model;
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fetchTransactions() {
        try {
            if (socket == null || socket.isClosed()) {
                initializeConnection();
            }

            outputStream.writeObject("FETCH_SALES_REPORT");
            outputStream.flush();

            Object response = inputStream.readObject();
            if (response instanceof List) {
                transactions = (List<Transaction>) response;
            }

            Object revenueData = inputStream.readObject();
            if (revenueData instanceof Map) {
                revenueByMonth = (Map<String, Double>) revenueData;
            }

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

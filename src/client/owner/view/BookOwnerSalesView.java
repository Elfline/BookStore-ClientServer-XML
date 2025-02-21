
package client.owner.view;

import utilities.Transaction;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BookOwnerSalesView extends JFrame {
    private JTable salesTable;
    private JButton refreshButton;
    private JLabel totalRevenueLabel;

    public BookOwnerSalesView() {
        setTitle("Sales Report");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        salesTable = new JTable();
        refreshButton = new JButton("Refresh");
        totalRevenueLabel = new JLabel("Total Revenue: 0.00");

        add(new JScrollPane(salesTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalRevenueLabel, BorderLayout.SOUTH);
        bottomPanel.add(refreshButton, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    public JButton getRefreshButton() {
        return refreshButton;
    }

    public void showSalesData(List<Transaction> transactions, Map<String, Double> revenueByMonth) {
        String[] columnHeader = {"Date", "Transaction ID", "Book Title", "Quantity", "Price", "Total Sales"};
        DefaultTableModel tableModel = new DefaultTableModel(columnHeader, 0);

        String currentMonth = "";
        double monthTotal = 0.0;

        for (Transaction transaction : transactions) {
            String month = transaction.getDate().substring(0, 7); // Extract "MM-yyyy"

            if (!currentMonth.equals(month) && !currentMonth.isEmpty()) {
                // Add summary row for the previous month
                tableModel.addRow(new Object[]{"", "", "", "", "TOTAL REVENUE:", monthTotal});
                monthTotal = 0.0;
            }

            double totalSales = transaction.getQuantity() * transaction.getPrice();
            monthTotal += totalSales;

            tableModel.addRow(new Object[]{
                    transaction.getDate(),
                    transaction.getTransactionId(),
                    transaction.getBookTitle(),
                    transaction.getQuantity(),
                    transaction.getPrice(),
                    totalSales
            });

            currentMonth = month;
        }

        // Add final month's summary row
        if (!currentMonth.isEmpty()) {
            tableModel.addRow(new Object[]{"", "", "", "", "TOTAL REVENUE:", monthTotal});
        }

        salesTable.setModel(tableModel);

        double totalRevenue = revenueByMonth.values().stream().mapToDouble(Double::doubleValue).sum();
        totalRevenueLabel.setText("Total Revenue: " + totalRevenue);
    }
}
package client.owner.view;

import utilities.Transaction;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class BookOwnerSalesView extends JFrame {
    private JTable salesTable;
    private JButton refreshButton;
    private JLabel totalRevenueLabel;
    private JComboBox<String> monthFilter;
    private JComboBox<String> yearFilter;

    public BookOwnerSalesView() {
        setTitle("Sales Report");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        salesTable = new JTable();
        refreshButton = new JButton("Refresh");
        totalRevenueLabel = new JLabel("Total Revenue: 0.00");

        String[] months = {"All", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        String[] years = {"All", "2022", "2023", "2024", "2025"};

        monthFilter = new JComboBox<>(months);
        yearFilter = new JComboBox<>(years);

        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Month:"));
        filterPanel.add(monthFilter);
        filterPanel.add(new JLabel("Year:"));
        filterPanel.add(yearFilter);

        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(salesTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(totalRevenueLabel);
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JComboBox<String> getMonthFilter() {
        return monthFilter;
    }

    public JComboBox<String> getYearFilter() {
        return yearFilter;
    }

    public void showSalesData(List<Transaction> transactions, Map<String, Double> revenueByMonth) {
        String[] columnHeader = {"Date", "Transaction ID", "Book Title", "Quantity", "Price", "Total Sales"};
        DefaultTableModel tableModel = new DefaultTableModel(columnHeader, 0);

    }
}
package client.owner.view;

import client.owner.model.*;

import javax.swing.*;
import java.awt.*;

public class BookOwnerUpdateBookView extends JFrame {
    private JLabel titleLabel;
    private JTextField stockField, priceField;
    private JButton updateButton, cancelButton;

    public BookOwnerUpdateBookView(BookOwnerUpdateBookModel model) {
        setTitle("Update Book");
        setSize(400, 300);
        setLayout(new GridLayout(6, 2));

        add(new JLabel("Title:"));
        titleLabel = new JLabel(model.getBookTitle()); // Display book title
        add(titleLabel);

        add(new JLabel("Stock:"));
        stockField = new JTextField();
        add(stockField);

        add(new JLabel("Price:"));
        priceField = new JTextField();
        add(priceField);

        JPanel buttonPanel = new JPanel();
        updateButton = new JButton("Update Book");
        cancelButton = new JButton("Cancel");

        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);

    }

    public int getStockInput() {
        return Integer.parseInt(stockField.getText());
    }

    public double getPriceInput() {
        return Double.parseDouble(priceField.getText());
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public void setBookDetails(int stock, double price) {
        stockField.setText(String.valueOf(stock));
        priceField.setText(String.valueOf(price));
    }
    public void clearFields() {
        stockField.setText("");
        priceField.setText("");
    }
}

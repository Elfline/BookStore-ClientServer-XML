package client.owner.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class BookOwnerAddBookView extends JFrame {
    private JTextField titleField, authorField, genreField, yearField, stockField, priceField;
    private JButton addButton, cancelButton;

    public BookOwnerAddBookView() {
        setTitle("Add Book");
        setSize(400, 300);
        setLayout(new GridLayout(7, 2));

        add(new JLabel("Title:"));
        titleField = new JTextField();
        add(titleField);

        add(new JLabel("Author:"));
        authorField = new JTextField();
        add(authorField);

        add(new JLabel("Genre:"));
        genreField = new JTextField();
        add(genreField);

        add(new JLabel("Year:"));
        yearField = new JTextField();
        add(yearField);

        add(new JLabel("Stock:"));
        stockField = new JTextField();
        add(stockField);

        add(new JLabel("Price:"));
        priceField = new JTextField();
        add(priceField);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Book");
        cancelButton = new JButton("Cancel");

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

    }
    public String getTitleInput() {
        return titleField.getText();
    }
    public String getAuthorInput() {
        return authorField.getText();
    }
    public String getGenreInput() {
        return genreField.getText();
    }
    public String getYearInput() {
        return yearField.getText();
    }
    public int getStockInput() {
        return Integer.parseInt(stockField.getText());
    }
    public double getPriceInput() {
        return Double.parseDouble(priceField.getText());
    }
    public JButton getAddButton() {
        return addButton;
    }
    public JButton getCancelButton() {
        return cancelButton;
    }
}

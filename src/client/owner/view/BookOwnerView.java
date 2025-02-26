package client.bookowner.view;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class BookOwnerView extends JFrame {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, updateButton, salesButton, logOutButton;

    public BookOwnerView(String username) {
        setTitle("Book Inventory");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Title", "Author", "Stock", "Price"}, 0);
        bookTable = new JTable(tableModel);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Book");
        deleteButton = new JButton("Delete Book");
        updateButton = new JButton("Update Book");
        salesButton = new JButton("Show Sales");
        logOutButton = new JButton("Log Out");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(salesButton);
        buttonPanel.add(logOutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTable getBookTable() {
        return bookTable;
    }

    public void setAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    public void setDeleteButtonListener(ActionListener listener) {
        deleteButton.addActionListener(listener);
    }

    public void setSalesButtonListener(ActionListener listener) {
        salesButton.addActionListener(listener);
    }
    public void setLogOutButton(ActionListener listener) {
        logOutButton.addActionListener(listener);
    }
    public void setUpdateButton(ActionListener listener) { updateButton.addActionListener(listener);}
}

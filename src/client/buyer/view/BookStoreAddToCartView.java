package client.buyer.view;
import utilities.Cart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookStoreAddToCartView extends JFrame {
    private JTable tableForBooks;
    private DefaultTableModel modelForBooks;
    private DefaultListModel<String> cartModel;
    private JList<String> cartList;
    private JButton addToCartButton, removeFromCartButton, buyBooksButton;
    private JSpinner quantitySpinner;

    public BookStoreAddToCartView() {
        setTitle("Book Store - Add to Cart");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Left Panel for Available Books
        JPanel leftPanel = new JPanel(new BorderLayout());
        String[] columns = {"Title"};
        modelForBooks = new DefaultTableModel(columns, 0);
        tableForBooks = new JTable(modelForBooks);
        leftPanel.add(new JScrollPane(tableForBooks), BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        // Right Panel for Cart Items
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 400));

        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        rightPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Bottom Panel for Controls
        JPanel bottomPanel = new JPanel(new GridLayout(4, 2));
        add(bottomPanel, BorderLayout.SOUTH);

        bottomPanel.add(new JLabel("Quantity:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        bottomPanel.add(quantitySpinner);

        addToCartButton = new JButton("Add to Cart");
        removeFromCartButton = new JButton("Remove from Cart");
        buyBooksButton = new JButton("Buy Books");
        bottomPanel.add(addToCartButton);
        bottomPanel.add(removeFromCartButton);
        bottomPanel.add(buyBooksButton);
    }

    public void updateBookData(List<String> bookTitles) {
        modelForBooks.setRowCount(0);
        for (String title : bookTitles) {
            modelForBooks.addRow(new Object[]{title});
        }
    }

    public void updateCartData(List<String> cartItems) {
        cartModel.clear();
        for (String item : cartItems) {
            cartModel.addElement(item);
        }
    }

    public void showTransactionSummary(String message) {
        JOptionPane.showMessageDialog(this, message, "Purchase Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    // Getter methods for UI components
    public JButton getAddToCartButton() { return addToCartButton; }
    public JButton getRemoveFromCartButton() { return removeFromCartButton; }
    public JButton getBuyBooksButton() { return buyBooksButton; }
    public JList<String> getCartList() { return cartList; }
    public JTable getTableForBooks() { return tableForBooks; }
    public JSpinner getQuantitySpinner() { return quantitySpinner; }
}

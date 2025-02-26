package client.buyer.controller;

import client.buyer.model.BookStoreAddToCartModel;
import client.buyer.view.BookStoreAddToCartView;
import utilities.Cart;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class BookStoreAddToCartController {
    private final String CART_FILE = "res/client/buyer/cart.xml";
    private BookStoreAddToCartView view;
    private BookStoreAddToCartModel model;
    BookStoreController mainController;

    public BookStoreAddToCartController(BookStoreAddToCartView view, BookStoreAddToCartModel model, BookStoreController mainController) {
        this.view = view;
        this.model = model;
        this.mainController = mainController;

        // Initialize view with the book data
        view.updateBookData(getBookTitles(model.getBooks()));

        // Attach event listeners
        view.getAddToCartButton().addActionListener(e -> addToCart());
        view.getRemoveFromCartButton().addActionListener(e -> removeFromCart());
        view.getBuyBooksButton().addActionListener(e -> displayCheckoutConfirmation());
    }

    private List<String> getBookTitles(List<Cart> books) {
        List<String> titles = new ArrayList<>();
        for (Cart book : books) {
            titles.add(book.getTitle());
        }
        return titles;
    }

    private List<String> getCartItems(List<Cart> cart) {
        List<String> items = new ArrayList<>();
        for (Cart item : cart) {
            items.add(item.getTitle() + " x" + item.getQuantity());
        }
        return items;
    }

    private void addToCart() {
        int selectedRow = view.getTableForBooks().getSelectedRow();
        if (selectedRow != -1) {
            String bookTitle = (String) view.getTableForBooks().getValueAt(selectedRow, 0);
            int quantity = (int) view.getQuantitySpinner().getValue(); // Get selected quantity
            model.addToCart(bookTitle, quantity);
            view.updateCartData(getCartItems(model.getCart())); // Update cart display
        } else {
            JOptionPane.showMessageDialog(view, "Please select a book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeFromCart() {
        int selectedIndex = view.getCartList().getSelectedIndex();
        if (selectedIndex != -1) {
            model.removeFromCart(selectedIndex);
            view.updateCartData(getCartItems(model.getCart())); // Update cart display
        } else {
            JOptionPane.showMessageDialog(view, "Please select a book from the cart.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayCheckoutConfirmation() {
        // Check if cart is empty
        if (model.getCart().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Your cart is empty. Please add items to the cart before proceeding.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prepare the cart details to be shown in the confirmation dialog
        StringBuilder cartDetails = new StringBuilder();
        for (Cart item : model.getCart()) {
            cartDetails.append(item.getTitle()).append(" x").append(item.getQuantity()).append("\n");
        }

        // Confirm checkout dialog
        int response = JOptionPane.showConfirmDialog(view,
                "Confirm Checkout?\n\n" + cartDetails.toString(),
                "Checkout Confirmation",
                JOptionPane.YES_NO_OPTION);

        // If user confirms checkout
        if (response == JOptionPane.YES_OPTION) {
            model.saveCart(CART_FILE); // Save cart to cart.xml
            model.sendCartToServer(CART_FILE); // Send cart to server


            // Display the transaction summary with total price
            view.showTransactionSummary("Purchase successful!");

            double totalPrice = model.getTotalPrice();
            view.showTransactionSummary("Total Price of ₱" + totalPrice);

            // Update cart to reflect an empty cart
            view.updateCartData(new ArrayList<>());
        } else {
            JOptionPane.showMessageDialog(view, "Transaction Cancelled.");
        }
    }
}


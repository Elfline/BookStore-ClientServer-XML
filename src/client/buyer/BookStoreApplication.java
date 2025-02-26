package client.buyer;
import client.buyer.controller.*;
import client.buyer.model.*;
import client.buyer.view.*;

import javax.swing.*;

public class BookStoreApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize the "User" view, model, and controller
            UserModel userModel = new UserModel();
            UserView userView = new UserView();
            UserController userController = new UserController(userModel, userView);

            // Show the login window (UserView)
            userView.setVisible(true);

            // Add login listener to handle redirection after successful login
            userView.addLoginListener(e -> {
                String username = userView.getUsername();
                String password = userView.getPassword();
                String accountType = userView.getAccountType();

                // Validate login
                if (userModel.validateLogin(username, password, accountType)) {
                    userView.setStatusMessage("Login successful!");

                    // Close the login window
                    userView.setVisible(false);

                    // Redirect based on account type
                    if ("Buyer".equalsIgnoreCase(accountType)) {
                        // Initialize the "BookStore" view, model, and controller
                        BookStoreView bookStoreView = new BookStoreView(username);
                        BookStoreModel bookStoreModel = new BookStoreModel();
                        BookStoreController bookStoreController = new BookStoreController(bookStoreModel, bookStoreView, userModel);

                        // Set the "Search" button action
                        bookStoreView.addSearchButtonListener(f -> bookStoreModel.searchBooks(bookStoreView.getSearchText()));

                        // Set the "Log Out" button action to return to the login panel
                        bookStoreView.addLogOutButtonListener(e1 -> {
                            bookStoreView.setVisible(false); // Close the BookStoreView
                            userView.clearFields(); // Clear the login fields
                            userView.setStatusMessage(""); // Clear any status messages
                            userView.setVisible(true);
                        });

                        userView.setVisible(false);

                        // Initialize the "Add To Cart" view, model, and controller
                        BookStoreAddToCartView cartView = new BookStoreAddToCartView();
                        BookStoreAddToCartModel cartModel = new BookStoreAddToCartModel();
                        new BookStoreAddToCartController(cartView, cartModel, bookStoreController);

                        // Set the "Add To Cart" button action
                        bookStoreView.addAddToCartListener(f -> cartView.setVisible(true));

                        // Initialize the "Show All Books" view, model, and controller
                        BookStoreShowBookView showBookView = new BookStoreShowBookView();
                        BookStoreShowBookModel showBookModel = new BookStoreShowBookModel();
                        new BookStoreShowBookController(showBookView, showBookModel, bookStoreController);

                        // Set the "Show All Books" button action
                        bookStoreView.addShowAllBooksListener(f -> showBookView.setVisible(true));


                        // Show the BookStoreView
                        bookStoreView.setVisible(true);
                    }
                } else {
                    userView.setStatusMessage("Invalid username, password, or account type.");
                }
            });
        });
    }
}
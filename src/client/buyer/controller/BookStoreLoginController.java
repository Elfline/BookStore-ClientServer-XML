package client.buyer.controller;

import client.buyer.model.BookStoreLoginModel;
import client.buyer.view.BookStoreLoginView;
import utilities.User;
import server.ServerConnection;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class BookStoreLoginController {
    private BookStoreLoginModel model;
    private BookStoreLoginView view;

    public BookStoreLoginController(BookStoreLoginModel model, BookStoreLoginView view) {
        this.model = model;
        this.view = view;

        view.addCreateAccountListener(new CreateAccountListener());
        view.addLoginListener(new LoginListener());

        // Sync users when the controller starts (not in the model)
        syncUsersFromServer();
    }

    private void syncUsersFromServer() {
        List<User> serverUsers = ServerConnection.fetchUsers();
        for (User user : serverUsers) {
            if (!model.userExists(user.getUsername())) { // Check if user exists
                model.addUserLocally(user); // Only add locally
            }
        }
        System.out.println("[UserController] Synced users from the server.");
    }

    // Create Account Logic
    private class CreateAccountListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();
            String accountType = view.getAccountType();

            // Simple validation
            if (username.isEmpty() || password.isEmpty()) {
                view.setStatusMessage("Username and password cannot be empty!");
                return;
            }

            if (model.userExists(username)) {
                view.setStatusMessage("Username already exists.");
                return;
            }

            // Create a new user
            User newUser = new User(username, password, accountType);
            boolean serverResponse = ServerConnection.saveUser(newUser);

            if (serverResponse) {
                model.addUserLocally(newUser); // Save only if the server confirms
                view.setStatusMessage("Account created successfully!");

                // Ask for confirmation before navigating
                int choice = JOptionPane.showConfirmDialog(
                        view,
                        "Your account has been created successfully.\nLog in to your account",
                        "Confirm Navigation",
                        JOptionPane.YES_NO_OPTION
                );

            } else {
                view.setStatusMessage("Failed to save account. Try again later.");
            }
        }
    }

    // Login Logic
    private class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();
            String accountType = view.getAccountType(); // User selects Buyer

            if (model.validateLogin(username, password, accountType)) {
                User.setLoggedInUsername(username);

                view.setStatusMessage("Login successful!");
                navigateToDashboard(accountType, username);

                view.dispose(); // Close login window after success
            } else {
                view.setStatusMessage("Invalid username, password, or account type.");
            }
        }
    }

    public void navigateToDashboard(String accountType, String username) {
        if ("Buyer".equalsIgnoreCase(accountType)) {
            view.setStatusMessage("Redirecting to Book Store");
        } else {
            view.setStatusMessage("Invalid account tye");
        }
    }
}

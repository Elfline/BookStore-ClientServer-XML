package client.owner.controller;

import client.owner.model.BookOwnerLoginModel;
import client.owner.view.BookOwnerLoginView;
import server.ServerConnection;
import utilities.User;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class BookOwnerLoginController {
    private BookOwnerLoginModel model;
    private BookOwnerLoginView view;

    public BookOwnerLoginController(BookOwnerLoginModel model, BookOwnerLoginView view){
        this.model = model;
        this.view = view;

        view.addLoginListener(new LoginListener());

        //Sync Book owner when the controller starts
        syncBookOwnerFromServer();
    }

    private void syncBookOwnerFromServer() {
        List<User> serverBookOwner = ServerConnection.fetchUsers();

        if (serverBookOwner != null) { // Check for null before iterating
            for (UserUtility bookowner : serverBookOwner) {
                if (!model.bookOwnerExists(bookowner.getUsername())) {
                    model.addBookOwner(bookowner); // Add to the model (you'll need to implement this)
                    System.out.println("[BookOwnerLoginController] Book owner synced from server: " + bookowner.getUsername());
                }
            }
        }
    }


    //Login logic
    private class LoginListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();
            String accountType = view.getAccountType(); // User selects Buyer or Book Owner

            if (model.validateLogin(username, password, accountType)) {
                UserUtility.setLoggedInUsername(username);

                view.setStatusMessage("Login successful!");
                navigateToDashboard(accountType, username);

                view.dispose(); // Close login window after success
            } else {
                view.setStatusMessage("Invalid username, password, or account type.");
            }
        }
    }

    public void navigateToDashboard(String accountType, String username) {
        if ("Book Owner".equalsIgnoreCase(accountType)) {
            view.setStatusMessage("Redirecting to Book Owner");
        }
    }
}

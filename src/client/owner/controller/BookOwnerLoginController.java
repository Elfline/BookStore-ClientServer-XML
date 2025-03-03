package client.owner.controller;

import client.owner.model.BookOwnerLoginModel;
import client.owner.view.BookOwnerLoginView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookOwnerLoginController {
    BookOwnerLoginModel model;
    BookOwnerLoginView view;

    public BookOwnerLoginController(BookOwnerLoginModel model, BookOwnerLoginView view){
        this.model = model;
        this.view = view;

        view.addLoginListener(new LoginListener());

    }


    private class LoginListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();
            String accountType = view.getAccountType();

            if (username.isEmpty() || password.isEmpty()) {
                view.setStatusMessage("Please fill in all fields!");
                return;
            }

            model.createLoginXml(username, password, accountType);

            if (model.validateLogin()) {
                view.setStatusMessage("Login Successful!");
                System.out.println("[ADMIN] Login Successful");
                navigateToDashboard();
            } else {
                view.setStatusMessage("Invalid username or password!");
                System.err.println("[ERROR] Invalid username or password");
            }

        }
    }

    public void navigateToDashboard() {
        view.setStatusMessage("Redirecting to the dashboard");
        System.out.println("[ADMIN] Redirecting to the dashboard");
    }

}

package client.owner;
import client.owner.controller.*;
import client.owner.model.*;
import client.owner.view.*;

import javax.swing.*;

public class BookOwnerApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->{
            //initialize the BookOwner view, model, and controller
            client.bookowner.model.BookOwnerLoginModel model = new client.bookowner.model.BookOwnerLoginModel();
            client.bookowner.view.BookOwnerLoginView view = new client.bookowner.view.BookOwnerLoginView();
            client.bookowner.controller.BookOwnerLoginController controller = new client.bookowner.controller.BookOwnerLoginController(model, view);

            //show the login window
            view.setVisible(true);

            //add login listener
            view.addLoginListener(e ->{
                String username = view.getUsername();
                String password = view.getPassword();
                String accountType = view.getAccountType();

                //validate login
                if (model.validateLogin(username, password, accountType)){
                    view.setStatusMessage("Login Successful!");

                    //close the login window
                    view.setVisible(false);

                    //redirect based on account type
                    if("Book Owner".equalsIgnoreCase(accountType)) {
                        // Initialize the "BookOwner" view, model, and controller
                        client.bookowner.model.BookOwnerModel bookOwnerModel = new client.bookowner.model.BookOwnerModel();
                        client.bookowner.view.BookOwnerView bookOwnerView = new client.bookowner.view.BookOwnerView(username);
                        client.bookowner.controller.BookOwnerController bookOwnerController = new client.bookowner.controller.BookOwnerController(bookOwnerView, bookOwnerModel, model);

                        // Set the "Log Out" button action to return to the login panel
                        bookOwnerView.setLogOutButton(e1 -> {
                            bookOwnerView.setVisible(false); // Close the BookOwnerView
                            view.clearFields(); // Clear the login fields
                            view.setStatusMessage(""); // Clear any status messages
                            view.setVisible(true);
                        });


                        view.setVisible(false);
                        // Initialize the "Add Book" view, model, and controller
                        client.bookowner.view.BookOwnerAddBookView addBookView = new client.bookowner.view.BookOwnerAddBookView();
                        client.bookowner.model.BookOwnerAddBookModel addBookModel = new client.bookowner.model.BookOwnerAddBookModel(bookOwnerModel);
                        new client.bookowner.controller.BookOwnerAddBookController(addBookView, addBookModel, bookOwnerController);

                        // Set the "Add Book" button action
                        bookOwnerView.setAddButtonListener(f -> addBookView.setVisible(true));

                        // Initialize the "Sales Report" view, model, and controller
                        BookOwnerSalesReportView salesReportView = new BookOwnerSalesReportView();
                        BookOwnerSalesReportModel salesReportModel = new BookOwnerSalesReportModel(bookOwnerModel);
                        new BookOwnerSalesReportController(salesReportView, salesReportModel, bookOwnerController);

                        // Set the "Show Sales" button action
                        bookOwnerView.setSalesButtonListener(f -> salesReportView.setVisible(true));

                        // Show the BookOwnerView
                        bookOwnerView.setVisible(true);

                        bookOwnerView.setDeleteButtonListener(f -> {
                            if (bookOwnerModel.getSelectedBook() == null) {
                                JOptionPane.showMessageDialog(null, "No book selected!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            String selectedBookTitle = bookOwnerModel.getSelectedBook().getTitle();
                            client.bookowner.view.BookOwnerDeleteBookView deleteBookView = new client.bookowner.view.BookOwnerDeleteBookView(selectedBookTitle);
                            client.bookowner.model.BookOwnerDeleteBookModel deleteBookModel = new client.bookowner.model.BookOwnerDeleteBookModel(bookOwnerModel);
                            new client.bookowner.controller.BookOwnerDeleteBookController(deleteBookView, deleteBookModel, bookOwnerController);

                            deleteBookView.setVisible(true);
                        });


                        bookOwnerView.setUpdateButton(f -> {
                            if (bookOwnerModel.getSelectedBook() == null) {
                                JOptionPane.showMessageDialog(null, "No book selected!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            client.bookowner.model.BookOwnerUpdateBookModel updateBookModel = new client.bookowner.model.BookOwnerUpdateBookModel(bookOwnerModel);
                            client.bookowner.view.BookOwnerUpdateBookView updateBookView = new client.bookowner.view.BookOwnerUpdateBookView(updateBookModel);
                            new client.bookowner.controller.BookOwnerUpdateBookController(updateBookView, updateBookModel, bookOwnerController);

                            updateBookView.setVisible(true);
                        });
                    }
                } else {
                    view.setStatusMessage("Invalid username, password, or account type.");
                }

            });

        });
    }
}

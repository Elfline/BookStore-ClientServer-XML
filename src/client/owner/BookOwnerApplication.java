import client.owner.controller.*;
import client.owner.model.*;
import client.owner.view.*;

import javax.swing.*;

public class BookOwnerApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->{
            //initialize the BookOwner view, model, and controller
            BookOwnerLoginModel model = new BookOwnerLoginModel();
            BookOwnerLoginView view = new BookOwnerLoginView();
            BookOwnerLoginController controller = new BookOwnerLoginController(model, view);

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
                        BookOwnerModel bookOwnerModel = new BookOwnerModel();
                        BookOwnerView bookOwnerView = new BookOwnerView(username);
                        BookOwnerController bookOwnerController = new BookOwnerController(bookOwnerView, bookOwnerModel, model);

                        // Set the "Log Out" button action to return to the login panel
                        bookOwnerView.setLogOutButton(e1 -> {
                            bookOwnerView.setVisible(false); // Close the BookOwnerView
                            view.clearFields(); // Clear the login fields
                            view.setStatusMessage(""); // Clear any status messages
                            view.setVisible(true);
                        });


                        view.setVisible(false);
                        // Initialize the "Add Book" view, model, and controller
                        BookOwnerAddBookView addBookView = new BookOwnerAddBookView();
                        BookOwnerAddBookModel addBookModel = new BookOwnerAddBookModel(bookOwnerModel);
                        new BookOwnerAddBookController(addBookView, addBookModel, bookOwnerController);

                        // Set the "Add Book" button action
                        bookOwnerView.setAddButtonListener(f -> addBookView.setVisible(true));

                        // Initialize the "Sales Report" view, model, and controller
                        BookOwnerSalesView salesReportView = new BookOwnerSalesReportView();
                        BookOwnerSalesModel salesReportModel = new BookOwnerSalesReportModel(bookOwnerModel);
                        new BookOwnerSalesController(salesReportView, salesReportModel, bookOwnerController);

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
                            BookOwnerDeleteBookView deleteBookView = new BookOwnerDeleteBookView(selectedBookTitle);
                            BookOwnerDeleteBookModel deleteBookModel = new BookOwnerDeleteBookModel(bookOwnerModel);
                            new BookOwnerDeleteBookController(deleteBookView, deleteBookModel, bookOwnerController);

                            deleteBookView.setVisible(true);
                        });


                        bookOwnerView.setUpdateButton(f -> {
                            if (bookOwnerModel.getSelectedBook() == null) {
                                JOptionPane.showMessageDialog(null, "No book selected!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            BookOwnerUpdateBookModel updateBookModel = new BookOwnerUpdateBookModel(bookOwnerModel);
                            BookOwnerUpdateBookView updateBookView = new BookOwnerUpdateBookView(updateBookModel);
                            new BookOwnerUpdateBookController(updateBookView, updateBookModel, bookOwnerController);

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

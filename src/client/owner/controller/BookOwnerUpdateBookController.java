package client.owner.controller;

import client.owner.model.BookOwnerUpdateBookModel;
import client.owner.view.BookOwnerUpdateBookView;

import javax.swing.*;

public class BookOwnerUpdateBookController {
    BookOwnerUpdateBookView view;
    BookOwnerUpdateBookModel model;
    BookOwnerController mainController;

    public BookOwnerUpdateBookController(BookOwnerUpdateBookView view, BookOwnerUpdateBookModel model, client.bookowner.controller.BookOwnerController mainController) {
        this.view = view;
        this.model = model;
        this.mainController = mainController;

        // Ensure a book is selected
        if (model.getBookTitle().equals("No Book Selected")) {
            JOptionPane.showMessageDialog(null, "No book selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Set initial book details in the view
        view.setBookDetails(model.getBookStock(), model.getBookPrice());

        // Update button logic
        this.view.getUpdateButton().addActionListener(e -> updateBook());

        // Cancel button logic: reset fields instead of just closing
        this.view.getCancelButton().addActionListener(e -> view.clearFields());
    }

    private void updateBook() {
        try {
            int stock = view.getStockInput();
            double price = view.getPriceInput();

            // Send book data to model
            boolean success = model.updateBook(stock, price);

            if (success) {
                JOptionPane.showMessageDialog(view, "Book updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                mainController.updateBookTable(mainController.getView().getTableModel());
                view.dispose();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update book. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Invalid input. Please check all fields.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package client.owner.controller;

import client.owner.model.*;
import client.owner.view.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookOwnerAddBookController {
     BookOwnerAddBookView view;
     BookOwnerAddBookModel model;
     BookOwnerController mainController;

    public BookOwnerAddBookController(BookOwnerAddBookView view, BookOwnerAddBookModel model, BookOwnerController mainController) {
        this.view = view;
        this.model = model;
        this.mainController = mainController;

        view.setBookDetails(model.getBookStock(), model.getBookPrice());
        this.view.getAddButton().addActionListener(new AddBookListener());
        this.view.getCancelButton().addActionListener(e -> view.dispose());
    }

    class AddBookListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String title = view.getTitleInput();
                String author = view.getAuthorInput();
                String genre = view.getGenreInput();
                String year = view.getYearInput();
                int stock = view.getStockInput();
                double price = view.getPriceInput();

                // Validate input
                if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || year.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "All fields must be filled out.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Send book data to server
                boolean success = model.addBook(title, author, genre, year, stock, price);

                if (success) {
                    JOptionPane.showMessageDialog(view, "Book added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    mainController.updateBookTable(mainController.getView().getTableModel());
                    view.dispose();
                } else {
                    JOptionPane.showMessageDialog(view, "Failed to add book. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Invalid input. Please check all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
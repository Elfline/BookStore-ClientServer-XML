package client.owner.controller;

import client.owner.model.BookOwnerDeleteBookModel;
import client.owner.view.BookOwnerDeleteBookView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookOwnerDeleteBookController {
    BookOwnerDeleteBookView view;
    BookOwnerDeleteBookModel model;
    BookOwnerController mainController;

    public BookOwnerDeleteBookController (BookOwnerDeleteBookView view, BookOwnerDeleteBookModel model, client.bookowner.controller.BookOwnerController mainController) {
        this.model = model;
        this.view = view;
        this.mainController = mainController;


        this.view.getYesButton().addActionListener(new YesButtonListener());
        this.view.getNoButton().addActionListener(new NoButtonListener());
    }

    private class YesButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean deleted = model.deleteBook();
            if (deleted) {
                JOptionPane.showMessageDialog(view, "Book deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete the book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            view.dispose();
        }
    }

    private class NoButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.dispose();
        }
    }
}

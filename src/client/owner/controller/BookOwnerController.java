package client.owner.controller;

import client.owner.model.*;
import client.owner.view.*;
import client.buyer.model.BookOwnerLoginModel;
import utilities.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class BookOwnerController {
    private BookOwnerView view;
    private List<Book> books = new ArrayList<>();
    BookOwnerModel model;
    BookOwnerLoginModel bookOwnerLoginModel;

    public BookOwnerController(BookOwnerView view, BookOwnerModel model, BookOwnerLoginModel bookOwnerLoginModel) {
        this.model = model;
        this.view = view;
        this.bookOwnerLoginModel = bookOwnerLoginModel;

        // Initialize books from model
        this.books = model.getBooks(); // Ensure books is initialized

        // Set listeners
        this.view.setSalesButtonListener(this::handleSales);
        this.view.setAddButtonListener(this::handleAddBook);
        this.view.setDeleteButtonListener(this::handleDeleteBook);
        this.view.setUpdateButton(this:: handleUpdateBook);
        this.view.setLogOutButton(this:: handleLogOut);

        // Track row selection
        view.getBookTable().getSelectionModel().addListSelectionListener(e -> handleRowSelection());

        // Update the book table
        updateBookTable(view.getTableModel());
    }

    public BookOwnerView getView() {
        return this.view;
    }

    public void setView(BookOwnerView view) {
        this.view = view;
    }
    public BookUtility getSelectedBook() {
        return model.getSelectedBook();
    }


    public void updateBookTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Clear the table
        System.out.println("Updating book table with " + books.size() + " books.");
        for (BookUtility book : books) {
            tableModel.addRow(new Object[]{book.getTitle(), book.getAuthor(), book.getStock(), book.getPrice()});
            view.getBookTable();
        }
    }

    private void handleRowSelection() {
        int selectedRow = view.getBookTable().getSelectedRow();
        if (selectedRow >= 0) {
            BookUtility selectedBook = books.get(selectedRow);
            model.setSelectedBook(selectedBook);
            System.out.println("[DEBUG] Selected book: " + selectedBook.getTitle());
        }
    }

    public void handleUpdateBook(ActionEvent e) {
        BookUtility selectedBook = getSelectedBook();
        if (selectedBook == null) {
            JOptionPane.showMessageDialog(view, "Please select a book to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookOwnerUpdateBookModel updateBookModel = new BookOwnerUpdateBookModel(model);
        BookOwnerUpdateBookView updateBookView = new BookOwnerUpdateBookView(updateBookModel);
        new BookOwnerUpdateBookController(updateBookView, updateBookModel, this);
    }

    public void handleAddBook(ActionEvent e) {
        BookOwnerAddBookView addBookView = new BookOwnerAddBookView();
        BookOwnerAddBookModel addBookModel = new BookOwnerAddBookModel(model);
        new client.bookowner.controller.BookOwnerAddBookController(addBookView, addBookModel, this);

        updateBookTable(view.getTableModel());
    }
    public void handleSales(ActionEvent e) {
        BookOwnerSalesReportView salesReportView = new BookOwnerSalesReportView();
        BookOwnerSalesReportModel salesReportModel = new BookOwnerSalesReportModel(model);
        BookOwnerSalesReportController salesReportController = new BookOwnerSalesReportController(salesReportView, salesReportModel, this);

        // Refresh sales data before displaying
        salesReportController.refresh();
    }
    public void handleLogOut(ActionEvent e) {
        view.dispose(); // Close the current book owner view
        UserView userView = new UserView();
        UserModel userModel = new UserModel();
        new UserController(userModel, userView);

        userView.setVisible(true);
    }

    public void handleDeleteBook(ActionEvent e) {
        if (model.getSelectedBook() == null) {
            JOptionPane.showMessageDialog(view, "Please select a book to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedBookTitle = model.getSelectedBook().getTitle();
        BookOwnerDeleteBookView deleteBookView = new BookOwnerDeleteBookView(selectedBookTitle); // 🔹 Pass title here
        client.bookowner.model.BookOwnerDeleteBookModel deleteBookModel = new client.bookowner.model.BookOwnerDeleteBookModel(model);
        new BookOwnerDeleteBookController(deleteBookView, deleteBookModel, this);

        deleteBookView.setVisible(true);
    }


}
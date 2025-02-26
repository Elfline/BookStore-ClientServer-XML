package client.buyer.controller;

import client.buyer.model.*;
import client.buyer.view.*;
import utilities.Book;
import utilities.User;

import java.awt.event.*;
import java.util.List;

public class BookStoreController {
    private BookStoreModel model;
    private BookStoreView view;
    BookStoreLoginModel loginModel;
    private static String LOGGED_IN_USER = User.getLoggedInUsername();


    public BookStoreController(BookStoreModel model, BookStoreView view, BookStoreLoginModel loginModel) {
        this.model = model;
        this.view = view;
        this.loginModel = loginModel;

        // Adding listeners to the View
        this.view.addSearchButtonListener(new SearchButtonListener());

        this.view.addLogOutButtonListener(this:: handleLogOutButton);
        this.view.addAddToCartListener(this::handleAddToCartButton);
        this.view.addShowAllBooksListener(this::handleShowAllBooksButton);

        // Attach selection listener once

    }

    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.loadBooksFromXML();
            String searchTerm = view.getSearchText().toLowerCase().trim();
            List<Book> searchResults = model.searchBooks(searchTerm);

            view.clearBookList(); // Clear previous results
            if (searchResults.isEmpty()) {
                view.addBookToList("No matching books found.");
            } else {
                // Add search results to the book list in the view
                for (Book book : searchResults) {
                    view.addBookToList(book.getTitle() + " by " + book.getAuthor() + " - ₱" + book.getPrice() + " \n Stocks: " + book.getStock());
                }
            }
        }
    }

    public void handleAddToCartButton (ActionEvent e) {
        BookStoreAddToCartView addToCartView = new BookStoreAddToCartView();
        BookStoreAddToCartModel addToCartModel = new BookStoreAddToCartModel();
        new BookStoreAddToCartController(addToCartView, addToCartModel, this);
    }

    public void handleLogOutButton (ActionEvent e) {
        view.dispose(); // Close the current book owner view
        BookStoreLoginView loginView = new BookStoreLoginView();
        BookStoreLoginModel loginModel = new BookStoreLoginModel();
        new BookStoreLoginController(loginModel, loginView);

        loginView.setVisible(true);
    }
}

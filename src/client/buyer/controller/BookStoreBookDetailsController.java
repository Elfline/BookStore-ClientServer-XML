/*
 * BookStoreShowBookController manages book list selection and add to favorites
 *
 * Algorithm:
 * 1. Initialize the controller with the view and model.
 * 2. Set up event listeners for user interactions.
 * 3. Load book data from the model and update the view.
 * 4. Handle book selection:
 *    - Retrieve book details based on user selection.
 *    - Display book information in the view.
 * 5. Handle adding a book to favorites:
 *    - Retrieve the selected book title.
 *    - Find the corresponding book in the model.
 *    - Add the book to the favorites list.
 *    - Show a confirmation message to the user.
 */
package client.buyer.controller;

import utilities.Book;
import client.buyer.model.BookStoreBookDetailsModel;
import client.buyer.view.BookStoreBookDetailsView;

import javax.swing.*;

public class BookStoreBookDetailsController {
    private BookStoreBookDetailsView view;
    private BookStoreBookDetailsModel model;
    private BookStoreController mainController;

    public BookStoreBookDetailsController(BookStoreBookDetailsView view, BookStoreBookDetailsModel model, BookStoreController mainController) {
        this.view = view;
        this.model = model;
        this.mainController = mainController;

        // Load book data
        view.setBookData(model.getBooks());
        // Set the controller in the view
        view.setController(this);
    }


    public void handleBookSelection(String selectedTitle) {
        Book selectedBook = model.getBookByTitle(selectedTitle);
        if (selectedBook != null) {
            view.displayBookInfo(selectedBook);
        } else {
            JOptionPane.showMessageDialog(view, "Book information not found");
        }
    }
}

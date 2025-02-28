/*
 * Loads book data from the xml file and adds selected books to the favorites.xml
 *
 * Algorithm:
 * 1. Initialize the model:
 *    - Load book data from "res/books.xml" into a list.
 *    - Initialize an empty list for favorites.
 * 2. Retrieve book data:
 *    - Provide access to the list of books.
 *    - Search for a book by title and return its details.
 * 3. Handle adding books to favorites:
 *    - Retrieve the selected book from the book list.
 *    - Add it to the favorites list.
 *    - Save the updated favorites list to "res/favorites.xml".
 */
package client.buyer.model;

import utilities.Book;
import server.ServerXml;
import utilities.User;
import java.util.List;


public class BookStoreBookDetailsModel {
    private final String BOOKS_FILE = "res/client/buyer/books.xml";
    private List<Book> books;
    private List<Book> favorites;
    private static String LOGGED_IN_USER = User.getLoggedInUsername();

    public BookStoreBookDetailsModel() {
        this.books = ServerXml.readBooksFromXML(BOOKS_FILE);
    }


    public List<Book> getBooks() {
        return books;
    }

    public Book getBookByTitle(String title) {
        for (Book book : books) {
            if (book.getTitle().equals(title)) {
                return book;
            }
        }
        return null;
    }

    public void addToFavorites(Book book) {
        if (!favorites.contains(book)) {
            favorites.add(book);
        }
    }

}
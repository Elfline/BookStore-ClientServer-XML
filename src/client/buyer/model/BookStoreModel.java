/**
 * BookStoreModel represents the data model for the book store application.
 * It manages the list of available books and provides methods to access and manipulate the book data.
 *
 * Algorithm:
 * 1. Load Books:
 *    - Read book data from the "books.xml" file.
 *    - Parse the XML file to extract book information (title, author, genre, year, stock, price).
 *    - Create BookUtility objects for each book and store them in the `bookInventory` list.
 * 2. Get Book Inventory:
 *    - Return a copy of the `bookInventory` list to prevent external modifications.
 * 3. Search Books:
 *    - Create an empty list to store search results.
 *    - Iterate through the `bookInventory`.
 *    - Check if the search term (case-insensitive) is present in the book's title, author, or genre.
 *    - If a match is found, add the book to the results list.
 *    - Return the list of matching books.
 */
package client.buyer.model;

import utilities.BookUtility;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BookStoreModel {
    private List<BookUtility> bookInventory = new ArrayList<>();


    // Load Books from XML
    public void loadBooksFromXML() {
        bookInventory.clear();
        try {
            File file = new File("res/books.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("book");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String title = element.getElementsByTagName("Title").item(0).getTextContent();
                    String author = element.getElementsByTagName("Author").item(0).getTextContent();
                    String genre = element.getElementsByTagName("Genre").item(0).getTextContent();
                    String year = element.getElementsByTagName("Year").item(0).getTextContent();
                    int stock = Integer.parseInt(element.getElementsByTagName("Stock").item(0).getTextContent());
                    double price = Double.parseDouble(element.getElementsByTagName("Price").item(0).getTextContent());

                    bookInventory.add(new BookUtility(title, author, genre, stock, year, price));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BookUtility> getBookInventory() {
        return bookInventory;
    }

    public List<BookUtility> searchBooks(String searchTerm) {
        List<BookUtility> results = new ArrayList<>();
        for (BookUtility book : bookInventory) {
            if (book.getTitle().toLowerCase().contains(searchTerm) ||
                    book.getAuthor().toLowerCase().contains(searchTerm) ||
                    book.getGenre().toLowerCase().contains(searchTerm)) {
                results.add(book);
            }
        }
        return results;
    }

}
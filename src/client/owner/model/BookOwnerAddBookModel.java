

package client.bookowner.model;

import utilities.BookUtility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BookOwnerAddBookModel {

    BookOwnerModel model;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket;

    public BookOwnerAddBookModel(BookOwnerModel model) {
        this.model = model;
        initializeConnection();
    }
    private void initializeConnection() {
        try {
            socket = new Socket("localhost", 2000);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** Method for adding book */
    public boolean addBook(String title, String author, String genre, String year, int stock, double price) {
        try {
            // Create a new book object
            BookUtility newBook = new BookUtility(title, author, genre, stock, year, price);

            // Send request to server
            outputStream.writeObject("ADD_BOOK");
            outputStream.writeObject(newBook);
            outputStream.flush();

            // Receive server confirmation
            String response = (String) inputStream.readObject();
            return "SUCCESS".equals(response);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

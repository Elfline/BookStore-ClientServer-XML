package client.bookowner.model;

import utilities.BookUtility;
import utilities.XMLUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BookOwnerUpdateBookModel {
    private client.bookowner.model.BookOwnerModel model;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket;
    private BookUtility bookToUpdate;
    private List<BookUtility> books = new ArrayList<>();

    public BookOwnerUpdateBookModel(client.bookowner.model.BookOwnerModel model) {
        this.model = model;
        this.bookToUpdate = model.getSelectedBook(); // Get selected book from BookOwnerModel
        if (bookToUpdate != null) {
            System.out.println("Selected Book: " + bookToUpdate.getTitle());
            initializeConnection();
        } else {
            System.out.println("ERROR: No Book selected");
        }
    }

    public String getBookTitle() {
        return (bookToUpdate != null) ? bookToUpdate.getTitle() : "No Book Selected";
    }

    public int getBookStock() {
        return (bookToUpdate != null) ? bookToUpdate.getStock() : 0;
    }

    public double getBookPrice() {
        return (bookToUpdate != null) ? bookToUpdate.getPrice() : 0.0;
    }

    public boolean updateBook(int stock, double price) {
        if (bookToUpdate == null) {
            System.out.println("No book selected for update.");
            return false;
        }

        try {
            bookToUpdate.setStock(stock);
            bookToUpdate.setPrice(price);

            if (outputStream != null) {
                outputStream.writeObject("UPDATE_BOOK");
                outputStream.writeObject(bookToUpdate);
                outputStream.flush();

                String response = (String) inputStream.readObject();
                return "SUCCESS".equals(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
}

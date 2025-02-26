package client.bookowner.model;

import utilities.BookUtility;
import utilities.XMLUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BookOwnerModel {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2000;
    private List<BookUtility> books;
    private BookUtility selectedBook;
    private Runnable updateCallback; // This will notify the GUI when books are updated
    private static final String BOOKS_FILE = "res/books.xml";

    public BookOwnerModel() {
        System.out.println("[CLIENT] Requesting the books data from server...");
        this.books = XMLUtils.readBooksFromXML(BOOKS_FILE);
        requestBooksFromServer();
    }
    public void setUpdateCallback(Runnable callback) {
        this.updateCallback = callback;
    }

    /** Requests books.xml from the server */
    public void requestBooksFromServer() {
        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                 ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

                output.writeObject("REQUEST_BOOKS");
                output.flush();

                while (true) {
                    String command = (String) input.readObject();
                    System.out.println("[DEBUG] Received command: " + command);  // Debugging

                    if ("UPDATE_BOOKS".equals(command)) {
                        String xmlData = (String) input.readObject();
                        System.out.println("[DEBUG] Received books.xml data: " + xmlData); // Debugging
                        processReceivedBooks(xmlData);
                        break; // Exit after processing
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[CLIENT] Error requesting books: " + e.getMessage());
            }
        }).start();
    }

    /** Processes received books.xml data */
    private void processReceivedBooks(String xmlData) {
        if (xmlData == null || xmlData.isEmpty()) {
            System.err.println("[ERROR] Received empty books.xml!");
            return;
        }

        saveBooksFile(xmlData); // Save received books.xml

        // Reload books from saved file
        XMLUtils.readBooksFromXML(BOOKS_FILE);

        if (updateCallback != null) {
            updateCallback.run(); // Notify UI to refresh
        }
    }
    /** Saves received books.xml content locally */
    private void saveBooksFile(String xmlData) {
        File file = new File(BOOKS_FILE);

        // Ensure the directory exists before writing
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(xmlData);
            writer.flush();
            System.out.println("[CLIENT] Books file saved locally: " + BOOKS_FILE);
        } catch (IOException e) {
            System.err.println("[CLIENT] Error saving books.xml: " + e.getMessage());
        }
    }

    public List<BookUtility> getBooks() {
        return books;
    }

    public BookUtility getSelectedBook() {
        return selectedBook;
    }

    public void setSelectedBook(BookUtility book) {
        this.selectedBook = book;
    }
}
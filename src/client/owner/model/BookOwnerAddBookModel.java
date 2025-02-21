

package client.owner.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import utilities.Book;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.net.Socket;


public class BookOwnerAddBookModel {
    private static final String RECORDS_FILE = "res/records.xml";
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
    /** Adds book details to records.xml and sends the file to the server */
    public boolean addBook(String title, String author, String genre, String year, int stock, double price) {
        try {
            Book newBook = new Book(title, author, genre, stock, year, price);

            // Save the book details to records.xml
            saveAddToRecords(newBook);

            // Send records.xml to the server
            if (outputStream != null) {
                sendRecordsFile();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Saves new book details to records.xml using FileOutputStream */
    private void saveAddToRecords(Book newBook) {
        try {
            File file = new File(RECORDS_FILE);
            Document document;
            Element rootElement;

            if (file.exists()) {
                // Load existing XML file
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.parse(file);
                rootElement = document.getDocumentElement();
            } else {
                // Create a new XML file if it doesn't exist
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.newDocument();
                rootElement = document.createElement("records");
                document.appendChild(rootElement);
            }

            Element recordElement = document.createElement("record");

            Element statusElement = document.createElement("status");
            statusElement.appendChild(document.createTextNode("ADD"));
            recordElement.appendChild(statusElement);

            Element bookElement = document.createElement("book");

            Element titleElement = document.createElement("Title");
            titleElement.appendChild(document.createTextNode(newBook.getTitle()));
            bookElement.appendChild(titleElement);

            Element authorElement = document.createElement("Author");
            authorElement.appendChild(document.createTextNode(newBook.getAuthor()));
            bookElement.appendChild(authorElement);

            Element genreElement = document.createElement("Genre");
            genreElement.appendChild(document.createTextNode(newBook.getGenre()));
            bookElement.appendChild(genreElement);

            Element stockElement = document.createElement("Stock");
            stockElement.appendChild(document.createTextNode(String.valueOf(newBook.getStock())));
            bookElement.appendChild(stockElement);

            Element yearElement = document.createElement("Year");
            yearElement.appendChild(document.createTextNode(newBook.getYear()));
            bookElement.appendChild(yearElement);

            Element priceElement = document.createElement("Price");
            priceElement.appendChild(document.createTextNode(String.valueOf(newBook.getPrice())));
            bookElement.appendChild(priceElement);

            recordElement.appendChild(bookElement);

            rootElement.appendChild(recordElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                StreamResult result = new StreamResult(fos);
                transformer.transform(source, result);
            }

            System.out.println("[CLIENT] Book added to records.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Sends records.xml to the server */
    private void sendRecordsFile() {
        try {
            File file = new File(RECORDS_FILE);
            if (!file.exists()) {
                System.out.println("[CLIENT] records.xml does not exist.");
                return;
            }

            // Read file bytes using FileInputStream
            byte[] fileBytes = new byte[(int) file.length()];
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                fileInputStream.read(fileBytes);
            }

            // Send file to server
            outputStream.writeObject("SEND_RECORDS_FILE");
            outputStream.writeObject(fileBytes);
            outputStream.flush();

            System.out.println("[CLIENT] Sent records.xml to server.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[CLIENT] Error sending records.xml");
        }
    }
}

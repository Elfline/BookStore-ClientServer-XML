package client.owner.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import utilities.Book;
import utilities.XMLUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BookOwnerUpdateBookModel {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2000;
    BookOwnerModel model;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket;
    private BookUtility bookToUpdate;
    private List<BookUtility> books = new ArrayList<>();
    private static final String RECORDS_FILE = "res/records.xml";

    public BookOwnerUpdateBookModel(BookOwnerModel model) {
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

            // Save update to records.xml
            saveUpdateToRecords(bookToUpdate);

            // Send update request to the server
            sendRecordsFile();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private void initializeConnection() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUpdateToRecords(BookUtility updatedBook) {
        try {
            File file = new File(RECORDS_FILE);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootElement = document.createElement("records");
            document.appendChild(rootElement);

            Element recordElement = document.createElement("record");
            Element statusElement = document.createElement("status");
            statusElement.appendChild(document.createTextNode("UPDATE"));
            recordElement.appendChild(statusElement);

            Element bookElement = document.createElement("book");
            bookElement.appendChild(document.createTextNode(updatedBook.getTitle()));
            recordElement.appendChild(bookElement);

            rootElement.appendChild(recordElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

            System.out.println("[CLIENT] Book update recorded in records.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendRecordsFile() {
        try {
            File file = new File(RECORDS_FILE);
            if (!file.exists()) {
                System.out.println("[CLIENT] records.xml does not exist.");
                return;
            }

            // Read file contents
            StringBuilder xmlData = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                xmlData.append(line).append("\n");
            }
            reader.close();

            // Send XML data to server
            if (outputStream != null) {
                outputStream.writeObject("SEND_RECORDS_FILE");
                outputStream.writeObject(xmlData.toString());
                outputStream.flush();
                System.out.println("[CLIENT] Sent records.xml to server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[CLIENT] Error sending records.xml");
        }
    }

}


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

public class BookOwnerDeleteBookModel {
    BookOwnerModel model;
    private static final String RECORDS_FILE = "res/records.xml";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2000;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket;

    public BookOwnerDeleteBookModel(BookOwnerModel model) {
        this.model = model;
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

    public boolean deleteBook() {
        if (model.getSelectedBook() == null) {
            System.out.println("ERROR: No book selected for deletion!");
            return false;
        }

        Book bookToDelete = model.getSelectedBook();

        try {
            // Save deletion details to records.xml
            saveDeleteToRecords(bookToDelete);

            // Send updated records.xml to the server
            if (outputStream != null) {
                sendRecordsFile();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveDeleteToRecords(BookUtility bookToDelete) {
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
            statusElement.appendChild(document.createTextNode("DELETE"));
            recordElement.appendChild(statusElement);

            Element bookElement = document.createElement("book");

            Element titleElement = document.createElement("Title");
            titleElement.appendChild(document.createTextNode(bookToDelete.getTitle()));
            bookElement.appendChild(titleElement);

            Element authorElement = document.createElement("Author");
            authorElement.appendChild(document.createTextNode(bookToDelete.getAuthor()));
            bookElement.appendChild(authorElement);

            Element genreElement = document.createElement("Genre");
            genreElement.appendChild(document.createTextNode(bookToDelete.getGenre()));
            bookElement.appendChild(genreElement);

            Element stockElement = document.createElement("Stock");
            stockElement.appendChild(document.createTextNode(String.valueOf(bookToDelete.getStock())));
            bookElement.appendChild(stockElement);

            Element yearElement = document.createElement("Year");
            yearElement.appendChild(document.createTextNode(bookToDelete.getYear()));
            bookElement.appendChild(yearElement);

            Element priceElement = document.createElement("Price");
            priceElement.appendChild(document.createTextNode(String.valueOf(bookToDelete.getPrice())));
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

            System.out.println("[CLIENT] Book marked for deletion in records.xml");

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
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    xmlData.append(line).append("\n");
                }
            }

            // Send XML data to the server
            outputStream.writeObject("SEND_RECORDS_FILE");
            outputStream.writeObject(xmlData.toString());
            outputStream.flush();

            System.out.println("[CLIENT] Sent records.xml to server.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[CLIENT] Error sending records.xml");
        }
    }
}

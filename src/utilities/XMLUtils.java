package utilities;


import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class XMLUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    // Read books from books.xml
    public static List<BookUtility> readBooksFromXML(String filePath) {
        List<BookUtility> books = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("⚠️ books.xml not found! Creating an empty one.");
            writeBooksToXML(books, filePath);
            return books;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("book");

            if (nodeList.getLength() == 0) {
                System.out.println("⚠️ No <book> elements found in books.xml");
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String title = getTagValue("Title", element);
                    String author = getTagValue("Author", element);
                    String genre = getTagValue("Genre", element);
                    int stock = Integer.parseInt(getTagValue("Stock", element));
                    String year = getTagValue("Year", element);
                    double price = Double.parseDouble(getTagValue("Price", element));

                    books.add(new BookUtility(title, author, genre, stock, year, price));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() == 0 || nodeList.item(0) == null) {
            return "";  // Prevent NullPointerException
        }
        return nodeList.item(0).getTextContent().trim();
    }

    // Write books to books.xml
    public static void writeBooksToXML(List<BookUtility> books, String filePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element rootElement = document.createElement("books");
            document.appendChild(rootElement);

            for (BookUtility book : books) {
                Element bookElement = document.createElement("book");
                rootElement.appendChild(bookElement);

                addElement(document, bookElement, "Title", book.getTitle());
                addElement(document, bookElement, "Author", book.getAuthor());
                addElement(document, bookElement, "Genre", book.getGenre());
                addElement(document, bookElement, "Year", book.getYear());
                addElement(document, bookElement, "Stock", String.valueOf(book.getStock()));
                addElement(document, bookElement, "Price", String.valueOf(book.getPrice()));
            }

            // Write content to the file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filePath));

            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void addElement(Document document, Element parent, String tagName, String textContent) {
        Element element = document.createElement(tagName);
        element.appendChild(document.createTextNode(textContent));
        parent.appendChild(element);
    }

    public static void writeTransactionToXML(List<Transaction> transactions, String filePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootElement = document.createElement("transactions");
            document.appendChild(rootElement);

            int lastTransactionId = getLastTransactionId(filePath);

            for (Transaction transaction : transactions) {
                Element transactionElement = document.createElement("transaction");
                rootElement.appendChild(transactionElement);

                Element userElement = document.createElement("user");
                transactionElement.appendChild(userElement);

                addElement(document, userElement, "username", transaction.getUsername());
                addElement(document, userElement, "date", dateFormat.format(new Date()));
                addElement(document, userElement, "transactionId", String.valueOf(++lastTransactionId));
                addElement(document, userElement, "bookTitle", transaction.getBookTitle());
                addElement(document, userElement, "quantity", String.valueOf(transaction.getQuantity()));
                addElement(document, userElement, "price", String.valueOf(transaction.getPrice()));
                addElement(document, userElement, "totalAmount", String.valueOf(transaction.getTotalAmount()));
            }

            writeXMLToFile(document, filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Helper method */
    public static void writeXMLToFile(Document doc, String filePath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));

            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getLastTransactionId(String filePath) {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return 0; // If the file doesn't exist, start from 0
        }

        int lastId = 0;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("transaction");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element transactionElement = (Element) node;

                    Element userElement = (Element) transactionElement.getElementsByTagName("user").item(0);
                    if (userElement == null) {
                        continue; // Skip malformed transactions
                    }

                    String transactionId = getTagValue("transactionId", userElement);
                    if (!transactionId.isEmpty()) {
                        try {
                            int id = Integer.parseInt(transactionId);
                            lastId = Math.max(lastId, id);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastId;
    }

    // Read transactions from transaction.xml */
    public static List<Transaction> readTransactionFromXML(String filePath) {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Transaction file not found. Creating a new one.");
            writeTransactionToXML(transactions, filePath);
            return transactions;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("transaction");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element transactionElement = (Element) node;

                    // Find the <user> element inside <transaction>
                    Element userElement = (Element) transactionElement.getElementsByTagName("user").item(0);
                    if (userElement == null) {
                        System.out.println("⚠️ Skipping malformed transaction entry (missing <user>)");
                        continue;
                    }

                    String username = getTagValue("username", userElement);
                    String date = getTagValue("date", userElement);
                    String transactionId = getTagValue("transactionId", userElement);
                    String bookTitle = getTagValue("bookTitle", userElement);
                    int quantity = Integer.parseInt(getTagValue("quantity", userElement));
                    double price = Double.parseDouble(getTagValue("price", userElement));
                    double totalAmount = Double.parseDouble(getTagValue("totalAmount", userElement));

                    transactions.add(new Transaction(username, date, transactionId, bookTitle, quantity, price, totalAmount));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public static List<Sales> readSalesFromXML(String filePath) {
        List<Sales> sales = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Sales file not found. Creating a new one.");
            writeSalesToXML(sales, filePath);
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            Element transactionElement = (Element) document.getElementsByTagName("transaction");

            String transactionId = getTagValue("transactionId", transactionElement);
            String date = getTagValue("date", transactionElement);
            String bookTitle = getTagValue("title", transactionElement);
            int quantity = Integer.parseInt(getTagValue("quantity", transactionElement));
            double price = Double.parseDouble(getTagValue("price", transactionElement));
            double bookTotal = Double.parseDouble(getTagValue("bookTotal", transactionElement));
            double revenue = Double.parseDouble(getTagValue("revenue", transactionElement));

            sales.add(new Sales(transactionId, date, bookTitle, quantity, price, bookTotal, revenue));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sales;
    }
    public static void writeSalesToXML(List<Sales> sales, String filePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootElement = document.createElement("transaction");
            document.appendChild(rootElement);

            int lastTransactionId = getLastTransactionId(filePath);

            for (Sales s : sales) {
                Element transactionElement = document.createElement("transaction");
                rootElement.appendChild(transactionElement);


                addElement(document, transactionElement, "transactionId", s.getTransactionId());
                addElement(document, transactionElement, "date", dateFormat.format(new Date()));
                addElement(document, transactionElement, "transactionId", String.valueOf(++lastTransactionId));
                addElement(document, transactionElement, "title", s.getBookTitle());
                addElement(document, transactionElement, "quantity", String.valueOf(s.getQuantity()));
                addElement(document, transactionElement, "price", String.format("%.2f",s.getPrice()));
                addElement(document, transactionElement, "bookTotal",  String.format("%.2f", s.getBookTotal()));
                addElement(document, transactionElement, "revenue" ,  String.format("%.2f", s.getRevenue()));
            }

            writeXMLToFile(document, filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

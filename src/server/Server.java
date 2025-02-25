/**
 * Algorithm: Server
 * 1. Load existing users from file.
 * 2. Start server and listen for client connections.
 * 3. For each client connection:
 *    a. Accept client request.
 *    b. Process commands (Add user, Validate user, Fetch users, Process transaction).
 *    c. Handle transactions by updating books stock and generating reports.
 * 4. Save any updates to user data and sales reports.
 */

package server;

import org.w3c.dom.*;
import utilities.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Server {
    private static final int PORT = 2000;
    private static final String FILE_NAME = "res/user.ser";
    private static final String BOOKS_FILE = "res/books.xml";
    private static final String TRANSACTIONS_FILE = "res/transactions.xml";
    private static final String FAVORITE_FILE = "res/favorites.xml";
    private static final String SALES_FILE = "res/sales.xml";
    private static List<UserUtility> users = new ArrayList<>();
    private static AtomicInteger transactionCounter = new AtomicInteger(1);
    private static ExecutorService executor;
    private static Map<String, Socket> loggedInBookOwners = new ConcurrentHashMap<>();
    private static final String ACCOUNTS_FILE = "res/accounts.xml";

    /**
     * Main method to start the server and listen for incoming client connections.
     */
    public static void main(String[] args) {
        loadUsersFromXML();

        executor = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] Listening on port " + PORT);

            // Start a separate thread to handle automatic book owner login
            new Thread(() -> {
                while (true) {
                    try {
                        // Check if a book owner is already logged in (you'll need a way to track this)
                        boolean bookOwnerLoggedIn = isBookOwnerLoggedIn(); // Implement this method

                        if (!bookOwnerLoggedIn) {
                            // Find a book owner user
                            UserUtility bookOwner = findBookOwnerUser(); // Implement this method

                            if (bookOwner != null) {
                                // Simulate login
                                System.out.println("[Server] Automatically logging in book owner: " + bookOwner.getUsername());
                                try (Socket clientSocket = new Socket("localhost", PORT);
                                     ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                                     ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())) {

                                    output.writeObject("VALIDATE_USER");
                                    output.writeObject(bookOwner.getUsername());
                                    output.writeObject(bookOwner.getPassword());
                                    output.writeObject("BOOKOWNER"); // Or bookOwner.getAccountType()

                                    String response = (String) input.readObject();
                                    if (response.equals("VALID_BOOKOWNER")) {
                                        System.out.println("[Server] Automatic login successful for: " + bookOwner.getUsername());
                                        // You might want to store the client socket or some other information
                                        // to indicate that a book owner is logged in.
                                        markBookOwnerAsLoggedIn(bookOwner, clientSocket); // Implement this
                                    } else {
                                        System.out.println("[Server] Automatic login failed for: " + bookOwner.getUsername() + ". Response: " + response);
                                    }
                                    clientSocket.close();
                                } catch (IOException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Thread.sleep(5000); // Check every 5 seconds (adjust as needed)

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] New client connected: " + clientSocket.getInetAddress());

                // Submit a task to the executor to handle the client in a separate thread
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Gracefully shut down the executor service
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    /**
     * Handles client communication in a separate thread.
     */
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println("[Server] Handling client: " + clientSocket.getInetAddress());

                // Correct order: First create ObjectOutputStream, then ObjectInputStream
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

                while (true) {
                    try {
                        String command = (String) input.readObject();

                        switch (command) {
                            case "ADD_USER":
                                // Receive XML string
                                String userXml = (String) input.readObject();
                                try {
                                    UserUtility user = parseUserFromXML(userXml);
                                    handleAddUser(user, output);
                                } catch (Exception e) {
                                    output.writeObject("ERROR_PARSING_XML"); // Send error message
                                    System.err.println("[Server] Error parsing user XML: " + e.getMessage());
                                }
                                break;
                            case "VALIDATE_USER":
                                handleUserValidation(input, output);
                                break;
                            case "FETCH_USERS":
                                output.writeObject(users);
                                break;
                            case "PROCESS_CART":
                                File cartFile = new File("res/carts.xml");
                                String username = (String) input.readObject();
                                input.readObject();
                                processCart(username, cartFile);
                                sendBooksToUsers();
                                break;
                            case "FETCH_SALES_REPORT":
                                File salesFile = new File("res/sales.xml");
                                sendSalesReport(salesFile);
                                break;
                            case "ADD_BOOK":
                                BookUtility newBook = (BookUtility) input.readObject();
                                System.out.println("[Server] Received new book data: " + newBook.getTitle());
                                boolean success = addBookToXML(newBook);
                                output.writeObject(success ? "SUCCESS" : "ERROR");
                                if (success) {
                                    System.out.println("[Server] Book '" + newBook.getTitle() + "' has been added to books.xml");
                                }
                                sendBooksToUsers();
                                break;
                            case "DELETE_BOOK":
                                String titleToDelete = (String) input.readObject(); // Read the book title
                                System.out.println("[Client] Sending book for deletion: " + titleToDelete);
                                boolean deleted = deleteBookFromXML(titleToDelete); // Call the updated delete method
                                output.writeObject(deleted ? "SUCCESS" : "ERROR"); // Send response
                                if (deleted) {
                                    System.out.println("[Server] Book '" + titleToDelete + "' has been successfully deleted to books.xml");
                                }
                                sendBooksToUsers();
                                break;
                            case "UPDATE_BOOK":
                                BookUtility bookToUpdate = (BookUtility) input.readObject();
                                boolean updateSuccess = updateBookInXML(bookToUpdate);
                                output.writeObject(updateSuccess ? "SUCCESS" : "ERROR");
                                if (updateSuccess) {
                                    System.out.println("[Server] Book '" + bookToUpdate + "' is successfully updated");
                                }
                                sendBooksToUsers();
                                break;
                            case "REQUEST_BOOKS":
                                System.out.println("[SERVER] Client requested book list");
                                sendBooksToUsers();
                                System.out.println("[SERVER] Books sent");
                                break;
                            case "LOGOUT":
                                try {
                                    String string = (String) input.readObject(); // Get username from client

                                    if (loggedInBookOwners.containsKey(string)) {
                                        loggedInBookOwners.remove(string); // Remove from logged-in book owners
                                        logoutBookOwner(string);
                                        System.out.println("[Server] Book owner logged out: " + string);
                                    }

                                    output.writeObject("LOGOUT_SUCCESS"); // Inform client that logout was successful

                                    System.out.println("[Server] Client logged out: " + clientSocket.getInetAddress());

                                } catch (Exception e) {
                                    System.err.println("[Server] Error handling logout: " + e.getMessage());
                                } finally {
                                    try {
                                        clientSocket.close(); // Close socket after logout
                                    } catch (IOException e) {
                                        System.err.println("[Server] Error closing client socket: " + e.getMessage());
                                    }
                                }
                                return; // Exit the loop and end the thread
                            default:
                                System.out.println("[Server] Unknown command received: " + command);
                        }
                    } catch (EOFException e) {
                        break; // Exit loop
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                System.out.println("[Server] Client error: " + clientSocket.getInetAddress());
                e.printStackTrace(); // Print detailed error for debugging
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("[Server] Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    private static boolean isBookOwnerLoggedIn() {
        // If there's at least one logged-in book owner, return true
        return !loggedInBookOwners.isEmpty();
    }

    private static UserUtility findBookOwnerUser() {
        // Implement your logic to find a user with accountType "BOOKOWNER".
        // You can iterate through the 'users' list.
        for (UserUtility user : users) {
            if (user.getAccountType().equalsIgnoreCase("BOOKOWNER")) {
                return user;
            }
        }
        return null; // No book owner user found
    }

    private static void markBookOwnerAsLoggedIn(UserUtility bookOwner, Socket clientSocket) {
        loggedInBookOwners.put(bookOwner.getUsername(), clientSocket); // Track the book owner's connection
        System.out.println("[Server] Marked book owner as logged in: " + bookOwner.getUsername());
    }

    private static void logoutBookOwner(String username) {
        if (loggedInBookOwners.containsKey(username)) {
            loggedInBookOwners.remove(username);
            System.out.println("[Server] Book owner '" + username + "' logged out.");
        }
    }

    private static UserUtility parseUserFromXML(String userXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(userXml.getBytes()));
        doc.getDocumentElement().normalize();

        Element userElement = (Element) doc.getElementsByTagName("user").item(0); // Get the <user> element

        if (userElement == null) {
            throw new IllegalArgumentException("Invalid XML: Missing <user> element");
        }

        String username = userElement.getElementsByTagName("username").item(0).getTextContent();
        String password = userElement.getElementsByTagName("password").item(0).getTextContent();
        String accountType = userElement.getElementsByTagName("accountType").item(0).getTextContent();

        return new UserUtility(username, password, accountType);
    }

    private static void handleAddUser(UserUtility user, ObjectOutputStream output) throws IOException {
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            output.writeObject("USER_EXISTS");
        } else {
            users.add(user);
            saveUsersToXML();
            System.out.println("[Server] User added: " + user.getUsername() + " as " + user.getAccountType());
            output.writeObject("SUCCESS");
        }
    }

    /**
     * Handles user validation (Login)
     */
    private static void handleUserValidation(ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String password = (String) input.readObject();
        String accountType = (String) input.readObject();

        System.out.println("[DEBUG] Received Login Request: " + username + " | " + password + " | " + accountType);

        UserUtility user = users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user != null) {
            if (user.getAccountType().equalsIgnoreCase(accountType)) {
                System.out.println("[DEBUG] Login Successful for " + username);
                output.writeObject(accountType.equalsIgnoreCase("Buyer") ? "VALID_BUYER" : "VALID_BOOKOWNER");
            } else {
                System.out.println("[DEBUG] Account type mismatch for " + username);
                output.writeObject("INVALID");
            }
        } else {
            System.out.println("[DEBUG] User not found: " + username);
            output.writeObject("INVALID");
        }
    }

    /**
     * Saves users to file
     */
    private static void saveUsers() {
        UserUtility.serialize(users, FILE_NAME);
    }

    /**
     * Loads users from file
     */
    private static void loadUsersFromXML() {
        try {
            File file = new File(ACCOUNTS_FILE);
            if (!file.exists()) {
                System.out.println("[Server] accounts.xml not found, creating a new one.");
                // Create a root element if the file doesn't exist
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.newDocument();
                Element rootElement = doc.createElement("users");
                doc.appendChild(rootElement);
                saveXMLDocument(doc, file); // Save the initial empty XML file
                return; // No users to load yet
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("user");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String username = eElement.getElementsByTagName("username").item(0).getTextContent();
                    String password = eElement.getElementsByTagName("password").item(0).getTextContent();
                    String accountType = eElement.getElementsByTagName("accountType").item(0).getTextContent();
                    users.add(new UserUtility(username, password, accountType));
                }
            }
            System.out.println("[Server] Loaded " + users.size() + " users from accounts.xml");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[Server] Error loading users from accounts.xml: " + e.getMessage());
        }
    }

    private static void saveUsersToXML() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("users");
            doc.appendChild(rootElement);

            for (UserUtility user : users) {
                Element userElement = doc.createElement("user");
                appendChildElement(doc, userElement, "username", user.getUsername());
                appendChildElement(doc, userElement ,"password", user.getPassword());
                appendChildElement(doc, userElement, "accountType", user.getAccountType());

                rootElement.appendChild(userElement);
            }

            saveXMLDocument(doc, new File(ACCOUNTS_FILE));
            System.out.println("[Server] Users saved to accounts.xml");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[Server] Error saving users to accounts.xml: " + e.getMessage());
        }
    }

    private static void saveXMLDocument(Document doc, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Pretty printing
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    /**
     * Method to process the cart, getting the inputs in the carts.xml, computing the total amount,
     * subtracting the stocks to the quantity in carts.xml and writing the transactions.xml and sales.xml after the user's purchased
     */
    private static void processCart(String username, File cartXml) throws Exception {
        System.out.println("[DEBUG] Received cart XML for user: " + username);

        // Load book utilities from the BOOKS_FILE
        List<BookUtility> bookUtilities = loadBooksData();

        // Parse cart XML (cart items)
        Document cartDoc = parseXml(cartXml);
        NodeList bookNodes = cartDoc.getElementsByTagName("book");

        double total = 0;
        List<Transaction> transactions = new ArrayList<>();
        List<Sales> sales = new ArrayList<>();

        // Process each book in the cart
        for (int i = 0; i < bookNodes.getLength(); i++) {
            Element bookElement = (Element) bookNodes.item(i);
            String title = bookElement.getElementsByTagName("Title").item(0).getTextContent();
            int quantity = Integer.parseInt(bookElement.getElementsByTagName("Quantity").item(0).getTextContent());

            // Find the matching book in bookUtilities list
            BookUtility matchingBook = findBookByTitle(bookUtilities, title);

            if (matchingBook != null) {
                if (matchingBook.getStock() >= quantity) {
                    // Print success message before computing the total price
                    System.out.println("[Server] " + quantity + " book/s of '" + title + "' have been successfully bought by " + username);

                    matchingBook.setStock(matchingBook.getStock() - quantity); // Reduce stock

                    // Calculate total for this book
                    double bookTotal = matchingBook.getPrice() * quantity;
                    total += bookTotal;

                    // Create transaction object
                    String transactionId = generateUniqueTransactionId();
                    String currentDate = new SimpleDateFormat("MM-dd-yyyy").format(new Date());

                    transactions.add(new Transaction(username, currentDate, transactionId, title, quantity, matchingBook.getPrice(), bookTotal));
                    sales.add(new Sales(transactionId, currentDate, title, quantity, matchingBook.getPrice(), bookTotal, 0));
                } else {
                    System.out.println("[Server] Not enough stock for the book: " + title);
                }
            } else {
                System.out.println("[Server] Book not found in catalog: " + title);
            }
        }
        saveBooksData(bookUtilities); // Save updated stock
        saveTransactions(transactions);
        saveSales(sales);
    }


    /**
     * Generate unique transactionId for every purchased
     */
    private static String generateUniqueTransactionId() {
        // Get the current timestamp (milliseconds since epoch)
        long timestamp = System.currentTimeMillis();

        // Generate a unique transaction ID by combining timestamp and atomic counter
        int transactionNumber = transactionCounter.getAndIncrement(); // Increment the counter

        // Format the transaction ID to ensure uniqueness
        return String.format("%d-%06d", timestamp, transactionNumber);
    }

    /**
     * Method for saving the transactions to transactions.xml
     */
    private static void saveTransactions(List<Transaction> newTransactions) throws Exception {
        File file = new File(TRANSACTIONS_FILE);
        Document doc;

        if (file.exists() && file.length() > 0) {
            doc = parseXml(file);
        } else {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            saveXMLDocument(doc, TRANSACTIONS_FILE);
            Element root = doc.createElement("transactions");
            doc.appendChild(root);
        }

        Element root = doc.getDocumentElement();

        for (Transaction transaction : newTransactions) {
            Element transactionElement = doc.createElement("transaction");
            Element userElement = doc.createElement("user");

            appendChildElement(doc, userElement, "username", transaction.getUsername());
            appendChildElement(doc, userElement, "date", transaction.getDate());
            appendChildElement(doc, userElement, "transactionId", transaction.getTransactionId());
            appendChildElement(doc, userElement, "bookTitle", transaction.getBookTitle());
            appendChildElement(doc, userElement, "quantity", String.valueOf(transaction.getQuantity()));
            appendChildElement(doc, userElement, "price", String.format("%.2f", transaction.getPrice()));
            appendChildElement(doc, userElement, "totalAmount", String.format("%.2f", transaction.getTotalAmount()));

            transactionElement.appendChild(userElement);
            root.appendChild(transactionElement);
        }

        saveXmlDocument(doc, file);
    }
    private static void saveSales(List<Sales> newSales) throws Exception {
        File file = new File(SALES_FILE);
        Document salesXML;

        if (file.exists() && file.length() > 0) {
            salesXML = parseXml(file);
        } else {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            salesXML = builder.newDocument();
            Element root = salesXML.createElement("sales");
            salesXML.appendChild(root);
        }

        Element root = salesXML.getDocumentElement();

        for (Sales sales : newSales) {
            Element transactionElement = salesXML.createElement("transactions");

            appendChildElement(salesXML, transactionElement, "transactionId", sales.getTransactionId());
            appendChildElement(salesXML, transactionElement, "book", sales.getBookTitle());
            appendChildElement(salesXML, transactionElement, "quantity", String.valueOf(sales.getQuantity()));
            appendChildElement(salesXML, transactionElement, "price", String.format("%.2f", sales.getPrice()));
            appendChildElement(salesXML, transactionElement, "total", String.format("%.2f", sales.getBookTotal()));
            appendChildElement(salesXML, transactionElement, "revenue", String.format("%.2f", sales.getRevenue()));

            root.appendChild(transactionElement);
        }

        saveXmlDocument(salesXML, file);
    }

    /**
     * Helper methods of saveTransaction method
     */
    private static void appendChildElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
    }

    /**
     * Updating the Book stocks after the user's purchase to the books.xml
     */
    private static void saveBooksData(List<BookUtility> books) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("books");
        doc.appendChild(rootElement);

        for (BookUtility book : books) {
            Element bookElement = doc.createElement("book");

            appendChildElement(doc, bookElement, "Title", book.getTitle());
            appendChildElement(doc, bookElement, "Author", book.getAuthor());
            appendChildElement(doc, bookElement, "Genre", book.getAuthor());
            appendChildElement(doc, bookElement, "Stock", String.valueOf(book.getStock()));
            appendChildElement(doc, bookElement, "Year", String.valueOf(book.getYear()));
            appendChildElement(doc, bookElement, "Price", String.valueOf(book.getPrice()));

            rootElement.appendChild(bookElement);
        }

        saveXmlDocument(doc, new File(BOOKS_FILE)); // Save to books.xml
    }

    /**
     * Helper method to load books from XML file
     */
    private static List<BookUtility> loadBooksData() throws Exception {
        List<BookUtility> bookUtilities = new ArrayList<>();
        Document doc = parseXml(new File(BOOKS_FILE));
        NodeList bookNodes = doc.getElementsByTagName("book");

        for (int i = 0; i < bookNodes.getLength(); i++) {
            Element bookElement = (Element) bookNodes.item(i);
            String title = bookElement.getElementsByTagName("Title").item(0).getTextContent();
            String author = bookElement.getElementsByTagName("Author").item(0).getTextContent();
            String genre = bookElement.getElementsByTagName("Genre").item(0).getTextContent();
            int stock = Integer.parseInt(bookElement.getElementsByTagName("Stock").item(0).getTextContent());
            String year = bookElement.getElementsByTagName("Year").item(0).getTextContent();
            double price = Double.parseDouble(bookElement.getElementsByTagName("Price").item(0).getTextContent());

            // Create BookUtility object and add to list
            bookUtilities.add(new BookUtility(title, author, genre, stock, year, price));
        }

        return bookUtilities;
    }

    /**
     * method to find a book by title in the list of BookUtilities
     */
    private static BookUtility findBookByTitle(List<BookUtility> books, String title) {
        for (BookUtility book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;  // Return null if book is not found
    }

    private static Document parseXml(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    private static void saveXmlDocument(Document doc, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    /** method to deserialize the transaction.xml */
    private static List<Transaction> loadTransactions() {
        try {
            return XMLUtils.readTransactionFromXML(TRANSACTIONS_FILE); // Using the existing utility method
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private static List<Sales> loadSales() {
        try {
            return XMLUtils.readSalesFromXML(SALES_FILE); // Using the existing utility method
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    /** Fetches all the transactions, computes total sales and revenue per month, then send it to the BookOwnerSalesReportModel */
    private static File sendSalesReport(File salesXML) throws IOException {
        salesXML = new File("res/sales.xml");

        List<Transaction> transactions = loadTransactions();
        List<Sales> sales = loadSales();
        Map<String, Double> revenueByMonth = new HashMap<>();

        for (Transaction transaction : transactions) {
            String month = transaction.getDate().substring(0, 7); // Extract "MM-yyyy"
            double totalSales = transaction.getQuantity() * transaction.getPrice();

            revenueByMonth.put(month, revenueByMonth.getOrDefault(month, 0.0) + totalSales);
        }

        sales.add(new Sales(transactionId, date, title, quantity, matchingBook.getPrice(), bookTotal, revenueByMonth));
        System.out.println("[Server] Sales Report successfully sent to client ");
        return salesXML;
    }

    /** Method for adding the book received from the BookOwnerAddBookModel */
    private static synchronized boolean addBookToXML(BookUtility book) {
        try {
            List<BookUtility> books = XMLUtils.readBooksFromXML("res/books.xml");
            books.add(book);
            XMLUtils.writeBooksToXML(books, "res/books.xml");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Method for deleting the book */
    private static boolean deleteBookFromXML(String title) {
        try {
            List<BookUtility> books = XMLUtils.readBooksFromXML(BOOKS_FILE);
            boolean removed = books.removeIf(existingBook -> existingBook.getTitle().equalsIgnoreCase(title));

            if (removed) {
                XMLUtils.writeBooksToXML(books, BOOKS_FILE);
                System.out.println("[SERVER] Book deleted: " + title);
            } else {
                System.out.println("[SERVER] Book not found: " + title);
            }
            return removed;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Method for updating the books*/
    private static boolean updateBookInXML(BookUtility updatedBook) {
        try {
            List<BookUtility> books = loadBooksData();
            boolean updated = false;

            for (BookUtility book : books) {
                if (book.getTitle().equalsIgnoreCase(updatedBook.getTitle())) {
                    book.setStock(updatedBook.getStock()); // Update stock
                    book.setPrice(updatedBook.getPrice()); // Update price
                    updated = true;
                    break;
                }
            }

            if (updated) {
                saveBooksData(books); // Save the updated book list back to XML
                System.out.println("[SERVER] Book updated: " + updatedBook.getTitle());
            } else {
                System.out.println("[SERVER] Book not found for update: " + updatedBook.getTitle());
            }

            return updated;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private static void sendBooksToUsers() {
        File booksFile = new File(BOOKS_FILE);

        if (!booksFile.exists()) {
            System.err.println("[SERVER] Error: books.xml not found.");
            return;
        }

        try {
            ObjectInputStream fileReader = new ObjectInputStream(new FileInputStream(BOOKS_FILE));

            for (Map.Entry<String, Socket> entry : loggedInBookOwners.entrySet()) {
                try {
                    Socket clientSocket = entry.getValue();
                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                    output.writeObject("UPDATE_BOOKS"); // Send command
                    output.writeObject(fileReader); // Send XML data
                    System.out.println("[DEBUG] Server sending books.xml content:\n" + fileReader);
                    output.flush();

                    System.out.println("[SERVER] Sent books.xml to user: " + entry.getKey());
                } catch (IOException e) {
                    System.err.println("[SERVER] Error sending books.xml to " + entry.getKey() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Error reading books.xml: " + e.getMessage());
        }
    }


}




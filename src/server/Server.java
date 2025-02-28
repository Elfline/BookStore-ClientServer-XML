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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 2000;
    private static final String FILE_NAME = "res/server/user.ser";
    private static final String BOOKS_FILE = "res/server/books.xml";
    private static final String TRANSACTIONS_FILE = "res/server/transactions.xml";
    private static final String FAVORITE_FILE = "res/server/favorites.xml";
    private static final String SALES_FILE = "res/server/sales.xml";
    private static final String ACCOUNTS_FILE = "res/server/accounts.xml";
    private static List<User> users = new ArrayList<>();
    private static List<Book> books = new ArrayList<>();
    private static AtomicInteger transactionCounter = new AtomicInteger(1);
    private static ExecutorService executor;
    private static Map<String, Socket> loggedInBookOwners = new ConcurrentHashMap<>();


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
                            User bookOwner = findBookOwnerUser(); // Implement this method

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
                                    User user = XMLUtils.parseUserFromXML(userXml);
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
                            case "ADD_TO_CART":
                                String username = (String) input.readObject();
                                String cartXml = (String) input.readObject();
                                double total = processCart(username, cartXml);
                                output.writeObject(total);
                                break;
                            case "FETCH_BUY_HISTORY":
                                String loggedUser = (String) input.readObject(); // Read username from client
                                sendTransactionsToUsers();
                                System.out.println("[Server] Sent buy history for: " + loggedUser);
                                break;
                            case "FETCH_SALES_REPORT":
                                sendSalesReport(output);
                                break;
                            case "ADD_BOOK":
                                Book newBook = (Book) input.readObject();
                                System.out.println("[Server] Received new book data: " + newBook.getTitle());
                                boolean success = addBookToXML(newBook);
                                output.writeObject(success ? "SUCCESS" : "ERROR");
                                if (success) {
                                    System.out.println("[Server] Book '" + newBook.getTitle() +"' has been added to books.xml");
                                }
                                break;
                            case "DELETE_BOOK":
                                String titleToDelete = (String) input.readObject(); // Read the book title
                                System.out.println("[Client] Sending book for deletion: " + titleToDelete);
                                boolean deleted = deleteBookFromXML(titleToDelete); // Call the updated delete method
                                output.writeObject(deleted ? "SUCCESS" : "ERROR"); // Send response
                                if (deleted) {
                                    System.out.println("[Server] Book '" + titleToDelete + "' has been successfully deleted to books.xml");
                                }
                                break;
                            case "UPDATE_BOOK":
                                Book bookToUpdate = (Book) input.readObject();
                                boolean updateSuccess = updateBookInXML(bookToUpdate);
                                output.writeObject(updateSuccess ? "SUCCESS" : "ERROR");
                                if (updateSuccess) {
                                    System.out.println("[Server] Book '" + bookToUpdate +"' is successfully updated");
                                }
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
        return !loggedInBookOwners.isEmpty();
    }

    private static User findBookOwnerUser() {
        for (User user : users) {
            if (user.getAccountType().equalsIgnoreCase("BOOKOWNER")) {
                return user;
            }
        }
        return null; // No book owner user found
    }

    private static void markBookOwnerAsLoggedIn(User bookOwner, Socket clientSocket) {
        loggedInBookOwners.put(bookOwner.getUsername(), clientSocket); // Track the book owner's connection
        System.out.println("[Server] Marked book owner as logged in: " + bookOwner.getUsername());
    }

    private static void logoutBookOwner(String username) {
        if (loggedInBookOwners.containsKey(username)) {
            loggedInBookOwners.remove(username);
            System.out.println("[Server] Book owner '" + username + "' logged out.");
        }
    }

    private static void handleAddUser(User user, ObjectOutputStream output) throws IOException {
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            output.writeObject("USER_EXISTS");
        } else {
            users.add(user);
            XMLUtils.saveUsersToXML();
            System.out.println("[Server] User added: " + user.getUsername() + " as " + user.getAccountType());
            output.writeObject("SUCCESS");
        }
    }

    public static User parseUserFromXML(String userXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(userXml.getBytes()));
        doc.getDocumentElement().normalize();

        Element userElement = (Element) doc.getElementsByTagName("user").item(0);

        if (userElement == null) {
            throw new IllegalArgumentException("Invalid XML: Missing <user> element");
        }

        String username = userElement.getElementsByTagName("username").item(0).getTextContent();
        String password = userElement.getElementsByTagName("password").item(0).getTextContent();
        String accountType = userElement.getElementsByTagName("accountType").item(0).getTextContent();

        return new User(username, password, accountType);
    }

    /** Updating the Book stocks after the user's purchase to the books.xml */
    public static void saveBooksData(List<Book> books) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("books");
        doc.appendChild(rootElement);

        for (Book book : books) {
            Element bookElement = doc.createElement("book");

            appendChildElement(doc, bookElement, "Title", book.getTitle());
            appendChildElement(doc, bookElement, "Author", book.getAuthor());
            appendChildElement(doc, bookElement, "Genre", book.getGenre());
            appendChildElement(doc, bookElement, "Stock", String.valueOf(book.getStock()));
            appendChildElement(doc, bookElement, "Year", String.valueOf(book.getYear()));
            appendChildElement(doc, bookElement, "Price", String.format("2.f",book.getTitle()));

            rootElement.appendChild(bookElement);
        }

        saveXmlDocument(doc, new File(BOOKS_FILE));
    }


    /**
     * Handles user validation (Login)
     */
    private static void handleUserValidation(ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String password = (String) input.readObject();
        String accountType = (String) input.readObject();

        System.out.println("[DEBUG] Received Login Request: " + username + " | " + password + " | " + accountType);

        User user = users.stream()
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

    public static void appendChildElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
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

    /**
     * Loads users from file
     */
    private static void loadUsersFromXML() {
        try {
            File file = new File(ACCOUNTS_FILE);
            if (!file.exists()) {
                System.out.println("[Server] accounts.xml not found, creating a new one.");
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element rootElement = doc.createElement("users");
                doc.appendChild(rootElement);
                saveXmlDocument(doc, file);
                return; // No users to load yet
            }
            Document doc = parseXml(file);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("user");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String username = eElement.getElementsByTagName("username").item(0).getTextContent();
                    String password = eElement.getElementsByTagName("password").item(0).getTextContent();
                    String accountType = eElement.getElementsByTagName("accountType").item(0).getTextContent();
                    users.add(new User(username, password, accountType));
                }
            }
            System.out.println("[SERVER] Loaded " + users.size() + " users from accounts.xml");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[DEBUG] Error loading users from accounts.xml: " + e.getMessage());
        }
    }

    /** Method to process the cart, */
    private static double processCart(String username, File cart) throws Exception {
        System.out.println("[SERVER] Received cart XML for user: " + username);

        // Load book utilities from the BOOKS_FILE
        List<Book> books = loadBooksData();

        Document cartDoc = parseXml(cart);
        NodeList bookNodes = cartDoc.getElementsByTagName("book");

        double total = 0;
        List<Transaction> transactions = new ArrayList<>();

        // Process each book in the cart
        for (int i = 0; i < bookNodes.getLength(); i++) {
            Element bookElement = (Element) bookNodes.item(i);
            String title = bookElement.getElementsByTagName("Title").item(0).getTextContent();
            int quantity = Integer.parseInt(bookElement.getElementsByTagName("Quantity").item(0).getTextContent());

            // Find the matching book in bookUtilities list
            Book matchingBook = findBookByTitle(books, title);

            if (matchingBook != null) {
                if (matchingBook.getStock() >= quantity) {
                    System.out.println("[Server] " + quantity + " book/s of '" + title + "' have been successfully bought by " + username);
                    matchingBook.setStock(matchingBook.getStock() - quantity); // Reduce stock

                    // Calculate total for this book
                    double bookTotal = matchingBook.getPrice() * quantity;
                    total += bookTotal;

                    // Create transaction object
                    String transactionId = generateUniqueTransactionId();
                    String currentDate = new SimpleDateFormat("MM-dd-yyyy").format(new Date());

                    transactions.add(new Transaction(username, currentDate, transactionId, title, quantity, matchingBook.getPrice(), bookTotal));
                } else {
                    System.out.println("[Server] Not enough stock for the book: " + title);
                }
            } else {
                System.out.println("[Server] Book not found in catalog: " + title);
            }
        }
        saveBooksData(books); // Save updated stock
        saveTransactions(transactions);
        return total;
    }

     /** Updating the Book stocks after the user's purchase to the books.xml */
    private static void saveBooksData(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("books");
        doc.appendChild(rootElement);

        for (Book book : books) {
            Element bookElement = doc.createElement("book");

            appendChildElement(doc, bookElement, "Title", book.getTitle());
            appendChildElement(doc, bookElement, "Author", book.getAuthor());
            appendChildElement(doc, bookElement, "Genre", book.getGenre());
            appendChildElement(doc, bookElement, "Stock", String.valueOf(book.getStock()));
            appendChildElement(doc, bookElement, "Year", String.valueOf(book.getYear()));
            appendChildElement(doc, bookElement, "Price", String.format("2.f",book.getTitle()));

            rootElement.appendChild(bookElement);
        }

        saveXmlDocument(doc, new File(BOOKS_FILE));
    }

     /** Generate unique transactionId for every purchased */
    private static String generateUniqueTransactionId() {

        long timestamp = System.currentTimeMillis(); // Get the current timestamp (milliseconds since epoch)
        int transactionNumber = transactionCounter.getAndIncrement(); // Generate a unique transaction ID by combining timestamp and atomic counter and increment the counter
        return String.format("%d-%06d", timestamp, transactionNumber); // Format the transaction ID to ensure uniqueness
    }

     /** Method for saving the transactions to transactions.xml */
    private static void saveTransactions(List<Transaction> newTransactions) throws Exception {
        File file = new File(TRANSACTIONS_FILE);
        Document doc;

        if (file.exists() && file.length() > 0) {
            doc = parseXml(file);
        } else {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            Element root = doc.createElement("transactions");
            doc.appendChild(root);
        }

        Element root = doc.getDocumentElement();

        for (Transaction transaction : newTransactions) {
            Element userElement = doc.createElement("user");

            appendChildElement(doc, userElement, "username", transaction.getUsername());
            appendChildElement(doc, userElement, "date", transaction.getDate());
            appendChildElement(doc, userElement, "transactionId", transaction.getTransactionId());
            appendChildElement(doc, userElement, "bookTitle", transaction.getBookTitle());
            appendChildElement(doc, userElement, "quantity", String.valueOf(transaction.getQuantity()));
            appendChildElement(doc, userElement, "price", String.format("%.2f", transaction.getPrice()));
            appendChildElement(doc, userElement, "totalAmount", String.format("%.2f", transaction.getTotalAmount()));

            root.appendChild(userElement);
        }

        saveXmlDocument(doc, file);
    }
    /** Method for saving sales to sales.xml */
    private static void saveSales() {
        try {
            File transactionsFile = new File(TRANSACTIONS_FILE);
            if (!transactionsFile.exists()) {
                System.out.println("[DEBUG] Transactions file not found");
                return;
            }

            // Parse transactions.xml using helper method
            Document transactionsDoc = parseXml(transactionsFile);
            transactionsDoc.getDocumentElement().normalize();

            // Data structure: Year -> Month -> Day -> List of Transactions
            Map<String, Map<String, Map<String, List<Element>>>> salesData = new TreeMap<>();
            Map<String, Double> monthlyRevenue = new HashMap<>();
            Map<String, Double> yearlyRevenue = new HashMap<>();

            NodeList nodeList = transactionsDoc.getElementsByTagName("user");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element transaction = (Element) nodeList.item(i);
                String date = transaction.getElementsByTagName("date").item(0).getTextContent();
                String year = date.substring(6, 10);
                String month = date.substring(0, 2);
                String day = date.substring(3, 5);
                String totalAmount = transaction.getElementsByTagName("totalAmount").item(0).getTextContent();

                // Organize transactions per year, month, and day
                salesData.computeIfAbsent(year, y -> new TreeMap<>())
                        .computeIfAbsent(month, m -> new TreeMap<>())
                        .computeIfAbsent(day, d -> new ArrayList<>())
                        .add(transaction);

                // Compute revenue per month and year
                String monthKey = year + "-" + month;
                String yearKey = year;
                double amount = Double.parseDouble(totalAmount);
                monthlyRevenue.put(monthKey, monthlyRevenue.getOrDefault(monthKey, 0.0) + amount);
                yearlyRevenue.put(yearKey, yearlyRevenue.getOrDefault(yearKey, 0.0) + amount);
            }

            // Generate sales.xml
            Document salesDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = salesDoc.createElement("sales");
            salesDoc.appendChild(root);

            for (String year : salesData.keySet()) {
                Element yearElement = salesDoc.createElement("year");
                yearElement.setAttribute("value", year);
                yearElement.setAttribute("totalRevenue", String.format("%.2f", yearlyRevenue.get(year)));
                root.appendChild(yearElement);

                for (String month : salesData.get(year).keySet()) {
                    Element monthElement = salesDoc.createElement("month");
                    monthElement.setAttribute("value", month);
                    monthElement.setAttribute("totalRevenue", String.format("%.2f", monthlyRevenue.get(year + "-" + month)));
                    yearElement.appendChild(monthElement);

                    for (String day : salesData.get(year).get(month).keySet()) {
                        Element dayElement = salesDoc.createElement("day");
                        dayElement.setAttribute("value", day);
                        monthElement.appendChild(dayElement);

                        for (Element transaction : salesData.get(year).get(month).get(day)) {
                            Element transactionElement = salesDoc.createElement("transaction");

                            // Copy relevant transaction details
                            String[] fields = {"transactionId", "bookTitle", "quantity", "price", "totalAmount"};
                            for (String field : fields) {
                                Element fieldElement = salesDoc.createElement(field);
                                fieldElement.setTextContent(transaction.getElementsByTagName(field).item(0).getTextContent());
                                transactionElement.appendChild(fieldElement);
                            }

                            dayElement.appendChild(transactionElement);
                        }
                    }
                }
            }

            saveXmlDocument(salesDoc, new File(SALES_FILE));
            System.out.println("[SERVER] sales.xml generated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to load books from XML file
     */
    private static List<Book> loadBooksData(File xmlFile) throws Exception {
        List<Book> books = new ArrayList<>();
        try {
            if (!xmlFile.exists()) {
                return books; // Return an empty list if the file doesn't exist
            }
            Document doc = parseXml(new File(BOOKS_FILE));
            doc.getDocumentElement().normalize();
            NodeList bookNodes = doc.getElementsByTagName("book");

            for (int i = 0; i < bookNodes.getLength(); i++) {
                Element bookElement = (Element) bookNodes.item(i);
                String title = bookElement.getElementsByTagName("Title").item(0).getTextContent();
                String author = bookElement.getElementsByTagName("Author").item(0).getTextContent();
                String genre = bookElement.getElementsByTagName("Genre").item(0).getTextContent();
                int stock = Integer.parseInt(bookElement.getElementsByTagName("Stock").item(0).getTextContent());
                String year = bookElement.getElementsByTagName("Year").item(0).getTextContent();
                double price = Double.parseDouble(bookElement.getElementsByTagName("Price").item(0).getTextContent());

                books.add(new Book(title, author, genre, stock, year, price));
            }

            return books;
        }
    }

    /**
     * method to find a book by title in the list of BookUtilities
     */
    private static Book findBookByTitle(List<Book> books, String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;  // Return null if book is not found
    }

    /** method to deserialize the transaction.xml */
    private static List<Transaction> loadTransactions() {
        try {
            return XMLUtils.readTransactionFromXML(TRANSACTIONS_FILE);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Method for adding the book received from the BookOwnerAddBookModel */
    private static synchronized boolean addBookToXML(File bookXML, Book book) {
        try {
            books = loadBooksData(bookXML);
            book.add(book);
            saveBooksData(bookXML);
            System.out.println("[SERVER] Book added: " + book.getTitle());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Method for deleting the book */
    private static synchronized boolean deleteBookFromXML(File bookXML, String title) {
        try {
            books = loadBooksData(xmlFile);
            boolean removed = books.removeIf(book -> book.getTitle().equalsIgnoreCase(title));

            if (removed) {
                saveBooksData(xmlFile);
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
    private static synchronized boolean updateBookInXML(File bookXML, Book updatedBook) {
        try {
            books = loadBooksData();
            boolean updated = false;

            for (Book book : books) {
                if (book.getTitle().equalsIgnoreCase(updatedBook.getTitle())) {
                    book.setStock(updatedBook.getStock()); // Update stock
                    book.setPrice(updatedBook.getPrice()); // Update price
                    updated = true;
                    break;
                }
            }

            if (updated) {
                saveBooksData(bookXML); // Save the updated book list back to XML
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

    /** Method for sending the books.xml to all users */
    private static synchronized void sendBooksToUsers() {
        File booksFile = new File(BOOKS_FILE);

        if (!booksFile.exists()) {
            System.err.println("[DEBUG] books.xml not found.");
            return;
        }

        try {
            for (Map.Entry<String, Socket> entry : loggedInBookOwners.entrySet()) {
                try {
                    Socket clientSocket = entry.getValue();
                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                    output.writeObject("UPDATE_BOOKS");
                    output.writeObject(BOOKS_FILE);
                    output.flush();

                    System.out.println("[SERVER] Sent books.xml to user: " + entry.getKey());
                } catch (IOException e) {
                    System.err.println("[SERVER] Error sending books.xml to " + entry.getKey() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Method for sending transactions.xml to all user */
    private static synchronized void sendTransactionsToUsers() {
        File booksFile = new File(TRANSACTIONS_FILE);

        if (!booksFile.exists()) {
            System.err.println("[DEBUG] books.xml not found.");
            return;
        }

        try {
            for (Map.Entry<String, Socket> entry : loggedInBookOwners.entrySet()) {
                try {
                    Socket clientSocket = entry.getValue();
                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                    output.writeObject("UPDATE_TRANSACTIONS");
                    output.writeObject(TRANSACTIONS_FILE);
                    output.flush();

                    System.out.println("[SERVER] Sent transactions.xml to user: " + entry.getKey());
                } catch (IOException e) {
                    System.err.println("[SERVER] Error sending transactions.xml to " + entry.getKey() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




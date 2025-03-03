package server;

import org.w3c.dom.Node;
import utilities.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ServerXml {
    private static final String BOOKS_FILE = "res/server/books.xml";
    private static final String TRANSACTIONS_FILE = "res/server/transactions.xml";
    private static final String FAVORITE_FILE = "res/server/favorites.xml";
    private static final String SALES_FILE = "res/server/sales.xml";
    private static final String ACCOUNTS_FILE = "res/server/accounts.xml";
    private static List<User> users = new ArrayList<>();
    private static AtomicInteger transactionCounter = new AtomicInteger(1);

    /** Helper methods in parsing xml */
    public static void appendChildElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
    }

    /** Helper methods in parsing xml */
    public static Document parseXml(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    /** Helper methods in parsing xml */
    public static void saveXmlDocument(Document doc, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    /** Load users from XML file */
    public static List<User> loadUsers() {
        File xmlFile = new File(ACCOUNTS_FILE);

        try {
            if (!xmlFile.exists()) {
                System.out.println("[SERVER] accounts.xml not found, creating a new one.");
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element rootElement = doc.createElement("accounts");
                doc.appendChild(rootElement);
                return users; // Return empty list if file doesn't exist
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("user");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
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
        return users;
    }

    /** Save users list to XML file */
    public static boolean saveUsers(User newUser) throws Exception {
        File xmlFile = new File(ACCOUNTS_FILE);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc;

        if (xmlFile.exists() && xmlFile.length() > 0) {
            // Load existing XML document
            doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();
        } else {
            // Create a new XML document if none exists
            doc = builder.newDocument();
            Element rootElement = doc.createElement(ACCOUNTS_FILE);
            doc.appendChild(rootElement);
        }

        Element rootElement = doc.getDocumentElement();

        // Check if the user already exists
        NodeList userList = rootElement.getElementsByTagName("user");
        for (int i = 0; i < userList.getLength(); i++) {
            Element existingUser = (Element) userList.item(i);
            String existingUsername = existingUser.getElementsByTagName("Username").item(0).getTextContent();
            if (existingUsername.equals(newUser.getUsername())) {
                System.out.println("[SERVER] User " + newUser.getUsername() + " already exists.");
                return false; // User already exists
            }
        }

        // Create new user entry
        Element userElement = doc.createElement("user");
        appendChildElement(doc, userElement, "Username", newUser.getUsername());
        appendChildElement(doc, userElement, "Password", newUser.getPassword());
        appendChildElement(doc, userElement, "AccountType", newUser.getAccountType());

        rootElement.appendChild(userElement);

        saveXmlDocument(doc, xmlFile);
        return true;
    }

    /** Loads users from the given file */
    public static List<User> loadUsersFromFile(File userFile) {
        List<User> users = new ArrayList<>();

        try {
            if (!userFile.exists()) {
                System.out.println("[SERVER] File not found: " + userFile.getAbsolutePath());
                return users; // Return empty list if file doesn't exist
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(userFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("user");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String username = element.getElementsByTagName("username").item(0).getTextContent();
                    String password = element.getElementsByTagName("password").item(0).getTextContent();
                    String accountType = element.getElementsByTagName("accountType").item(0).getTextContent();

                    users.add(new User(username, password, accountType));
                }
            }
            System.out.println("[SERVER] Loaded " + users.size() + " users from " + userFile.getName());

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load users from " + userFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /** Method for loading a single user from XML for logout.xml */
    public static User loadUserForLoggedOut(File file) {
        if (!file.exists()) {
            System.err.println("[DEBUG] Logout XML file not found: " + file.getAbsolutePath());
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList userList = doc.getElementsByTagName("user");

            if (userList.getLength() == 0) {
                System.err.println("[DEBUG] No user data found in logout.xml.xml.");
                return null;
            }

            Element userElement = (Element) userList.item(0);
            String username = userElement.getElementsByTagName("username").item(0).getTextContent();

            return new User(username);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to parse logout.xml.xml: " + e.getMessage());
            return null;
        }
    }

    /** Load books from XML file */
    public static List<Book> loadBooks(File xmlFile) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }

    /** Updating the Books from books.xml */
    public static void saveBooks(List<Book> books, File bookXML) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.getDocumentElement().normalize();

        Element rootElement = doc.createElement("books");
        doc.appendChild(rootElement);

        for (Book book : books) {
            Element bookElement = doc.createElement("book");

            appendChildElement(doc, bookElement, "Title", book.getTitle());
            appendChildElement(doc, bookElement, "Author", book.getAuthor());
            appendChildElement(doc, bookElement, "Genre", book.getGenre());
            appendChildElement(doc, bookElement, "Stock", String.valueOf(book.getStock()));
            appendChildElement(doc, bookElement, "Year", String.valueOf(book.getYear()));
            appendChildElement(doc, bookElement, "Price", String.format("2.f",book.getPrice()));

            rootElement.appendChild(bookElement);
        }

        saveXmlDocument(doc, bookXML);
    }

    /** Method for saving the cart to cart.xml */
    public static synchronized void saveCart(String username, File cart) throws Exception {
        System.out.println("[SERVER] Received cart XML for user: " + username);

        File bookXML = new File(BOOKS_FILE);
        List<Book> books = loadBooks(bookXML);

        Document cartDoc = parseXml(cart);
        NodeList bookNodes = cartDoc.getElementsByTagName("book");

        double total = 0;
        List<Transaction> transactions = new ArrayList<>();

        // Process each book in the cart
        for (int i = 0; i < bookNodes.getLength(); i++) {
            Element bookElement = (Element) bookNodes.item(i);
            String title = bookElement.getElementsByTagName("Title").item(0).getTextContent();
            int quantity = Integer.parseInt(bookElement.getElementsByTagName("Quantity").item(0).getTextContent());

            // Find the matching book in the catalog
            Book matchingBook = findBookByTitle(books, title);

            if (matchingBook != null) {
                if (matchingBook.getStock() >= quantity) {
                    System.out.println("[SERVER] " + quantity + " book of '" + title + "' successfully purchased by " + username);
                    matchingBook.setStock(matchingBook.getStock() - quantity); // Reduce stock

                    // Calculate total for this book
                    double bookTotal = matchingBook.getPrice() * quantity;
                    total += bookTotal;

                    // Create transaction object
                    String transactionId = generateUniqueTransactionId();
                    String currentDate = new SimpleDateFormat("MM-dd-yyyy").format(new Date());

                    transactions.add(new Transaction(username, currentDate, transactionId, title, quantity, matchingBook.getPrice(), bookTotal));
                } else {
                    System.out.println("[SERVER] Not enough stock for the book: " + title);
                }
            } else {
                System.out.println("[SERVER] Book not found in catalog: " + title);
            }
        }

        // Save updated book stock
        saveBooks(books, bookXML);
        if (!transactions.isEmpty()) {
            saveTransactions(username, transactions);
            saveSales(transactions);
        }
    }


    /** Generate unique transactionId for every purchased */
    private static String generateUniqueTransactionId() {

        long timestamp = System.currentTimeMillis(); // Get the current timestamp (milliseconds since epoch)
        int transactionNumber = transactionCounter.getAndIncrement(); // Generate a unique transaction ID by combining timestamp and atomic counter and increment the counter
        return String.format("%d-%06d", timestamp, transactionNumber); // Format the transaction ID to ensure uniqueness
    }

    /**
     * method to find a book by title that is used in cart
     */
    public static Book findBookByTitle(List<Book> books, String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;  // Return null if book is not found
    }


    /** Method for saving the transactions to transactions.xml */
    public static void saveTransactions(String username, List<Transaction> newTransactions) throws Exception {
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
            userElement.setAttribute("username", transaction.getUsername().equals(username));

            Element transactionElement = doc.createElement("transaction");
            appendChildElement(doc, transactionElement, "date", transaction.getDate());
            appendChildElement(doc, transactionElement, "transactionId", transaction.getTransactionId());
            appendChildElement(doc, transactionElement, "bookTitle", transaction.getBookTitle());
            appendChildElement(doc, transactionElement, "quantity", String.valueOf(transaction.getQuantity()));
            appendChildElement(doc, transactionElement, "price", String.format("%.2f", transaction.getPrice()));
            appendChildElement(doc, transactionElement, "totalAmount", String.format("%.2f", transaction.getTotalAmount()));

            userElement.appendChild(transactionElement);
            root.appendChild(userElement);
        }

        saveXmlDocument(doc, file);

        // Now save sales correctly
        saveSales(newTransactions);
    }


    /** Method in loading transactions from an XML file */
    public static List<Transaction> loadTransactions(File file) {
        List<Transaction> transactions = new ArrayList<>();

        if (!file.exists()) {
            System.err.println("[DEBUG] Transactions.xml file not found: " + file.getAbsolutePath());
            return transactions;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Element userElement = (Element) userList.item(i);
                String username = userElement.getAttribute("username");

                NodeList transactionNodes = userElement.getElementsByTagName("transaction");

                for (int j = 0; j < transactionNodes.getLength(); j++) {
                    Element transactionElement = (Element) transactionNodes.item(j);

                    String date = transactionElement.getElementsByTagName("date").item(0).getTextContent();
                    String transactionId = transactionElement.getElementsByTagName("transactionId").item(0).getTextContent();
                    String bookTitle = transactionElement.getElementsByTagName("bookTitle").item(0).getTextContent();
                    int quantity = Integer.parseInt(transactionElement.getElementsByTagName("quantity").item(0).getTextContent());

                    double price = 0.0;
                    double totalAmount = 0.0;

                    // Handle missing or malformed price and totalAmount
                    Node priceNode = transactionElement.getElementsByTagName("price").item(0);
                    if (priceNode != null) {
                        price = Double.parseDouble(priceNode.getTextContent());
                    }

                    Node totalAmountNode = transactionElement.getElementsByTagName("totalAmount").item(0);
                    if (totalAmountNode != null) {
                        totalAmount = Double.parseDouble(totalAmountNode.getTextContent());
                    }

                    transactions.add(new Transaction(username, date, transactionId, bookTitle, quantity, price, totalAmount));
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load transactions.xml: " + e.getMessage());
        }

        return transactions;
    }
    /** Method for daving the sales when there is a new transaction */
    public static void saveSales(List<Transaction> transactions) {
        try {
            File transactionsFile = new File(TRANSACTIONS_FILE);

            if (!transactionsFile.exists() && (transactions == null || transactions.isEmpty())) {
                System.out.println("[DEBUG] No transactions available for sales.xml");
                return;
            }

            List<Transaction> allTransactions = transactions;
            if (transactionsFile.exists()) {
                allTransactions.addAll(loadTransactions(transactionsFile));
            }

            // Process sales as before, using allTransactions
            generateSalesReport(allTransactions);

            System.out.println("[SERVER] sales.xml updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Method for saving sales to sales.xml */
    public static synchronized void generateSalesReport(List<Transaction> transactions) {
        try {
            File transactionsFile = new File(TRANSACTIONS_FILE);

            // Parse transactions.xml
            Document transactionsDoc = parseXml(transactionsFile);
            transactionsDoc.getDocumentElement().normalize();

            // Data structure: Year -> Month -> Day -> List of Transactions
            Map<String, Map<String, Map<String, List<Element>>>> salesData = new TreeMap<>();
            Map<String, Double> monthlyRevenue = new HashMap<>();
            Map<String, Double> yearlyRevenue = new HashMap<>();

            NodeList userList = transactionsDoc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Element userElement = (Element) userList.item(i);
                String username = userElement.getAttribute("username");

                NodeList transactionNodes = userElement.getElementsByTagName("transaction");

                for (int j = 0; j < transactionNodes.getLength(); j++) {
                    Element transaction = (Element) transactionNodes.item(j);

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

    /** Method for saving the favorites to favorites.xml */
    public static void saveFavorites(String username, List<Favorites> newFavorites) throws Exception {
        System.out.println("[SERVER] Received favorites XML from user: " + username);

        File file = new File(FAVORITE_FILE);
        Document doc;
        Element root;

        if (file.exists() && file.length() > 0) {
            doc = parseXml(file);
            root = doc.getDocumentElement();
        } else {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            root = doc.createElement("favorites");
            doc.appendChild(root);
        }

        // Remove existing favorites for the user if they already exist
        NodeList users = root.getElementsByTagName("user");
        for (int i = 0; i < users.getLength(); i++) {
            Element userElement = (Element) users.item(i);
            if (userElement.getAttribute("username").equals(username)) {
                root.removeChild(userElement);
                break;  // Remove only the first occurrence
            }
        }
        // Create a new user element
        Element userElement = doc.createElement("user");
        userElement.setAttribute("username", username);

        // Add favorite books under this user
        for (Favorites favorite : newFavorites) {
            Element bookElement = doc.createElement("book");

            appendChildElement(doc, bookElement, "title", favorite.getTitle());
            appendChildElement(doc, bookElement, "author", favorite.getAuthor());
            appendChildElement(doc, bookElement, "year", favorite.getYear());
            appendChildElement(doc, bookElement, "stock", String.valueOf(favorite.getStock()));

            userElement.appendChild(bookElement);
        }
        root.appendChild(userElement);

        saveXmlDocument(doc, file);
        System.out.println("[SERVER] Favorites updated successfully for user: " + username);
    }

    public static List<Favorites> loadFavorites(File file) {
        List<Favorites> favorites = new ArrayList<>();

        if (!file.exists()) {
            System.err.println("[DEBUG] Favorites file not found: " + file.getAbsolutePath());
            return favorites;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Element userElement = (Element) userList.item(i);
                String username = userElement.getAttribute("username");

                NodeList favoriteNodes = userElement.getElementsByTagName("favorite");

                for (int j = 0; j < favoriteNodes.getLength(); j++) {
                    Element favoriteElement = (Element) favoriteNodes.item(j);

                    String title = favoriteElement.getElementsByTagName("title").item(0).getTextContent();
                    String author = favoriteElement.getElementsByTagName("author").item(0).getTextContent();
                    String year = favoriteElement.getElementsByTagName("year").item(0).getTextContent();
                    int stock = 0;

                    // Handle optional stock field
                    Node stockNode = favoriteElement.getElementsByTagName("stock").item(0);
                    if (stockNode != null) {
                        stock = Integer.parseInt(stockNode.getTextContent());
                    }

                    favorites.add(new Favorites(username, title, author, year, stock));
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load favorites.xml: " + e.getMessage());
        }

        return favorites;
    }

    /** Method for adding the favorites list to the xml for a specified username */
    public static boolean addFavoritesToXML(String username, Favorites addFavorite) {
        try {
            File file = new File(FAVORITE_FILE);
            Document doc;
            Element root;

            if (file.exists() && file.length() > 0) {
                doc = parseXml(file);
                root = doc.getDocumentElement();
            } else {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                doc = builder.newDocument();
                root = doc.createElement("favorites");
                doc.appendChild(root);
            }

            // Find user node
            NodeList users = root.getElementsByTagName("user");
            Element userElement = null;

            for (int i = 0; i < users.getLength(); i++) {
                Element user = (Element) users.item(i);
                if (user.getAttribute("username").equals(username)) {
                    userElement = user;
                    break;
                }
            }

            // If user doesn't exist, create a new one
            if (userElement == null) {
                userElement = doc.createElement("user");
                userElement.setAttribute("username", username);
                root.appendChild(userElement);
            }

            // Create new book element
            Element bookElement = doc.createElement("book");
            appendChildElement(doc, bookElement, "title", addFavorite.getTitle());
            appendChildElement(doc, bookElement, "author", addFavorite.getAuthor());
            appendChildElement(doc, bookElement, "year", addFavorite.getYear());
            appendChildElement(doc, bookElement, "stock", String.valueOf(addFavorite.getStock()));

            userElement.appendChild(bookElement);

            saveXmlDocument(doc, file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Method for deleting the favorites list to the xml for a specified username */
    public static boolean deleteFavoritesToXML(String username, Favorites deleteFavorite) {
        try {
            File file = new File(FAVORITE_FILE);
            if (!file.exists()) {
                return false;
            }

            Document doc = parseXml(file);
            Element root = doc.getDocumentElement();

            NodeList users = root.getElementsByTagName("user");
            Element userElement = null;

            // Find user node
            for (int i = 0; i < users.getLength(); i++) {
                Element user = (Element) users.item(i);
                if (user.getAttribute("username").equals(username)) {
                    userElement = user;
                    break;
                }
            }

            // If user not found, return false
            if (userElement == null) {
                return false;
            }

            // Find the book to remove
            NodeList books = userElement.getElementsByTagName("book");
            for (int i = 0; i < books.getLength(); i++) {
                Element book = (Element) books.item(i);
                String title = book.getElementsByTagName("title").item(0).getTextContent();

                if (title.equals(deleteFavorite.getTitle())) {
                    userElement.removeChild(book);
                    saveXmlDocument(doc, file);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}

package client.buyer.model;

import org.w3c.dom.*;
import utilities.Cart;
import utilities.User;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BookStoreAddToCartModel {
    private static String LOGGED_IN_USER = User.getLoggedInUsername();
    private List<Cart> books;
    private List<Cart> cart;

    public BookStoreAddToCartModel() {
        this.books = parseBooks();
        this.cart = new ArrayList<>();
    }

    public static List<Cart> parseBooks() {
        List<Cart> books = new ArrayList<>();
        String filePath = "res/books.xml";  // Use a relative path

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Error: books.xml not found at " + file.getAbsolutePath());
            return books; // Return empty list if file is missing
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("book");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String title = element.getElementsByTagName("Title").item(0).getTextContent();
                    int stock = Integer.parseInt(element.getElementsByTagName("Stock").item(0).getTextContent());
                    double price = Double.parseDouble(element.getElementsByTagName("Price").item(0).getTextContent());
                    books.add(new Cart(title, stock, price));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }

    public void addToCart(String title, int quantity) {
        Cart book = getBookByTitle(title);
        if (book != null) {
            boolean updated = false;
            for (Cart item : cart) {
                if (item.getTitle().equals(book.getTitle())) {
                    item.setQuantity(item.getQuantity() + quantity);  // Update quantity
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                cart.add(new Cart(book.getTitle(), quantity, book.getPrice()));
            }
        }
    }

    public void removeFromCart(int index) {
        if (index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
    }

    public void saveCart(String filePath) {
        LOGGED_IN_USER = User.getLoggedInUsername();
        // Load the existing XML
        Document doc = null;
        File cartFile = new File(filePath);

        if (cartFile.exists()) {
            try {
                // Parse the existing XML file
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                doc = builder.parse(cartFile);
                doc.getDocumentElement().normalize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // Create a new XML document structure if the file doesn't exist
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                doc = builder.newDocument();
                Element rootElement = doc.createElement("cart");
                doc.appendChild(rootElement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (doc != null) {
            try {
                // Create or get the root element
                Element rootElement = doc.getDocumentElement();

                // Find if the user already exists in the cart XML
                NodeList userNodes = rootElement.getElementsByTagName("user");
                Element userElement = null;

                for (int i = 0; i < userNodes.getLength(); i++) {
                    userElement = (Element) userNodes.item(i);
                    String username = userElement.getElementsByTagName("username").item(0).getTextContent();
                    if (username.equals(LOGGED_IN_USER)) {
                        break;
                    } else {
                        userElement = null;
                    }
                }

                // If the user does not exist in the cart, create a new user element
                if (userElement == null) {
                    userElement = doc.createElement("user");
                    Element usernameElement = doc.createElement("username");
                    usernameElement.appendChild(doc.createTextNode(LOGGED_IN_USER));
                    userElement.appendChild(usernameElement);
                    rootElement.appendChild(userElement);
                }

                // For each book in the cart, either update the quantity or add a new entry under the user
                for (Cart item : cart) {
                    boolean updated = false;

                    // Check if the book already exists under this user
                    NodeList bookNodes = userElement.getElementsByTagName("book");
                    for (int i = 0; i < bookNodes.getLength(); i++) {
                        Element bookElement = (Element) bookNodes.item(i);
                        String title = bookElement.getElementsByTagName("Title").item(0).getTextContent();
                        if (title.equals(item.getTitle())) {
                            // Update the quantity if the book already exists
                            Element quantityElement = (Element) bookElement.getElementsByTagName("Quantity").item(0);
                            int existingQuantity = Integer.parseInt(quantityElement.getTextContent());
                            quantityElement.setTextContent(String.valueOf(existingQuantity + item.getQuantity()));
                            updated = true;
                            break;
                        }
                    }

                    // If the book doesn't exist, add a new book entry
                    if (!updated) {
                        Element bookElement = doc.createElement("book");

                        Element titleElement = doc.createElement("Title");
                        titleElement.appendChild(doc.createTextNode(item.getTitle()));
                        bookElement.appendChild(titleElement);

                        Element quantityElement = doc.createElement("Quantity");
                        quantityElement.appendChild(doc.createTextNode(String.valueOf(item.getQuantity())));
                        bookElement.appendChild(quantityElement);

                        userElement.appendChild(bookElement);
                    }
                }

                // Write the changes to the file
                try (FileWriter writer = new FileWriter(cartFile)) {
                    writer.write(docToString(doc));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String docToString(Document doc) {
        try {
            // Convert document to a string
            StringWriter writer = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void sendCartToServer(String filePath) {
        try (Socket socket = new Socket("localhost", 2000);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            // Generate XML content for the current cart
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element rootElement = doc.createElement("cart");
            doc.appendChild(rootElement);

            // For each book in the cart, create the appropriate XML structure
            for (Cart item : cart) {
                Element bookElement =doc.createElement("book");

                Element titleElement = doc.createElement("Title");
                titleElement.appendChild(doc.createTextNode(item.getTitle()));
                bookElement.appendChild(titleElement);

                Element quantityElement = doc.createElement("Quantity");
                quantityElement.appendChild(doc.createTextNode(String.valueOf(item.getQuantity())));
                bookElement.appendChild(quantityElement);

                rootElement.appendChild(bookElement);
            }
            // Convert the document to a string to send
            String cartXml = docToString(doc);

            // Send the cart to the server
            output.writeObject("PROCESS_CART");
            output.writeObject(LOGGED_IN_USER);
            output.writeObject(cartXml);

            // Receive the total price from the server
            double total = (double) input.readObject();
            System.out.println("[SERVER] Total Price: " + total);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a book by its title
     */
    public Cart getBookByTitle(String title) {
        for (Cart book : books) {
            if (book.getTitle().equals(title)) {
                return book;
            }
        }
        return null;
    }

    /**
     * Returns the list of books
     */
    public List<Cart> getBooks() {
        return books;
    }

    /**
     * Returns the list of cart items.
     */
    public List<Cart> getCart() {
        return cart;
    }


}

package client.buyer.model;

import client.buyer.view.BookStoreLoginView;
import utilities.User;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

public class BookStoreLoginModel {
    private List<User> users;
    private static final String ACCOUNTS_FILE = "res/client/buyer/accounts.xml";
    BookStoreLoginView view;

    public BookStoreLoginModel() {
        users = new ArrayList<>();
        loadUsersFromXML();

        this.view.clearFields();
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean userExists(String username) {
        return users.stream().anyMatch(user -> user.getUsername().equals(username));
    }
    public boolean validateLogin(String username, String password, String accountType) {
        String serverResponse = ServerConnection.validateUser(username, password, accountType);

        System.out.println("[DEBUG] Server Response: " + serverResponse);  // Debugging

        if (serverResponse.equals("VALID_BUYER")) {
            System.out.println("[UserModel] User logged in: " + username + " as " + accountType);
            return true;
        } else {
            System.out.println("[UserModel] Failed login attempt: " + username);
            return false;
        }
    }

    public void addUserLocally(User user) {
        if (!userExists(user.getUsername())) {
            users.add(user);
            saveUserToXML(user);
            System.out.println("[UserModel] User saved locally: " + user.getUsername());
        }
    }

    private void loadUsersFromXML() {
        try {
            File xmlFile = new File(ACCOUNTS_FILE);
            if (!xmlFile.exists()) return;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("user");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String username = getElementText(element, "username");
                String password = getElementText(element, "password");
                String accountType = getElementText(element, "accountType");
                users.add(new User(username, password, accountType));
            }
        } catch (Exception e) {
            System.err.println("[UserModel] Error loading users from XML: " + e.getMessage());
        }
    }

    private void saveUserToXML(User user) {
        try {
            File xmlFile = new File(ACCOUNTS_FILE);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            Element rootElement;

            if (xmlFile.exists() && xmlFile.length() > 0) {
                document = builder.parse(xmlFile);
                document.getDocumentElement().normalize();
                rootElement = document.getDocumentElement();
            } else {
                document = builder.newDocument();
                rootElement = document.createElement("users");
                document.appendChild(rootElement);
            }

            Element userElement = document.createElement("user");
            rootElement.appendChild(userElement);

            createElementWithText(document, userElement, "username", user.getUsername());
            createElementWithText(document, userElement, "password", user.getPassword());
            createElementWithText(document, userElement, "accountType", user.getAccountType());

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(XML_FILE)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }

    private void createElementWithText(Document document, Element parent, String tagName, String text) {
        Element element = document.createElement(tagName);
        element.appendChild(document.createTextNode(text));
        parent.appendChild(element);
    }
    // Method to get the logged-in user's username
    public String getLoggedInUser() {
        return User.getLoggedInUsername();  // Fetch the logged-in username
    }
}

package client.owner.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import server.ServerConnection;
import utilities.User;

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

public class BookOwnerLoginModel {
    private List<User> bookowner;
    private static final String FILE = "res/accounts.xml";
    private static final String SERVER_ADDRESS = "localhost";
    private static final  int PORT = 2000;

    public BookOwnerLoginModel(){
        bookowner = new ArrayList<>();
        loadBookOwnerFromXML();

    }

    public boolean bookOwnerExists(String username){
        return bookowner.stream().anyMatch(user -> user.getUsername().equals(username));
    }
    public boolean validateLogin(String username, String password, String accountType){
        String serverResponse = ServerConnection.validateUser(username, password, accountType);
        System.out.println("[DEBUG] Server Response: " + serverResponse);

        if (serverResponse.equals("VALID_BOOKOWNER")){
            System.out.println("[BookOwnerModelLoginModel] Book owner logged in: " + username);
            return true;
        }else {
            System.out.println("[BookOwnerLoginModel Failed login attempt: " + username);
            return false;
        }
    }

    public void addBookOwner(UserUtility user) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeObject("ADD_USER");

            // Convert UserUtility to XML string
            String userXml = convertUserToXML(user);
            output.writeObject(userXml); // Send XML string

            String response = (String) input.readObject();

            if ("SUCCESS".equals(response)) {
                System.out.println("[Client] User added successfully.");
            } else if ("USER_EXISTS".equals(response)) {
                System.out.println("[Client] User already exists.");
            } else if ("ERROR_PARSING_XML".equals(response)) { // Handle XML parsing errors
                System.out.println("[Client] Error parsing XML on server.");
            } else {
                System.out.println("[Client] Error adding user: " + response);
            }

        } catch (Exception e) { // Catch the exception
            e.printStackTrace();
        }
    }

    private String convertUserToXML(UserUtility user) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("user");

        Element username = doc.createElement("username");
        username.appendChild(doc.createTextNode(user.getUsername()));
        root.appendChild(username);

        Element password = doc.createElement("password");
        password.appendChild(doc.createTextNode(user.getPassword()));  // Send hashed password
        root.appendChild(password);

        Element accountType = doc.createElement("accountType");
        accountType.appendChild(doc.createTextNode(user.getAccountType()));
        root.appendChild(accountType);

        doc.appendChild(root);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.getBuffer().toString();
    }

    private void loadBookOwnerFromXML() {
        try {
            File xmlFile = new File(FILE);
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
                bookowner.add(new UserUtility(username, password, accountType));
            }
        } catch (Exception e) {
            System.err.println("[UserModel] Error loading users from XML: " + e.getMessage());
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

}

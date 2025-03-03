package client.owner.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.Socket;

import static utilities.XmlUtils.appendChildElement;
import static utilities.XmlUtils.saveXmlDocument;

public class BookOwnerLoginModel {
    private static final String LOGIN_FILE = "res/client/owner/accounts.xml";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2000;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket;

    public BookOwnerLoginModel(){
        initializeConnection();
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

    /** Create a xml file for the given login */
    public void createLoginXml(String username, String password, String accountType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("accounts");
            doc.appendChild(root);

            Element userElement = doc.createElement("user");
            appendChildElement(doc, userElement, "Username", username);
            appendChildElement(doc, userElement, "Password", password);
            appendChildElement(doc, userElement, "AccountType", accountType);

            root.appendChild(userElement);

            saveXmlDocument(doc, new File(LOGIN_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean validateLogin(){
        try {
            File loginFile = new File(LOGIN_FILE);
            if (!loginFile.exists()) {
                System.err.println("[ERROR] Login file not found");
                return false;
            }

            outputStream.writeObject("VALIDATE_ADMIN");
            outputStream.writeObject(loginFile);
            outputStream.flush();

            String response = (String) inputStream.readObject();
            return "SUCCESS".equals(response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}

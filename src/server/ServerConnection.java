package server;

import utilities.User;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerConnection {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 2000;


    /** Fetch users */
    public static List<User> fetchUsers() {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeObject("FETCH_USERS");
            return (List<UserUtility>) input.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Save user */
    public static boolean saveUser(UserUtility user) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeObject("ADD_USER");
            output.writeObject(user);
            return "SUCCESS".equals(input.readObject());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Validate user */
    public static String validateUser(String username, String password, String accountType) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeObject("VALIDATE_USER");
            output.writeObject(username);
            output.writeObject(password);
            output.writeObject(accountType);  // Ensure this is sent correctly

            return (String) input.readObject(); // Returns "VALID_BUYER", "VALID_BOOKOWNER", or "INVALID"

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    /** Functional interface for server actions. */
    @FunctionalInterface
    private interface ServerAction<T> {
        T perform(ObjectOutputStream output, ObjectInputStream input) throws IOException, ClassNotFoundException;
    }
}

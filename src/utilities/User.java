package utilities;

import java.io.*;

public class User implements Serializable {
    private String username;
    private String password;
    private String accountType;

    // Static variable to store the logged-in username
    private static String loggedInUsername;

    private static final ThreadLocal<String> loggedInUser = new ThreadLocal<>(); //added for threads compatibility, pacomment out na lang pag ayaw

    public User(String username, String password, String accountType) {
        this.username = username;
        this.password = password;
        this.accountType = accountType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccountType() { return accountType; }

    // Thread-local methods
    public static void setLoggedInUsername(String username) {
        loggedInUser.set(username);
    }

    public static String getLoggedInUsername() {
        return loggedInUser.get();
    }

    public static void clearLoggedInUsername() {
        loggedInUser.remove();
    }
    // Generic serialization method
    public static <T> void serialize(T object, String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(object);
            System.out.println("[Serialization] Object serialized to: " + filePath);
        } catch (IOException e) {
            System.err.println("[Serialization] Error serializing object: " + e.getMessage());
        }
    }

    // Generic deserialization method
    public static <T> T deserialize(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            T object = (T) in.readObject();
            System.out.println("[Deserialization] Object deserialized from: " + filePath);
            return object;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Deserialization] Error deserializing object: " + e.getMessage());
            return null;
        }
    }


}

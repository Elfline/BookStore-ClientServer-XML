package server;

import utilities.User;
import java.util.List;

public class ServerConnection {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 2000;



    /** Fetch users */
    public static List<User> fetchUsers() {
        return ServerXml.loadUsersFromXML();
    }

    /** Save a new user to the XML file */
    public static boolean saveUser(User user) {
         try {
            List<User> users = ServerXml.loadUsersFromXML();
            users.add(user);
            ServerXml.saveUsersToXML(users);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Validate user */
    public static String validateUser(String username, String password, String accountType) {
        List<User> users = ServerXml.loadUsersFromXML();

        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password) &&
                    user.getAccountType().equalsIgnoreCase(accountType)) {

                if ("BUYER".equalsIgnoreCase(accountType)) {
                    return "VALID_BUYER";
                } else if ("BOOKOWNER".equalsIgnoreCase(accountType)) {
                    return "VALID_BOOKOWNER";
                }
            }
        }
        return "INVALID";
    }


}

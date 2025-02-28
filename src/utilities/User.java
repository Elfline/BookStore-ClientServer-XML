package utilities;

public class User {
    private String username;
    private String password;
    private String accountType;
    private static String loggedInUsername;  // Static variable to store the logged-in username

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

}

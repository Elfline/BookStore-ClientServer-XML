package server;

import utilities.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int PORT = 2000;
    private static final String BOOKS_FILE = "res/server/books.xml";
    private static final String TRANSACTIONS_FILE = "res/server/transactions.xml";
    private static final String FAVORITE_FILE = "res/server/favorites.xml";
    private static final String SALES_FILE = "res/server/sales.xml";
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final Map<String, Socket> activeUsers = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Socket> clientSockets = Collections.synchronizedMap(new HashMap<>());
    private static Socket adminSocket;

    public static void main(String[] args) {
        ServerXml.loadUsers();
        System.out.println("[SERVER] Server is running on port " + PORT + "...");try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] New client connected: " + clientSocket.getInetAddress());
                executorService.execute(new ClientHandler(clientSocket)); // Assign new client to a thread
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Error starting server: " + e.getMessage());
        } finally {
            shutdownExecutor();
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
                System.out.println("[SERVER] Handling client: " + clientSocket.getInetAddress());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

                while (true) {
                    try {
                        String command = (String) input.readObject();

                        switch (command) {
                            case "VALIDATE_ADMIN":
                                File adminXml = (File) input.readObject();
                                boolean validAdmin = handleAdminValidation(adminXml);
                                output.writeObject(validAdmin ? "SUCCESS" : "ERROR");
                                System.out.println(validAdmin ? "[SERVER] Admin validated successfully" : "[DEBUG] Invalid admin");
                                break;
                            case "ADD_USER":
                                File userXml = (File) input.readObject();
                                User user = (User) ServerXml.loadUsersFromFile(userXml);
                                boolean userAdded = ServerXml.saveUsers(user);
                                output.writeObject(userAdded ? "SUCCESS" : "ERROR");
                                if (userAdded) {
                                    System.out.println("[SERVER] User: " + user.getUsername() +" is successfully added");
                                }
                                break;
                            case "VALIDATE_USER":
                                File UserXml = (File) input.readObject();
                                boolean validUser = handleUserValidation(UserXml);
                                output.writeObject(validUser ? "SUCCESS" : "ERROR");
                                System.out.println(validUser ? "[SERVER] User validated successfully." : "[SERVER] Invalid user credentials.");
                                break;
                            case "ADD_TO_CART":
                                String username = (String) input.readObject(); // Read client's username
                                File cartXml = (File) input.readObject();
                                System.out.println("[SERVER] Received cart.xml from user: " + username);
                                ServerXml.saveCart(username, cartXml);
                                sendTransactionsToUsers(username);
                                System.out.println("[SERVER] Sent total amount to user: " + username);
                                break;
                            case "FETCH_TRANSACTION":
                                String loggedUser = (String) input.readObject(); // Read client's username
                                sendTransactionsToUsers(loggedUser);
                                System.out.println("[SERVER] Sent transaction history for: " + loggedUser);
                                break;
                            case "ADD_FAVORITES":
                                String usernameFavorites = (String) input.readObject();
                                File favoritesXml = (File) input.readObject();
                                System.out.println("[SERVER] Received favorites.xml from user: " + usernameFavorites);
                                List<Favorites> favoritesList = ServerXml.loadFavorites(favoritesXml);
                                ServerXml.saveFavorites(usernameFavorites, favoritesList);
                                sendFavoritesToUsers(usernameFavorites);
                                break;
                            case "FETCH_FAVORITES":
                                String userFavorite = (String) input.readObject();
                                sendFavoritesToUsers(userFavorite);
                                System.out.println("[SERVER] Favorites sent to " + userFavorite);
                                break;
                            case "FETCH_SALES_REPORT":
                                sendSales();
                                System.out.println("[SERVER] sales.xml sent");
                                break;
                            case "ADD_BOOK":
                                File addBook = (File) input.readObject();
                                Book addedBooks = (Book) ServerXml.loadBooks(addBook);
                                boolean addSuccess = addBookToXML(addedBooks);
                                output.writeObject(addSuccess ? "SUCCESS" : "ERROR");
                                if (addSuccess) {
                                    System.out.println("[SERVER] Book '" + addedBooks.getTitle() +"' is successfully added");
                                }
                                sendBooksToUsers();
                                break;
                            case "DELETE_BOOK":
                                File deleteBook = (File) input.readObject();
                                Book deletedBook = (Book) ServerXml.loadBooks(deleteBook);
                                boolean deleteSuccess = deleteBookToXML(deletedBook);
                                output.writeObject(deleteSuccess ? "SUCCESS" : "ERROR");
                                if (deleteSuccess) {
                                    System.out.println("[SERVER] Book '" + deletedBook.getTitle() +"' is successfully deleted");
                                }
                                sendBooksToUsers();
                                break;
                            case "UPDATE_BOOK":
                                File updateBook = (File) input.readObject();
                                Book updateBooks = (Book) ServerXml.loadBooks(updateBook);
                                boolean updateSuccess = updateBookToXML(updateBooks);
                                output.writeObject(updateSuccess ? "SUCCESS" : "ERROR");
                                if (updateSuccess) {
                                    System.out.println("[SERVER] Book '" + updateBooks.getTitle() +"' is successfully updated");
                                }
                                sendBooksToUsers();
                                break;
                            case "REQUEST_BOOKS":
                                sendBooksToUsers();
                                System.out.println("[SERVER] Books sent");
                                break;
                            case "LOGOUT":
                                try {
                                    File logoutXml = (File) input.readObject();
                                    User logoutUser = ServerXml.loadUserForLoggedOut(logoutXml);
                                    boolean isLoggedOut = handleUserLogout(logoutUser);
                                    output.writeObject(isLoggedOut ? "SUCCESS" : "ERROR");
                                    if (isLoggedOut) {
                                        System.out.println("[Server] Client logged out: " + logoutUser.getUsername() + "is successfully logged out");
                                    } else {
                                        System.out.println("[Server] Client: " + logoutUser.getUsername() + "isn't logged out");
                                    }
                                } catch (Exception e) {
                                    System.err.println("[Server] Error handling logout.xml: " + e.getMessage());
                                } finally {
                                    try {
                                        clientSocket.close(); // Close socket after logout.xml
                                    } catch (IOException e) {
                                        System.err.println("[Server] Error closing client socket: " + e.getMessage());
                                    }
                                }
                                return; // Exit the loop and end the thread
                            default:
                                System.out.println("[Server] Unknown command received: " + command);
                        }
                    } catch (EOFException e) {
                        break; // Exit loop when client disconnects
                    } catch (Exception e) {
                        System.err.println("[DEBUG] Error handling client request: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.out.println("[Server] Client error: " + clientSocket.getInetAddress());
                e.printStackTrace(); // Print detailed error for debugging
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("[SERVER] Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    /** Shuts down the thread executor service */
    private static void shutdownExecutor() {
        System.out.println("[SERVER] Shutting down executor.....");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("[SERVER] Forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("[SERVER] Shutdown interrupted: " + e.getMessage());
            executorService.shutdownNow();
        }
    }

    /** Validates the admin */
    private static boolean handleAdminValidation(File admin) {
        List<User> serverUsers = ServerXml.loadUsers(); // Load server's copy
        List<User> clientUsers = ServerXml.loadUsersFromFile(admin); // Load client's copy

        if (clientUsers.isEmpty()) {
            return false;
        }

        for (User clientUser : clientUsers) {
            for (User serverUser : serverUsers) {
                if (serverUser.getUsername().equals(clientUser.getUsername()) &&
                        serverUser.getPassword().equals(clientUser.getPassword()) &&
                        "BOOKOWNER".equalsIgnoreCase(serverUser.getAccountType())) {
                    return true; // Admin match found
                }
            }
        }
        return false; // No matching admin found
    }


    /** Validates the user */
    private static boolean handleUserValidation(File user) {
        List<User> serverUsers = ServerXml.loadUsers(); // Load server's copy
        List<User> clientUsers = ServerXml.loadUsersFromFile(user); // Load client's copy

        if (clientUsers.isEmpty()) {
            return false;
        }

        for (User clientUser : clientUsers) {
            for (User serverUser : serverUsers) {
                if (serverUser.getUsername().equals(clientUser.getUsername()) &&
                        serverUser.getPassword().equals(clientUser.getPassword()) &&
                        "BUYER".equalsIgnoreCase(serverUser.getAccountType())) {
                    return true; // User match found
                }
            }
        }
        return false; // No matching user found
    }

    /** Handles the user log out */
    private static boolean handleUserLogout(User user) {
        if (user == null || user.getUsername() == null) {
            System.err.println("[DEBUG] Invalid logout request: User data is missing");
            return false;
        }
        synchronized (activeUsers) {
            if (activeUsers.containsKey(user.getUsername())) {
                try {
                    activeUsers.get(user.getUsername()).close(); // Close the socket
                } catch (IOException e) {
                    System.err.println("[SERVER] Error closing socket for " + user.getUsername() + ": " + e.getMessage());
                }
                activeUsers.remove(user.getUsername()); // Remove user from active list
                System.out.println("[SERVER] User: " + user.getUsername() + " logged out successfully");
                return true;
            } else {
                System.err.println("[DEBUG] Logout failed: User " + user.getUsername() + " not found in active users");
                return false;
            }
        }
    }


    /** Method for adding the book received from the BookOwnerAddBookModel */
    private static boolean addBookToXML(Book book) {
        try {
            List<Book> books = ServerXml.loadBooks(new File(BOOKS_FILE));
            books.add(book);
            ServerXml.saveBooks(books, new File(BOOKS_FILE));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Method for deleting the book */
    private static boolean deleteBookToXML(Book book) {
        try {
            List<Book> books = ServerXml.loadBooks(new File(BOOKS_FILE));
            books.add(book);
            ServerXml.saveBooks(books, new File(BOOKS_FILE));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Method for updating the books*/
    private static boolean updateBookToXML(Book updatedBook) {
        try {
            List<Book> books = ServerXml.loadBooks(new File(BOOKS_FILE));
            boolean updated = false;

            for (Book book : books) {
                if (book.getTitle().equalsIgnoreCase(updatedBook.getTitle())) {
                    book.setStock(updatedBook.getStock());
                    book.setPrice(updatedBook.getPrice());
                    updated = true;
                    break;
                }
            }

            if (updated) {
                ServerXml.saveBooks(books, new File(BOOKS_FILE));
            }
            return updated;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Method for sending the books.xml to all users */
    private static void sendBooksToUsers() {
        File booksFile = new File(BOOKS_FILE);

        if (!booksFile.exists()) {
            System.err.println("[DEBUG] books.xml not found.");
            return;
        }

        for (Map.Entry<String, Socket> entry : clientSockets.entrySet()) {
            try {
                ObjectOutputStream output = new ObjectOutputStream(entry.getValue().getOutputStream());

                output.writeObject("UPDATE_BOOKS");
                output.writeObject(booksFile);
                output.flush();

                System.out.println("[SERVER] Sent books.xml to user: " + entry.getKey());
            } catch (IOException e) {
                System.err.println("[DEBUG] Error sending books.xml to " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    /** Method for sending transactions.xml to a specified user */
    private static synchronized void sendTransactionsToUsers(String username) {
        File transactionFile = new File(TRANSACTIONS_FILE);

        if (!transactionFile.exists()) {
            System.err.println("[DEBUG] transactions.xml not found.");
            return;
        }

        try {
            List<Transaction> allTransactions = ServerXml.loadTransactions(transactionFile);
            List<Transaction> userTransactions = new ArrayList<>();

            // Filter transactions for the given username
            for (Transaction t : allTransactions) {
                if (t.getUsername().equals(username)) {
                    userTransactions.add(t);
                }
            }
            if (userTransactions.isEmpty()) {
                System.out.println("[DEBUG] No transactions found for user: " + username);
                return;
            }

            // Create temporary XML file for the specified user's transactions
            File userTransactionFile = new File ("res/server/temp_transactions_" +username+".xml");
            ServerXml.saveTransactions(userTransactions, userTransactionFile);

            ObjectOutputStream output = new ObjectOutputStream(clientSockets.get(username).getOutputStream());
            output.writeObject("UPDATE_TRANSACTIONS");
            output.writeObject(userTransactionFile);
            output.flush();

            System.out.println("[SERVER] Sent transactions.xml to user: " + username);

        } catch (IOException e) {
            System.err.println("[DEBUG] Error sending transactions.xml to " + username + ": " + e.getMessage());
        }
    }
    /** Method for sending transactions.xml to specified user */
    private static synchronized void sendFavoritesToUsers(String username) {
        File favoriteFile = new File(FAVORITE_FILE);

        if (!favoriteFile.exists()) {
            System.err.println("[DEBUG] favorites.xml not found.");
            return;
        }

        try {
            List<Favorites> allFavorites = ServerXml.loadFavorites(favoriteFile);
            List<Favorites> userFavorites = new ArrayList<>();

            // Filter favorites for the given username
            for (Favorites f : allFavorites) {
                if (f.getUser().equals(username)) {
                    userFavorites.add(f);
                }
            }

            if (userFavorites.isEmpty()) {
                System.out.println("[SERVER] No favorites found for user: " + username);
                return;
            }

            // Create a temporary XML file for the user's favorites
            File userFavoriteFile = new File("res/server/temp_favorites_" + username + ".xml");
            ServerXml.saveFavorites(userFavorites, userFavoriteFile);

            ObjectOutputStream output = new ObjectOutputStream(clientSockets.get(username).getOutputStream());
            output.writeObject("UPDATE_FAVORITES");
            output.writeObject(userFavoriteFile);
            output.flush();

            System.out.println("[SERVER] Sent favorites.xml to user: " + username);
        } catch (IOException e) {
            System.err.println("[DEBUG] Error sending favorites.xml to " + username + ": " + e.getMessage());
        }
    }


    /** Method for sending sales.xml to the admin */
    private static void sendSales() {
        File salesFile = new File(SALES_FILE);

        if (!salesFile.exists()) {
            System.err.println("[DEBUG] sales.xml not found.");
            return;
        }
        try {
            ObjectOutputStream output = new ObjectOutputStream(adminSocket.getOutputStream());
            output.writeObject("UPDATE_SALES");
            output.writeObject(salesFile);
            output.flush();

            System.out.println("[SERVER] Sent sales.xml to admin");
        } catch (IOException e) {
            System.err.println("[SERVER] Error sending sales.xml to admin: " + e.getMessage());
        }
    }
}




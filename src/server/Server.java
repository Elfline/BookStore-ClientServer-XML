package server;

import utilities.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 2000;
    private static final String BOOKS_FILE = "res/server/books.xml";
    private static final String TRANSACTIONS_FILE = "res/server/transactions.xml";
    private static final String FAVORITE_FILE = "res/server/favorites.xml";
    private static final String SALES_FILE = "res/server/sales.xml";
    private static final String ACCOUNTS_FILE = "res/server/accounts.xml";
    private static ExecutorService executor;
    private static Map<String, Socket> loggedInBookOwners = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        ServerXml.loadUsersFromXML();

        executor = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] Listening on port " + PORT);

            // Start a separate thread to handle automatic book owner login
            new Thread(() -> {
                while (true) {
                    try {
                        // Check if a book owner is already logged in (you'll need a way to track this)
                        boolean bookOwnerLoggedIn = isBookOwnerLoggedIn(); // Implement this method

                        if (!bookOwnerLoggedIn) {
                            // Find a book owner user
                            User bookOwner = findBookOwnerUser(); // Implement this method

                            if (bookOwner != null) {
                                // Simulate login
                                System.out.println("[Server] Automatically logging in book owner: " + bookOwner.getUsername());
                                try (Socket clientSocket = new Socket("localhost", PORT);
                                     ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                                     ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())) {

                                    output.writeObject("VALIDATE_USER");
                                    output.writeObject(bookOwner.getUsername());
                                    output.writeObject(bookOwner.getPassword());
                                    output.writeObject("BOOKOWNER"); // Or bookOwner.getAccountType()

                                    String response = (String) input.readObject();
                                    if (response.equals("VALID_BOOKOWNER")) {
                                        System.out.println("[Server] Automatic login successful for: " + bookOwner.getUsername());
                                        markBookOwnerAsLoggedIn(bookOwner, clientSocket); // Implement this
                                    } else {
                                        System.out.println("[Server] Automatic login failed for: " + bookOwner.getUsername() + ". Response: " + response);
                                    }
                                    clientSocket.close();
                                } catch (IOException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Thread.sleep(5000); // Check every 5 seconds (adjust as needed)

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] New client connected: " + clientSocket.getInetAddress());

                // Submit a task to the executor to handle the client in a separate thread
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Gracefully shut down the executor service
            if (executor != null) {
                executor.shutdown();
            }
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
                            case "ADD_USER":
                                File userXml = (File) input.readObject();
                                try {
                                    User user = parseUserFromXML(userXml);
                                    handleAddUser(user, output);
                                } catch (Exception e) {
                                    output.writeObject("ERROR_PARSING_XML"); // Send error message
                                    System.err.println("[DEBUG] Error parsing user XML: " + e.getMessage());
                                }
                                break;
                            case "VALIDATE_USER":
                                handleUserValidation(input, output);
                                break;
                            case "FETCH_USERS":
                                output.writeObject(ACCOUNTS_FILE);
                                System.out.println("[SERVER] accounts.xml sent ");
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
                            case "FETCH_SALES_REPORT":
                                sendSales(clientSocket);
                                System.out.println("[SERVER] sales.xml sent");
                                break;
                            case "ADD_BOOK":
                                File newBook = (File) input.readObject();
                                System.out.println("[SERVER] Received new book");
                                Book newBooks = (Book) ServerXml.loadBooks(newBook);
                                boolean success = addBookToXML(newBooks);
                                output.writeObject(success ? "SUCCESS" : "ERROR");
                                if (success) {
                                    System.out.println("[SERVER] Book '" + newBooks.getTitle() +"' has been added to books.xml");
                                }
                                break;
                            case "DELETE_BOOK":
                                File deleteBook = (File) input.readObject();
                                Book deleteBooks = (Book) ServerXml.loadBooks(deleteBook);
                                System.out.println("[CLIENT] Sending book for deletion: " + deleteBooks.getTitle());
                                boolean deleted = deleteBookFromXML(deleteBooks);
                                output.writeObject(deleted ? "SUCCESS" : "ERROR");
                                if (deleted) {
                                    System.out.println("[SERVER] Book '" + deleteBooks + "' has been successfully deleted to books.xml");
                                }
                                break;
                            case "UPDATE_BOOK":
                                File updateBook = (File) input.readObject();
                                Book updateBooks = (Book) ServerXml.loadBooks(updateBook);
                                boolean updateSuccess = updateBookInXML(updateBooks);
                                output.writeObject(updateSuccess ? "SUCCESS" : "ERROR");
                                if (updateSuccess) {
                                    System.out.println("[Server] Book '" + updateBooks.getTitle() +"' is successfully updated");
                                }
                                break;
                            case "REQUEST_BOOKS":
                                sendBooksToUsers();
                                System.out.println("[SERVER] Books sent");
                                break;
                            case "LOGOUT":
                                try {
                                    String string = (String) input.readObject(); // Get username from client

                                    if (loggedInBookOwners.containsKey(string)) {
                                        loggedInBookOwners.remove(string); // Remove from logged-in book owners
                                        logoutBookOwner(string);
                                        System.out.println("[Server] Book owner logged out: " + string);
                                    }

                                    output.writeObject("LOGOUT_SUCCESS"); // Inform client that logout was successful

                                    System.out.println("[Server] Client logged out: " + clientSocket.getInetAddress());

                                } catch (Exception e) {
                                    System.err.println("[Server] Error handling logout: " + e.getMessage());
                                } finally {
                                    try {
                                        clientSocket.close(); // Close socket after logout
                                    } catch (IOException e) {
                                        System.err.println("[Server] Error closing client socket: " + e.getMessage());
                                    }
                                }
                                return; // Exit the loop and end the thread
                            default:
                                System.out.println("[Server] Unknown command received: " + command);
                        }
                    } catch (EOFException e) {
                        break; // Exit loop
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                System.out.println("[Server] Client error: " + clientSocket.getInetAddress());
                e.printStackTrace(); // Print detailed error for debugging
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("[Server] Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    private static boolean isBookOwnerLoggedIn() {
        return !loggedInBookOwners.isEmpty();
    }

    private static User findBookOwnerUser() {
        for (User user : users) {
            if (user.getAccountType().equalsIgnoreCase("BOOKOWNER")) {
                return user;
            }
        }
        return null; // No book owner user found
    }

    private static void markBookOwnerAsLoggedIn(User bookOwner, Socket clientSocket) {
        loggedInBookOwners.put(bookOwner.getUsername(), clientSocket); // Track the book owner's connection
        System.out.println("[Server] Marked book owner as logged in: " + bookOwner.getUsername());
    }

    private static void logoutBookOwner(String username) {
        if (loggedInBookOwners.containsKey(username)) {
            loggedInBookOwners.remove(username);
            System.out.println("[Server] Book owner '" + username + "' logged out.");
        }
    }

    private static void handleAddUser(User user, ObjectOutputStream output) throws IOException {
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            output.writeObject("USER_EXISTS");
        } else {
            users.add(user);
            ServerXml.saveUsersToXML();
            System.out.println("[Server] User added: " + user.getUsername() + " as " + user.getAccountType());
            output.writeObject("SUCCESS");
        }
    }


    /**
     * Handles user validation (Login)
     */
    private static void handleUserValidation(ObjectInputStream input, ObjectOutputStream output) throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String password = (String) input.readObject();
        String accountType = (String) input.readObject();

        System.out.println("[DEBUG] Received Login Request: " + username + " | " + password + " | " + accountType);

        User user = users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user != null) {
            if (user.getAccountType().equalsIgnoreCase(accountType)) {
                System.out.println("[DEBUG] Login Successful for " + username);
                output.writeObject(accountType.equalsIgnoreCase("Buyer") ? "VALID_BUYER" : "VALID_BOOKOWNER");
            } else {
                System.out.println("[DEBUG] Account type mismatch for " + username);
                output.writeObject("INVALID");
            }
        } else {
            System.out.println("[DEBUG] User not found: " + username);
            output.writeObject("INVALID");
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
    private static boolean deleteBookFromXML(Book book) {
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
    private static boolean updateBookInXML(Book updatedBook) {
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

        try {
            for (Map.Entry<String, Socket> entry : loggedInBookOwners.entrySet()) {
                try {
                    Socket clientSocket = entry.getValue();
                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                    output.writeObject("UPDATE_BOOKS");
                    output.writeObject(booksFile);
                    output.flush();

                    System.out.println("[SERVER] Sent books.xml to user: " + entry.getKey());
                } catch (IOException e) {
                    System.err.println("[SERVER] Error sending books.xml to " + entry.getKey() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Method for sending transactions.xml to users */
    private static synchronized void sendTransactionsToUsers(String username) {
        File booksFile = new File(TRANSACTIONS_FILE);

        if (!booksFile.exists()) {
            System.err.println("[DEBUG] transactions.xml not found.");
            return;
        }

        try {
            for (Map.Entry<String, Socket> entry : loggedInBookOwners.entrySet()) {
                try {
                    Socket clientSocket = entry.getValue();
                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

                    output.writeObject("UPDATE_TRANSACTIONS");
                    output.writeObject(booksFile);
                    output.flush();

                    System.out.println("[SERVER] Sent transactions.xml to user: " + entry.getKey());
                } catch (IOException e) {
                    System.err.println("[SERVER] Error sending transactions.xml to " + entry.getKey() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Method for sending sales.xml to the admin */
    private static void sendSales(Socket clientSocket) {
        File salesFile = new File(SALES_FILE);

        if (!salesFile.exists()) {
            System.err.println("[DEBUG] sales.xml not found.");
            return;
        }
        try (
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
        ) {
            output.writeObject("UPDATE_SALES");
            output.writeObject(salesFile);
            output.flush();

            System.out.println("[SERVER] Sent sales.xml to admin");
        } catch (IOException e) {
            System.err.println("[SERVER] Error sending sales.xml to admin");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}




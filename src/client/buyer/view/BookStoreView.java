/**
 * BookStoreGUI represents the graphical user interface for the book store.
 * It allows users to search for books, view available books, manage their favorites, and access purchase history.
 * Algorithm:
 * 1. Initialize the JFrame and set its properties.
 * 2. Create and configure the search bar and book list.
 * 3. Add buttons for adding to cart, viewing purchase history, managing favorites, and showing all books.
 * 4. Provide list selection listener for book selection.
 * 5. Implement event listeners for navigation and interactions.
 * 6. Allow access to UI components for the controller.
 */

package client.buyer.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;



public class BookStoreView extends JFrame {

    private JTextField searchField;
    private JButton searchButton, addToCartButton, logOutButton, showAllBooksButton;
    private JPanel centerPanel, northPanel, westPanel;
    private CardLayout cardLayout;
    private JList<String> bookList;  // Changed from JTextArea to JList
    private DefaultListModel<String> bookListModel; // Stores book items

    public BookStoreView(String username) {
        setTitle("Welcome to the Book Store");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome to the Book Store!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(welcomeLabel, BorderLayout.NORTH);

        // North Panel (Search Bar)
        northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel listLabel = new JLabel("List of Available Books:");
        searchField = new JTextField(20);
        searchButton = new JButton("Search");

        northPanel.add(listLabel);
        northPanel.add(searchField);
        northPanel.add(searchButton);
        add(northPanel, BorderLayout.NORTH);

        // Center Panel (Book List)
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);

        bookListModel = new DefaultListModel<>();
        bookList = new JList<>(bookListModel);
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.setVisibleRowCount(10);
        JScrollPane scrollPane = new JScrollPane(bookList);
        centerPanel.add(scrollPane, "BookList");
        add(centerPanel, BorderLayout.CENTER);

        // Add List Selection Listener
        bookList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selectedBook = bookList.getSelectedValue();
                    if (selectedBook != null) {
                        JOptionPane.showMessageDialog(null, "Selected Book: " + selectedBook);
                    }
                }
            }
        });

        // West Panel (Buttons)
        westPanel = new JPanel();
        westPanel.setLayout(new GridLayout(3, 1));
        addToCartButton = new JButton("Add to Cart");
        showAllBooksButton = new JButton("Show All Books");
        logOutButton = new JButton("Log Out");

        westPanel.add(logOutButton);
        westPanel.add(addToCartButton);
        westPanel.add(showAllBooksButton);
        westPanel.add(logOutButton);
        add(westPanel, BorderLayout.WEST);

        setSize(700, 700);
        setLocationRelativeTo(null);
        setVisible(true);

    }

    public void addSearchButtonListener(ActionListener listener) {
        searchButton.addActionListener(listener);
    }

    public void addAddToCartListener(ActionListener listener) {
        addToCartButton.addActionListener(listener);
    }

    public void addShowAllBooksListener(ActionListener listener) {
        showAllBooksButton.addActionListener(listener);
    }

    public void addLogOutButtonListener(ActionListener listener) {
        logOutButton.addActionListener(listener);
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public void clearBookList() {
        bookListModel.clear();
    }

    public void addBookToList(String book) {
        bookListModel.addElement(book);
    }
}
package client.buyer.view;

import client.buyer.controller.BookStoreBookDetailsController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class BookStoreBookDetailsView extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JPanel bookInfo,bookList, infoLabels;
    private JLabel title, author, genre, year, price, stock;
    private JButton backButton;
    private CardLayout cardLayout;
    private Container content;
    BookStoreBookDetailsController controller;
    BookStoreView bookStoreView;

    public BookStoreBookDetailsView() {

        setTitle("Book List");
        setSize(600, 400);
        setLocationRelativeTo(null);

        content = getContentPane();
        cardLayout = new CardLayout();
        content.setLayout(cardLayout);

        // Book list panel
        bookList = new JPanel(new BorderLayout());
        String[] columns = {"Book List"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Makes the table non-editable
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(bookList, "main");

        // Book Info
        bookInfo = new JPanel(new BorderLayout());
        infoLabels = new JPanel(new GridLayout(6, 2)); // Panel for labels and values
        title = new JLabel("Title:");
        author = new JLabel("Author:");
        genre = new JLabel("Genre:");
        year = new JLabel("Year:");
        price = new JLabel("Price:");
        stock = new JLabel("Stock:");

        // Add labels to the info panel
        infoLabels.add(title); infoLabels.add(new JLabel(""));
        infoLabels.add(author); infoLabels.add(new JLabel(""));
        infoLabels.add(genre); infoLabels.add(new JLabel(""));
        infoLabels.add(year); infoLabels.add(new JLabel(""));
        infoLabels.add(price); infoLabels.add(new JLabel(""));
        infoLabels.add(stock); infoLabels.add(new JLabel(""));

        bookInfo.add(infoLabels, BorderLayout.CENTER);

        backButton = new JButton("Back");
        backButton.addActionListener(e -> {
        });

        // Add the back button to the layout
        bookInfo.add(backButton, BorderLayout.NORTH);

        content.add(bookInfo, "bookInfo");

        cardLayout.show(content, "main");

        // Double-click to show book info
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click to trigger action
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        String selectedTitle = (String) table.getValueAt(selectedRow, 0);
                        // Notify the controller to handle the selection
                        if (controller != null) {
                            controller.handleBookSelection(selectedTitle);
                        }
                    }
                }
            }
        });

        backButton.addActionListener(e -> {
            cardLayout.show(content, "main");
        });

        setVisible(false);
    }



    public void setBookData(List<BookUtility> books) {
        model.setRowCount(0);
        for (BookUtility book : books) {
            model.addRow(new Object[]{book.getTitle()});
        }
    }

    // Display the book info on the new panel that will be called in the controller
    public void displayBookInfo(BookUtility book) {
        JPanel labelsPanel = (JPanel) bookInfo.getComponent(0);

        ((JLabel) labelsPanel.getComponent(1)).setText(book.getTitle());
        ((JLabel) labelsPanel.getComponent(3)).setText(book.getAuthor());
        ((JLabel) labelsPanel.getComponent(5)).setText(book.getGenre());
        ((JLabel) labelsPanel.getComponent(7)).setText(book.getYear());
        ((JLabel) labelsPanel.getComponent(9)).setText(String.valueOf(book.getPrice()));
        ((JLabel) labelsPanel.getComponent(11)).setText(String.valueOf(book.getStock()));

        cardLayout.show(content, "bookInfo");
    }

    public void setController(BookStoreShowBookController controller) {
        this.controller = controller;
    }



}
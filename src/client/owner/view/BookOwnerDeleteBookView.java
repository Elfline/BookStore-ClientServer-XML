package client.owner.view;

import javax.swing.*;
import java.awt.*;

public class BookOwnerDeleteBookView extends JFrame{
    private JLabel titleLabel;
    private JButton yesButton, noButton;

    public BookOwnerDeleteBookView(String selectedBookTitle) {
        setTitle("Delete Book Confirmation");
        setSize(400, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Display the selected book title
        titleLabel = new JLabel("Are you sure you want to delete \n" + selectedBookTitle);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.CENTER);

        // Create Yes and No buttons
        JPanel buttonPanel = new JPanel();
        yesButton = new JButton("Yes");
        noButton = new JButton("No");
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        add(buttonPanel, BorderLayout.SOUTH);;

    }
    public JButton getYesButton() {
        return yesButton;
    }

    public JButton getNoButton() {
        return noButton;
    }
}

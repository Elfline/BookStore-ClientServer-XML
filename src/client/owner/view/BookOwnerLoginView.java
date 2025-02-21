package client.owner.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class BookOwnerLoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JComboBox<String> accountTypeComboBox;
    private JCheckBox showPasswordCheckBox;

    public BookOwnerLoginView() {
        setTitle("Book Owner Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());  // Use GridBagLayout for more control
        setSize(450, 350);
        setLocationRelativeTo(null);

        // Create GridBagConstraints to control layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Add some padding between components

        // Username label and text field
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        // Password label and text field
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        // Account type label and combo box
        JLabel accountTypeLabel = new JLabel("Account Type:");
        accountTypeComboBox = new JComboBox<>(new String[] {"Book Owner"});

        // Buttons
        loginButton = new JButton("Login");

        // Status label to display messages
        statusLabel = new JLabel(" ");

        // Show Password checkbox
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // Toggle password visibility based on checkbox state
                if (showPasswordCheckBox.isSelected()) {
                    passwordField.setEchoChar((char) 0); // Show password
                } else {
                    passwordField.setEchoChar('*'); // Hide password
                }
            }
        });

        // Add components to the frame with GridBagLayout constraints
        // Username label and text field
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(usernameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(usernameField, gbc);

        // Password label and text field
        gbc.gridx = 0; gbc.gridy = 1;
        add(passwordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        add(passwordField, gbc);

        // Show Password checkbox
        gbc.gridx = 1; gbc.gridy = 2;
        add(showPasswordCheckBox, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        add(accountTypeComboBox, gbc);


        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(loginButton, gbc);

        // Status label
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        add(statusLabel, gbc);

        setVisible(true);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public String getAccountType() {
        return (String) accountTypeComboBox.getSelectedItem();
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }
    /**
     * Clears all input fields and resets the login panel to its initial state.
     */
    public void clearFields() {
        usernameField.setText(""); // Clear the username field
        passwordField.setText(""); // Clear the password field
        passwordField.setEchoChar('*');
    }
}

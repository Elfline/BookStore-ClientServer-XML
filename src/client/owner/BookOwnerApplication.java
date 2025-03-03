package client.owner;
import client.owner.controller.*;
import client.owner.model.*;
import client.owner.view.*;

import javax.swing.*;

public class BookOwnerApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->{
            BookOwnerLoginModel model = new BookOwnerLoginModel();
            BookOwnerLoginView view = new BookOwnerLoginView();
            new BookOwnerLoginController(model, view);

            view.setVisible(true);
            });
        }
    }

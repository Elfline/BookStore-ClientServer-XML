package client.bookowner.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BookOwnerDeleteBookModel {
    BookOwnerModel model;

    public BookOwnerDeleteBookModel(BookOwnerModel model) {
        this.model = model;
    }

    public boolean deleteBook() {
        if (model.getSelectedBook() == null) {
            System.out.println("ERROR: No book selected for deletion!");
            return false;
        }

        String title = model.getSelectedBook().getTitle();
        try (Socket socket = new Socket("localhost", 2000);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            outputStream.writeObject("DELETE_BOOK");
            // Send the title to the server
            outputStream.writeObject(title);
            outputStream.flush();


            // Receive the response from the server
            // Receive the response from the server
            String response = (String) inputStream.readObject(); // Read the response as String
            return "SUCCESS".equals(response); // Check if response indicates success

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

}

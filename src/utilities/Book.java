package utilities;

import java.io.*;

public class Book implements Serializable {
    String title;
    String author;
    String genre;
    int stock;
    String year;
    double price;

    public Book(String title, String author, String genre, int stock, String year, double price) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.stock = stock;
        this.year = year;
        this.price = price;
    }
    // constructor for update book
    public Book(String title, int stock, double price) {
        this.title = title;
        this.stock = stock;
        this.price = price;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {this.stock = stock;}

    public String getYear() {return  year; }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public static <T> void serialize(T object, String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(object);
            System.out.println("[Serialization] Object serialized to: " + filePath);
        } catch (IOException e) {
            System.err.println("[Serialization] Error serializing object: " + e.getMessage());
        }
    }

    // Generic deserialization method
    public static <T> T deserialize(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            T object = (T) in.readObject();
            System.out.println("[Deserialization] Object deserialized from: " + filePath);
            return object;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Deserialization] Error deserializing object: " + e.getMessage());
            return null;
        }
    }
}
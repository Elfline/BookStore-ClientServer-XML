package utilities;

import java.io.*;

public class Cart implements Serializable {
    private int quantity;
    private double price;
    private String title;

    // Constructor
    public Cart(String title, int quantity, double price) {
        this.title = title;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }
}


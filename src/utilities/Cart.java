package utilities;


public class Cart  {
    private int quantity;
    private double price;
    private String title;

    // Constructor
    public Cart(String title, int quantity, double price) {
        this.title = title;
        this.quantity = quantity;
        this.price = price;
    }

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


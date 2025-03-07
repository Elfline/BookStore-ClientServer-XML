package utilities;



public class Transaction  {
    private String username;
    private String date;
    private String transactionId;
    private String bookTitle;
    private int quantity;
    private double price;
    private double totalAmount;

    public Transaction(String username, String date, String transactionId, String bookTitle, int quantity, double price, double totalAmount) {
        this.username = username;
        this.date = date;
        this.transactionId = transactionId;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername() {
        this.username = username;
    }
    public String getDate() {
        return date;
    }
    public void setDate() {
        this.date = date;
    }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() {
        return price;
    }
    public void setPrice() {
        this.price = price;
    }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}

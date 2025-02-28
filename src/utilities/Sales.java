package utilities;

public class Sales  {
    private String transactionId;
    private String bookTitle;
    private int quantity;
    private double price;
    private double bookTotal;
    private double revenue;
    private String date;

    public Sales(String transactionId, String date, String bookTitle, int quantity, double price, double bookTotal, double revenue) {
        this.transactionId = transactionId;
        this.date = date;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
        this.price = price;
        this.bookTotal = bookTotal;
        this.revenue = revenue;
    }

    public String getDate() {
        return date;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getBookTotal() {
        return bookTotal;
    }

    public double getRevenue() {
        return revenue;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public double getPrice() {
        return price;
    }
}

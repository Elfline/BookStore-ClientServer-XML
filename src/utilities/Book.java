package utilities;

public class Book {
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

}
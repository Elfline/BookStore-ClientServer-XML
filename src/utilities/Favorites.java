package utilities;

public class Favorites {
    private String user;
    private String title;
    private String author;
    private String year;
    private int stock;

    public Favorites(String user, String title, String author, String year, int stock) {
        this.user = user;
        this.title = title;
        this.author = author;
        this.year = year;
        this.stock = stock;
    }

    public String getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getYear() {
        return year;
    }

    public int getStock() {
        return stock;
    }
}

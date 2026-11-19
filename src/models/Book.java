package models;

import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    // Attributes
    private String isbn;
    private String title;
    private String author;
    private String category;

    private int totalCopies;
    private int availableCopies;
    private boolean archived;

    // Constructor
    public Book(String isbn, String title, String author, String category, int totalCopies, int availableCopies) {
        this.isbn = requireText(isbn, "ISBN");
        this.title = requireText(title, "Title");
        this.author = requireText(author, "Author");
        this.category = requireText(category, "Category");

        if (totalCopies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative");
        }
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException(
                    "Available copies must be between zero and total copies");
        }

        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    // Getters
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    // Setters
    public void setIsbn(String isbn) {
        this.isbn = requireText(isbn, "ISBN");
    }

    public void setTitle(String title) {
        this.title = requireText(title, "Title");
    }

    public void setAuthor(String author) {
        this.author = requireText(author, "Author");
    }

    public void setCategory(String category) {
        this.category = requireText(category, "Category");
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative");
        }

        int borrowedCopies = this.totalCopies - this.availableCopies;

        if (totalCopies < borrowedCopies) {
            throw new IllegalArgumentException("Total copies cannot be less than borrowed copies");
        }

        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies - borrowedCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public void borrowCopy() {
        if (archived) {
            throw new IllegalStateException("Archived books cannot be borrowed.");
        }
        if (!isAvailable()) {
            throw new IllegalArgumentException("No copies available to borrow");
        }
        availableCopies--;

    }

    public void returnCopy() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException(
                    "Available copies cannot exceed total copies.");
        }
        availableCopies++;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        return value.trim();
    }
}

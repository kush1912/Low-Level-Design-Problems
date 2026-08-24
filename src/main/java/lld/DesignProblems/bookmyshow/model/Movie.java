package lld.DesignProblems.bookmyshow.model;

public class Movie {
    private String title;
    private String language;
    private double rating;
    private String description;

    public Movie(String title, String language, double rating, String description) {
        this.title = title;
        this.language = language;
        this.rating = rating;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }
}

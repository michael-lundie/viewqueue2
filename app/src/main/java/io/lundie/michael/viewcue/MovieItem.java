package io.lundie.michael.viewcue;

public class MovieItem {
    private String title;
    private int date;
    private String posterURL;
    private int voteAverage;
    private String synopsis;

    public MovieItem(String title, int date, String posterURL, int voteAverage, String synopsis) {
        this.title = title;
        this.date = date;
        this.posterURL = posterURL;
        this.voteAverage = voteAverage;
        this.synopsis = synopsis;
    }

    public String getTitle() { return title; }

    public int getDate() { return date; }

    public String getPosterURL() { return posterURL; }

    public int getVoteAverage() { return voteAverage; }

    public String getSynopsis() { return synopsis; }
}

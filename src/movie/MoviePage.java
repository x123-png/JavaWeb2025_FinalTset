package movie;

import java.util.List;

public class MoviePage {
    private List<Movie> movies;
    private int pages;
    private int page;
    private int size;

    public MoviePage() {
    }

    public MoviePage(List<Movie> movies, int pages, int page, int size) {
        this.movies = movies;
        this.pages = pages;
        this.page = page;
        this.size = size;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
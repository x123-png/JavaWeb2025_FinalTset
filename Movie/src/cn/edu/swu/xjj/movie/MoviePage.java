package cn.edu.swu.xjj.movie;

import java.util.List;

public class MoviePage {
    int pages;
    int page;
    int size;
    List<Movie> movies;

    public MoviePage() {
    }

    public MoviePage(int pages, int page, int size, List<Movie> movies) {
        this.pages = pages;
        this.page = page;
        this.size = size;
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

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    @Override
    public String toString() {
        return "MoviePage{" +
                "pages=" + pages +
                ", page=" + page +
                ", size=" + size +
                ", movies=" + movies +
                '}';
    }
}

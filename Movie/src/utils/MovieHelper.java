package utils;

import movie.Movie;

import java.util.List;

public class MovieHelper {

    public static String buildMoviesTable(List<Movie> movies) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class='tb-movie'>");
        sb.append("<tr>")
                .append("<th>ID</th><th>Title</th><th>Year</th><th>Region</th><th>Language</th><th>Genre</th><th>Rating</th><th>Actions</th>")
                .append("</tr>");
        String template = """
            <tr>
                <td align='center'>%d</td>
                <td>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>
                    <a href='./updateMovie?id=%d'>Edit</a> |
                    <a href='./deleteMovie?id=%d' onclick='return confirm(\"Are you sure?\")'>Delete</a>
                </td>
            </tr>
        """;
        for (Movie m : movies) {
            String yearStr = m.getReleaseYear() != null ? m.getReleaseYear().toString() : "N/A";
            String ratingStr = m.getAverageRating() != null ? m.getAverageRating().toString() : "N/A";
            sb.append(String.format(template,
                    m.getMovieId(), 
                    m.getMovieTitle(), 
                    yearStr, 
                    m.getRegion(), 
                    m.getLanguage(), 
                    m.getGenre(), 
                    ratingStr,
                    m.getMovieId(), 
                    m.getMovieId()
            ));
        }
        sb.append("</table>");
        return sb.toString();
    }
}
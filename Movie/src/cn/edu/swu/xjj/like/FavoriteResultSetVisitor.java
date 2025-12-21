package cn.edu.swu.xjj.like;

import cn.edu.swu.xjj.repo.ResultSetVisitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteResultSetVisitor implements ResultSetVisitor {
    @Override
    public List visit(ResultSet rs) throws SQLException {
        List<Favorite> favorites = new ArrayList<>();
        while (rs.next()) {
            Favorite favorite = new Favorite();
            favorite.setFavoriteId(rs.getInt("favoriteId"));
            favorite.setUserId(rs.getInt("userId"));
            favorite.setMovieId(rs.getInt("movieId"));
            favorites.add(favorite);
        }
        return favorites;
    }
}
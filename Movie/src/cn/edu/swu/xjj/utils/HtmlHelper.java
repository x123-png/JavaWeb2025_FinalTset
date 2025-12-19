package cn.edu.swu.xjj.utils;

import cn.edu.swu.xjj.movie.Movie;

import java.util.List;

public class HtmlHelper {
    public static String wrapHtml(String content) {
        String template = """
<html>
    <head>
        <meta charset="utf-8">
        <title>剧光</title>
        <link rel="stylesheet" href="../css/movie.css" />
    </head>
<body>
    <center>
        <div class="page-container">
            <h1>欢迎访问剧光</h1>
            <div class="nav-links">
                <a href="./add_movie.html"> 添加电影 </a>
                <a href="./movies"> 显示列表 </a>
                <a href="../x123-png/logout"> 退出系统 </a>
            </div>
            <div class="search-form">
                <form action="../searchMovie" method="get">
                    <input type="text" name="content" placeholder="输入搜索内容"> <input type="submit" value="查询">
                </form>
            </div>
            %s
        </div>
    </center>
</body>
</html>
       """;
        return String.format(template, content);
    }
    public static String buildMoviessTable(List<Movie> movies) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class='tb-movie'>");
        sb.append("<tr>")
                .append("<th>编号</th><th>片名</th><th>发行日期</th><th>地区</th><th>语言</th><th>类型</th><th>故事梗概</th><th>平均评分</th><th>海报</th><th></th><th></th>")
                .append("</tr>");
        String template = """
            <tr>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'><div class="actions"><a href='./deleteMovie?movieId=%s'>删除</a></div></td>
                <td align='center'><div class="actions"><a href='./updateMovie?movieId=%s' class="update">修改</a></div></td>
            </tr>
        """;
        int seqNum = 1;
        for (Movie m : movies) {
            String pictureDisplay = m.getPicture() != null ?
                    "<img src='upload/" + m.getPicture() + "' width='50' height='50' />"  : "无图片";
            sb.append(String.format(template,
                    seqNum, m.getMovieTitle(), m.getReleaseYear(), m.getRegion(), m.getLanguage(), m.getGenre(), m.getPlotSummary(), m.getAverageRating(),
                    pictureDisplay, m.getMovieId(), m.getMovieId()
            ));
            seqNum++;
        }
        sb.append("</table>");
        return sb.toString();
    }
}
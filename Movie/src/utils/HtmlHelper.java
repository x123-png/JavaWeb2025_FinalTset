package utils;

import movie.Movie;

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
        <br><h1>欢迎访问剧光</h1>
        <div>
            <a href="./add_movie.html"> 添加电影 </a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <a href="./logout"> 退出系统 </a>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </div>
        <br>
        <form action="./searchMovie" method="get">
            <input type="text" name="content"> &nbsp;&nbsp;&nbsp; <input type="submit" value="查询">
        </form>
        %s
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
                <td align='center'><a href='./deleteMovie?id=%s'>删除</a></td>
                <td align='center'><a href='./updateMovie?id=%s'>修改</a></td>
            </tr>
        """;
        int seqNum = 1;
        for (Movie b : movies) {
            String pictureDisplay = b.getPicture() != null ?
                    "<img src='../upload/" + b.getPicture() + "' width='50' height='50' />" : "无图片";
            sb.append(String.format(template,
                    seqNum, b.getMovieTitle(), b.getReleaseYear(), b.getRegion(), b.getLanguage(), b.getGenre(), b.getPlotSummary(), b.getAverageRating(),
                    pictureDisplay, b.getMovieId(), b.getMovieId()
            ));
            seqNum++;
        }
        sb.append("</table>");
        return sb.toString();
    }
}
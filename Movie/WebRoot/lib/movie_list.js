function getMovies(page, size) {
    $.ajax({
        url: "./movies?page=" + page + "&size=" + size,
        type: "GET",
        success: showMovies,
        error: function(xhr, status, error){
            console.error("ajax failed:" + error);
        }
    });
}

function showMovies(data, status) {
    var container = $("#movieTableContainer");
    // 清除之前的内容
    container.empty();

    // 创建电影网格容器
    var moviesGrid = $("<div class='movies-container'></div>");

    var movies = data.movies;
    for (var i=0; i<movies.length; i++) {
        var movie = movies[i];
        console.log(movie);

        // 创建电影项目元素
        var movieItem = $("<div class='movie-item'></div>");
        movieItem.append($("<h3></h3>").text(movie.movieTitle));
        movieItem.append($("<p><strong>发行日期：</strong>" + movie.releaseYear + "</p>"));
        movieItem.append($("<p><strong>地区：</strong>" + movie.region + "</p>"));
        movieItem.append($("<p><strong>语言：</strong>" + movie.language + "</p>"));
        movieItem.append($("<p><strong>类型：</strong>" + movie.genre + "</p>"));
        movieItem.append($("<p><strong>故事梗概：</strong>" + movie.plotSummary + "</p>"));
        movieItem.append($("<p><strong>平均评分：</strong>" + movie.averageRating + "</p>"));

        // 如果有电影海报，添加图片
        if (movie.posterUrl) {
            var img = $("<img src='" + movie.posterUrl + "' alt='" + movie.movieTitle + "' />");
            movieItem.prepend(img);
        }

        moviesGrid.append(movieItem);
    }

    container.append(moviesGrid);

    // 添加分页控件
    var pagerContainer = $("<div id='moviePagerContainer' class='pagination'></div>");
    container.append(pagerContainer);

    // 显示分页
    createPager(data);
}

function createPager(data) {
    var container = $("#moviePagerContainer");
    var pages = data.pages;
    var page = data.page;
    var size = data.size;

    // 清除之前的内容
    container.empty();

    // 创建分页控件容器
    var paginationControls = $("<div class='pagination-controls'></div>");

    // 上一页按钮
    if (page != 1) {
        var prevBtn = $("<button class='page-btn' data-page='" + (page - 1) + "' data-size='" + size + "'>上一页</button>");
        prevBtn.click(function() {
            var p = $(this).data("page");
            var s = $(this).data("size");
            getMovies(p, s);
        });
        paginationControls.append(prevBtn);
    }

    // 页码按钮
    for (var i = 1; i <= pages; i++) {
        var pageBtn;
        if (i === page) {
            pageBtn = $("<button class='page-btn active-page' data-page='" + i + "' data-size='" + size + "'>" + i + "</button>");
        } else {
            pageBtn = $("<button class='page-btn' data-page='" + i + "' data-size='" + size + "'>" + i + "</button>");
        }
        pageBtn.click(function() {
            var p = $(this).data("page");
            var s = $(this).data("size");
            getMovies(p, s);
        });
        paginationControls.append(pageBtn);
    }

    // 下一页按钮
    if (page != pages) {
        var nextBtn = $("<button class='page-btn' data-page='" + (page + 1) + "' data-size='" + size + "'>下一页</button>");
        nextBtn.click(function() {
            var p = $(this).data("page");
            var s = $(this).data("size");
            getMovies(p, s);
        });
        paginationControls.append(nextBtn);
    }

    // 添加页数信息
    var pageInfo = $("<div class='page-info'>第 " + page + " 页，共 " + pages + " 页</div>");

    container.append(paginationControls).append(pageInfo);
}
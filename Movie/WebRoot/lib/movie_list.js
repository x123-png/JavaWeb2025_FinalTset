function getMovies(page, size) {
    $.ajax({
        url: "./x123-png/moviesService?page=" + page + "&size=" + size,
        type: "GET",
        success: showMovies,
        error: function(xhr, status, error){
            console.error("ajax failed:" + error);
            console.log("Failed URL: ./x123-png/moviesService?page=" + page + "&size=" + size);
            console.log("HTTP Status: " + xhr.status + ", " + xhr.statusText);

            // 尝试使用绝对路径
            $.ajax({
                url: "x123-png/moviesService?page=" + page + "&size=" + size,
                type: "GET",
                success: showMovies,
                error: function(xhr, status, error){
                    console.error("ajax failed with path x123-png/moviesService too:" + error);
                    console.log("HTTP Status: " + xhr.status + ", " + xhr.statusText);
                    // 显示错误信息给用户
                    $("#movieTableContainer").html("<p class='text-danger text-center'>加载电影列表失败，请检查后端服务是否正常运行: " + error + " (状态码: " + xhr.status + ")</p>");
                }
            });
        }
    });
}

function showMovies(data, status) {
    var container = $("#movieTableContainer");
    // 清除之前的内容
    container.empty();

    // 创建电影网格容器 - 适配首页Portfolio样式
    var moviesGrid = $("<div class='row gx-4 gx-lg-5 justify-content-center'></div>");

    var movies = data.movies;
    for (var i=0; i<movies.length; i++) {
        var movie = movies[i];
        console.log(movie);

        // 创建电影项目元素 - 使用Bootstrap网格系统
        var movieItem = $("<div class='col-lg-4 col-sm-6 mb-4'></div>");

        // 创建海报链接元素
        var posterLink = $("<a class='portfolio-box position-relative d-block'></a>");
        posterLink.attr("title", movie.movieTitle);

        // 添加海报图片，如果存在的话
        if (movie.picture) {
            var img = $("<img class='img-fluid rounded-3 mb-2' src='upload/" + movie.picture + "' alt='" + movie.movieTitle + "' style='height: 250px; object-fit: cover;'>");
        } else {
            // 如果没有图片，创建一个占位符
            var img = $("<div class='img-fluid rounded-3 mb-2 bg-primary d-flex align-items-center justify-content-center' style='height: 250px;'>" +
                        "<span class='text-white text-center'>无海报<br><small>" + movie.movieTitle + "</small></span></div>");
        }

        // 添加电影信息覆盖层
        var caption = $("<div class='portfolio-box-caption p-3 position-absolute w-100 bottom-0 start-0 text-white' style='background: linear-gradient(transparent, rgba(0,0,0,0.8));'></div>");
        caption.append($("<div class='project-category text-white-50 small'>评分：" + (movie.averageRating || "暂无") + "</div>"));
        caption.append($("<div class='project-name fw-bold'>" + movie.movieTitle + "</div>"));

        // 添加电影简介作为弹出信息
        if (movie.plotSummary) {
            caption.append($("<div class='project-description small d-none d-md-block'>"
                + movie.plotSummary.substring(0, 60) + "...</div>"));
        }

        posterLink.append(img).append(caption);
        movieItem.append(posterLink);

        moviesGrid.append(movieItem);
    }

    container.append(moviesGrid);

    // 添加分页控件
    // 注意：由于分页容器已在HTML中定义，我们更新现有的容器
    var pagerContainer = $("#moviePagerContainer");
    pagerContainer.empty(); // 清空现有内容

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

    // 创建分页控件容器 - 使用Bootstrap分页样式
    var paginationControls = $("<nav aria-label='电影列表分页'><ul class='pagination justify-content-center'></ul></nav>");
    var paginationList = paginationControls.find('ul');

    // 上一页按钮
    if (page != 1) {
        var prevLi = $("<li class='page-item'><a class='page-link' href='javascript:void(0)' data-page='" + (page - 1) + "' data-size='" + size + "'>上一页</a></li>");
        prevLi.find('a').click(function() {
            var p = $(this).data("page");
            var s = $(this).data("size");
            getMovies(p, s);
        });
        paginationList.append(prevLi);
    } else {
        // 如果是第一页，显示禁用的上一页按钮
        var prevLi = $("<li class='page-item disabled'><span class='page-link'>上一页</span></li>");
        paginationList.append(prevLi);
    }

    // 页码按钮
    for (var i = 1; i <= pages; i++) {
        var pageLi;
        if (i === page) {
            pageLi = $("<li class='page-item active'><span class='page-link'>" + i + "</span></li>");
        } else {
            pageLi = $("<li class='page-item'><a class='page-link' href='javascript:void(0)' data-page='" + i + "' data-size='" + size + "'>" + i + "</a></li>");
            pageLi.find('a').click(function() {
                var p = $(this).data("page");
                var s = $(this).data("size");
                getMovies(p, s);
            });
        }
        paginationList.append(pageLi);
    }

    // 下一页按钮
    if (page != pages) {
        var nextLi = $("<li class='page-item'><a class='page-link' href='javascript:void(0)' data-page='" + (page + 1) + "' data-size='" + size + "'>下一页</a></li>");
        nextLi.find('a').click(function() {
            var p = $(this).data("page");
            var s = $(this).data("size");
            getMovies(p, s);
        });
        paginationList.append(nextLi);
    } else {
        // 如果是最后一页，显示禁用的下一页按钮
        var nextLi = $("<li class='page-item disabled'><span class='page-link'>下一页</span></li>");
        paginationList.append(nextLi);
    }

    container.append(paginationControls);
}
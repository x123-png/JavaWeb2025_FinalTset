function getBooks(page, size) {
    $.ajax({
        url: "./books?page="+ page +"&size=" + size,
        type: "GET",
        success: function(data, status) {
           showBooks(data, status);
           showPager(data);
        },
        error: function(xhr, status, error) {
            console.error("ajax failed:" + error);
        }
    });
}

function showBooks(data, status) {
    var container = $("#bookTableContainer");
    container.empty();

    var books = data.books;
    for (var i=0; i<books.length; i++) {
        var book = books[i];
        console.log(book);
        var d = $("<div class='col-md-6 col-lg-4 mb-5 to-center'>");
        d.append("<img src='./upload/" + book.picture + "' width='200' height='200'></img>").append("<br><br>");
        d.append("书名：" + book.name).append("<br>");
        d.append("作者：" + book.author).append("<br>");
        d.append("价格：" + book.price).append("<br>");
        d.append("发行：" + book.publish).append("<br>");
        container.append(d);
    }
}

function showPager(data) {
    var pages = data.pages;
    var page = data.page;
    var size = data.size;

    var pager = $("#bookPager");
    pager.empty();

    if (page != 1) {
        var pre = $("<li class='page-item'><a class='page-link' href='#' page='" + (page-1) + "' size='" + size + "' onclick='return false;'><<上一页</a></li>")
        pager.append(pre);
    }

    for (var i=1; i<=pages; i++) {
       var cur = $("<li class='page-item'><a class='page-link' href='#' page='" + i + "' size='" + size + "' onclick='return false;'>" + i + "</a></li>")
        pager.append(cur);
    }

    if (page != pages) {
        var next = $("<li class='page-item'><a class='page-link' href='#' page='" + (page+1) + "' size='" + size + "' onclick='return false;'>下一页>></a></li>")
        pager.append(next);
    }

    pager.find("a").each(function(idx, e){
        $(e).click(function(){
            var page = $(this).attr("page");
            var size = $(this).attr("size");
            getBooks(page, size);
        });
    });
}
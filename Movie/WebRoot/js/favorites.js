// 电影收藏功能的JavaScript代码
function toggleFavorite(movieId, element) {
    // 获取当前按钮的文本，判断是收藏还是取消收藏
    var isFavorite = element.textContent.includes('取消收藏');
    var action = isFavorite ? 'remove' : 'add';
    
    // 向服务器发送请求
    fetch('./toggleFavorite', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'movieId=' + movieId + '&action=' + action
    })
    .then(response => response.json())
    .then(data => {
        if(data.success) {
            alert(data.message);
            // 更新按钮文本
            if(action === 'add') {
                element.textContent = '取消收藏';
            } else {
                element.textContent = '收藏';
            }
        } else {
            alert('操作失败: ' + data.message);
        }
    })
    .catch(error => {
        alert('网络错误，请重试');
    });
}
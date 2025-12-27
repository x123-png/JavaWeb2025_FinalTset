// 更新在线用户数的函数
function updateOnlineUserCount() {
    $.ajax({
        url: './x123-png/onlineUsers',
        type: 'GET',
        success: function(data) {
            // 在页面中显示在线用户数
            $('#onlineUserCount').text(data.onlineUserCount);
            
            // 如果有用户登录，也可以显示用户名
            if (data.isLoggedIn && data.currentUsername) {
                $('#currentUser').text('（' + data.currentUsername + '）');
            }
        },
        error: function(xhr, status, error) {
            console.error('获取在线用户数失败:', error);
        }
    });
}

// 页面加载完成后开始更新在线用户数
$(document).ready(function() {
    // 初始更新
    updateOnlineUserCount();
    
    // 每10秒更新一次在线用户数
    setInterval(updateOnlineUserCount, 10000);
});
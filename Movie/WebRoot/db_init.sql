CREATE DATABASE IF NOT EXISTS movie_list
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE movie_list;

DROP TABLE IF EXISTS movies;

CREATE TABLE movies (
    movieId INT NOT NULL AUTO_INCREMENT,
    movieTitle VARCHAR(255) NOT NULL,
    releaseYear DATE DEFAULT NULL,
    region VARCHAR(100) DEFAULT NULL,
    language VARCHAR(100) DEFAULT NULL,
    genre VARCHAR(100) DEFAULT NULL,
    plotSummary CHAR(100) DEFAULT NULL,
    averageRating DOUBLE DEFAULT NULL,
    picture VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (movieId)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

INSERT INTO movies
(movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture)
VALUES
('肖申克的救赎', '1994-09-23', '美国', '英语', '剧情',
 '讲述了银行家安迪被冤入狱，在肖申克监狱漫长的监禁中，他凭借智慧与坚韧默默谋划，最终通过一条秘密挖凿的隧道成功越狱，重获自由与正义的故事', 9.7,
 '/upload/肖申克的救赎.webp'),

('霸王别姬', '1993-01-01', '中国', '中文', '剧情/爱情/同性',
 '一对京剧艺人程蝶衣和段小楼跨越半个世纪的悲欢离合，展现了他们在时代洪流中从相知、相依到最终走向悲剧结局的命运。影片通过个人情感与历史变迁的交织，深刻探讨了艺术、爱情、背叛与身份认同等主题。', 9.6,
 '/upload/霸王别姬.webp'),

('公民凯恩', '1941-09-05', '美国', '英语', '剧情/悬疑/传记',
 '记者调查报业大亨凯恩一生，揭开“玫瑰花蕾”之谜，展现财富与人性的异化。', 9.5,
 '/upload/公民凯恩.jpg'),

('盗梦空间', '2010-07-16', '美国', '英语', '科幻/悬疑',
 '盗梦者柯布为归家，率领团队潜入多层梦境植入意念，在现实与梦境的边界挣扎，探讨记忆、执念与真实，以开放式陀螺结局引发虚实之思。', 9.3,
 '/upload/盗梦空间.jpg'),

('千与千寻', '2001-07-20', '日本', '日语', '动画/奇幻',
 '少女千寻在神秘世界为救父母，进入汤屋工作，经历成长考验，最终保持本心解救家人、帮助朋友，揭示贪欲之害与纯真力量。', 9.4,
 '/upload/千与千寻.webp');

SELECT 'movies table initialized successfully.' AS message;

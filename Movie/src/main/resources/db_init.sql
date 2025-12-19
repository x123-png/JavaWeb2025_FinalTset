CREATE DATABASE IF NOT EXISTS movie_list
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE movie_list;

CREATE TABLE IF NOT EXISTS db_init_flag (
    id INT PRIMARY KEY,
    initialized BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT IGNORE INTO db_init_flag (id, initialized)
VALUES (1, FALSE);

CREATE TABLE IF NOT EXISTS movies (
    movieId INT NOT NULL AUTO_INCREMENT,
    movieTitle VARCHAR(255) NOT NULL,
    releaseYear DATE DEFAULT NULL,
    region VARCHAR(100) DEFAULT NULL,
    language VARCHAR(100) DEFAULT NULL,
    genre VARCHAR(100) DEFAULT NULL,
    plotSummary VARCHAR(400) DEFAULT NULL,
    averageRating DOUBLE DEFAULT NULL,
    picture VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (movieId)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;

INSERT INTO movies
(movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture)
SELECT
    '肖申克的救赎', '1994-09-23', '美国', '英语', '剧情',
    '银行家安迪被冤入狱，在肖申克监狱中忍受多年压迫与不公，凭借智慧、隐忍与希望，暗中策划逃离牢笼，最终重获自由。',
    9.7, 'Shawshank.webp'
FROM dual
WHERE (SELECT initialized FROM db_init_flag WHERE id = 1) = FALSE;

INSERT INTO movies
(movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture)
SELECT
    '霸王别姬', '1993-01-01', '中国', '中文', '剧情/爱情',
    '京剧艺人程蝶衣与段小楼相伴半生，在时代动荡与情感纠葛中，从相知相守到分离决裂，映射个人命运与历史洪流的悲剧。',
    9.6, 'Farewell.webp'
FROM dual
WHERE (SELECT initialized FROM db_init_flag WHERE id = 1) = FALSE;

INSERT INTO movies
(movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture)
SELECT
    '盗梦空间', '2010-07-16', '美国', '英语', '科幻/悬疑',
    '盗梦者柯布率领团队潜入多层梦境执行植入任务，在时间错位与心理迷宫中挣扎，试图完成使命并重返真实生活。',
    9.3, 'Inception.jpg'
FROM dual
WHERE (SELECT initialized FROM db_init_flag WHERE id = 1) = FALSE;

INSERT INTO movies
(movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture)
SELECT
    '千与千寻', '2001-07-20', '日本', '日语', '动画/奇幻',
    '少女千寻误入神灵世界，为拯救被变成猪的父母进入汤屋工作，在历练中成长，最终守住善良与勇气找回自我。',
    9.4, 'SpiritedAway.webp'
FROM dual
WHERE (SELECT initialized FROM db_init_flag WHERE id = 1) = FALSE;

UPDATE db_init_flag
SET initialized = TRUE
WHERE id = 1 AND initialized = FALSE;

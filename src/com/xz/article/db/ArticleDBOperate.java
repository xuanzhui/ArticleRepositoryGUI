package com.xz.article.db;

/**
 * Created by xuanzhui on 15/4/14.
 */
public interface ArticleDBOperate {
    int NOT_FOUND = -1;
    int TOO_MANY_MATCH = -2;
    String CONN_SUCC = "SUCC";
    String CONN_FAIL = "FAIL";
    String DB_CONF_CACHE = "dbconf.properties";

    //first item contains connect info (succ or fail), second contains failure info
    String[] getConnection(String host, String port, String db, String username, String password);
    void closeConnection();
    int findArtist(String artistName, String dynasty);
    //contains the inserted id and possible failure info
    String[] insertArtist(String artistName, String dynasty);
    long findArticle(String artistName, String dynasty, String title, String content);
    String[] insertArticle(String artistName, String dynasty, String title, String content, int rate, String type);
    String getArticleContent(long articleid);
    String[] findComment(String artistName, String dynasty, String articleTitle, String articleContent, String commentTitle, String commentContent, String commentAuthor);
    String getCommentContent(long commentid);
    String[] insertComment(String artistName, String dynasty, String articleTitle, String articleContent, int articleRate, String articleType, String commentTitle, String commentContent, String commentAuthor);
}

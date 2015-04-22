package com.xz.article.db;

/**
 * Created by xuanzhui on 15/4/14.
 */
public interface ArticleDBOperate {
    public static String CONN_SUCC = "SUCC";
    public static String CONN_FAIL = "FAIL";
    public static String DB_CONF_CACHE = "dbconf.properties";

    //first item contains connect info (succ or fail), second contains failure info
    public String[] getConnection(String host, String port, String db, String username, String password);
    public void closeConnection();
    public int findPoet(String poetName, String dynasty);
    //contains the inserted id and possible failure info
    public String[] insertPoet(String poetName, String dynasty);
    public boolean insertPoem(String poetName, String title, String content, String dynasty, String type);
    public boolean updatePoemContent(String poetName, String title, String content);
}

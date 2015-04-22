package com.xz.article.db;

import java.sql.*;

/**
 * Created by xuanzhui on 15/4/14.
 */
public class PostgreSQLImpl implements ArticleDBOperate{
    private static Connection connection;

    public String[] getConnection(String host, String port, String db, String username, String password){
        String[] res=new String[2];
        res[1] = "";
        try {
            if (connection != null && !connection.isClosed()) {
                res[0] = ArticleDBOperate.CONN_SUCC;
                return res;
            }
        } catch (SQLException e) {
            res[0] = ArticleDBOperate.CONN_FAIL;
            res[1] = e.getMessage();
            e.printStackTrace();
        }

        try {
            Class.forName("org.postgresql.Driver");
            String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);
            connection = DriverManager
                    .getConnection(url,
                            username, password);
            res[0] = ArticleDBOperate.CONN_SUCC;
        } catch (ClassNotFoundException e) {
            res[0] = ArticleDBOperate.CONN_FAIL;
            res[1] = e.getMessage();
            e.printStackTrace();
        } catch (SQLException e) {
            res[0] = ArticleDBOperate.CONN_FAIL;
            res[1] = e.getMessage();
            e.printStackTrace();
        }

        return res;
    }

    public void closeConnection(){
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int findPoet(String poetName, String dynasty){
        int res=-1;
        try {
            PreparedStatement pst = connection.prepareStatement("select id from artists where name like ? and dynasty like ?");
            pst.setString(1, poetName);
            pst.setString(2, dynasty);
            ResultSet rs = pst.executeQuery();

            if (rs.next())
                res = rs.getInt("id");

            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public String[] insertPoet(String poetName, String dynasty) {
        String[] res = new String[2];
        res[0]="-1";

        if (this.findPoet(poetName, dynasty.length() == 0 ? "%" : dynasty) != -1){
            res[1] = poetName + (dynasty.length() == 0 ? "" : "("+dynasty+")") + " already exists!";

            return res;
        }

        try {
            PreparedStatement pst = connection.prepareStatement("insert into artists(name,dynasty) values(?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setString(1, poetName);
            pst.setString(2, dynasty);
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();

            if (rs.next())
                res[0] = Integer.toString(rs.getInt(1));
            else
                res[1] = "Cannot get the artist id!";

            rs.close();
            pst.close();
        } catch (SQLException e) {
            res[1] = e.getMessage();
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public boolean insertPoem(String poetName, String title, String content, String dynasty, String type) {
        return false;
    }

    @Override
    public boolean updatePoemContent(String poetName, String title, String content) {
        return false;
    }
}

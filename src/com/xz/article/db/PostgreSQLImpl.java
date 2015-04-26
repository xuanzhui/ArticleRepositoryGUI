package com.xz.article.db;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

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

    public int findArtist(String artistName, String dynasty){
        int res = ArticleDBOperate.NOT_FOUND;
        try {
            PreparedStatement pst = connection.prepareStatement("select id from artists where name like ? and dynasty like ?");
            pst.setString(1, artistName);
            pst.setString(2, dynasty);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                res = rs.getInt("id");

                if (rs.next())
                    res = ArticleDBOperate.TOO_MANY_MATCH;
            }

            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public String[] insertArtist(String artistName, String dynasty) {
        return this.insertArtist(artistName, dynasty, true);
    }

    private String[] insertArtist(String artistName, String dynasty, boolean checkBeforeAdd){
        String[] res = new String[2];
        res[0]="-1";

        if (checkBeforeAdd){
            int artistid = this.findArtist(artistName, dynasty.length() == 0 ? "%" : dynasty);

            if (artistid != ArticleDBOperate.NOT_FOUND || artistid == ArticleDBOperate.TOO_MANY_MATCH) {
                res[1] = artistName + (dynasty.length() == 0 ? "" : "(" + dynasty + ")") + " already exists!";
                return res;
            }
        }

        try {
            PreparedStatement pst = connection.prepareStatement("insert into artists(name,dynasty) values(?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setString(1, artistName);
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
    public long findArticle(String artistName, String dynasty, String title, String content) {
        int artistid=this.findArtist(artistName, dynasty);

        if (artistid == ArticleDBOperate.NOT_FOUND || artistid == ArticleDBOperate.TOO_MANY_MATCH)
            return artistid;

        return this.findArticle(artistid, title, content);
    }

    private long findArticle(int artistid, String title, String content) {
        long res = ArticleDBOperate.NOT_FOUND;

        try {
            PreparedStatement pst = connection.prepareStatement("select id from articles where artist_id = ? and title like ? and content like ?");
            pst.setInt(1, artistid);
            pst.setString(2, title);
            pst.setString(3, content);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                res = rs.getLong(1);

                if (rs.next())
                    res = ArticleDBOperate.TOO_MANY_MATCH;
            }

            rs.close();
            pst.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public String[] insertArticle(String artistName, String dynasty, String title, String content,int rate, String type) {
        String[] res = new String[2];
        res[0] = "-1";
        res[1] = "";

        int artistid=this.findArtist(artistName, dynasty.length() == 0 ? "%" : dynasty);
        String[] artistres = null;

        if (artistid == ArticleDBOperate.NOT_FOUND) {
            res[1] += (artistName + (dynasty.length() == 0 ? "" : "("+dynasty+")") + " doesn't exist!");
            artistres = this.insertArtist(artistName, dynasty, false);
            if (artistres[0].equals("-1")){
                res[1] += " Failed to add artist!\n";
                return res;
            }else{
                res[1] += (" Succeeded to add artist, its id is "+artistres[0]+"\n");
                artistid = Integer.valueOf(artistres[0]);
            }
        }
        else if (artistid == ArticleDBOperate.TOO_MANY_MATCH) {
            res[1] += (artistName + (dynasty.length() == 0 ? "" : "("+dynasty+")") + " has too many matches, please be more specific!\n");
            return res;
        }
        else{
            res[1] += (artistName + (dynasty.length() == 0 ? "" : "("+dynasty+")") + " already exists! won't add this artist again...\n");
        }

        if (this.findArticle(artistid, title, "%" + content + "%") != -1){
            res[1] += String.format("%s(%s) already exists, skipping...", title, "%"+content+"%");
            return res;
        }

        long articleid= this.insertArticle(artistid, title, content, type, rate);

        if (articleid == -1)
            res[1] += "Cannot get the artist id!";
        else
            res[0] = Long.toString(articleid);

        return res;
    }

    private long insertArticle(int artistid, String title, String content, String type, int rate){
        long res = -1;

        try {
            PreparedStatement pst = connection.prepareStatement("insert into articles(artist_id, title, content, article_type, rate) values(?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setInt(1, artistid);
            pst.setString(2, title);
            pst.setString(3, content);
            pst.setString(4, type);
            pst.setInt(5, rate);
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();

            if (rs.next())
                res = rs.getInt(1);

            rs.close();
            pst.close();
        } catch (SQLException e) {
            res = -1;
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public String getArticleContent(long articleid) {
        String res = "";
        try {
            PreparedStatement pst = connection.prepareStatement("select content from articles where id = ?");
            pst.setLong(1, articleid);
            ResultSet rs = pst.executeQuery();
            if (rs.next())
                res = rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public String[] findComment(String artistName, String dynasty, String articleTitle, String articleContent, String commentTitle, String commentContent, String commentAuthor) {
        String[] res = new String[2];
        res[0] = "-1";
        res[1] = "";

        long articleid = this.findArticle(artistName, dynasty, articleTitle, articleContent);

        if (articleid == ArticleDBOperate.NOT_FOUND) {
            res[1] += "article or artist not found!";
            return res;
        } else if (articleid == ArticleDBOperate.TOO_MANY_MATCH) {
            res[1] += "too many matches for artist or article with current input!";
            return res;
        }

        int criticid = this.findArtist(commentAuthor, "%");

        if (criticid == ArticleDBOperate.NOT_FOUND) {
            res[1] += "comment author info not found!";
            return res;
        } else if (criticid == ArticleDBOperate.TOO_MANY_MATCH){
            res[1] += "too many matches for comment author!";
            return res;
        }

        try {
            PreparedStatement pst = connection.prepareStatement("select id from comments where article_id = ? and critic_id = ? and title like ? and content like ?");
            pst.setLong(1, articleid);
            pst.setInt(2, criticid);
            pst.setString(3, commentTitle);
            pst.setString(4, commentContent);

            ResultSet rs = pst.executeQuery();

            if (rs.next()){
                res[0] = String.valueOf(rs.getLong(1));

                if (rs.next()){
                    res[0] = "-1";
                    res[1] += "too many matched for the comment criteria, please be more specific!";
                }
            }

        } catch (SQLException e) {
            res[0] = "-1";
            res[1] += " "+e.getMessage();
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public String getCommentContent(long commentid) {
        String res = "";

        try {
            PreparedStatement pst = connection.prepareStatement("select content from comments where id = ?");
            pst.setLong(1, commentid);

            ResultSet rs = pst.executeQuery();

            if (rs.next())
                res = rs.getString(1);

        } catch (SQLException e) {
            res = e.getMessage();
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public String[] insertComment(String artistName, String dynasty, String articleTitle, String articleContent, int articleRate, String articleType, String commentTitle, String commentContent, String commentAuthor) {
        String[] res = new String[2];
        res[0] = "-1";
        res[1] = "";

        int artistid = this.findArtist(artistName, dynasty);

        if (artistid == ArticleDBOperate.TOO_MANY_MATCH){
            res[1] += "too many matches for artist! skipping operation...";
            return res;
        } else if (artistid == ArticleDBOperate.NOT_FOUND){
            String[] artistres = this.insertArtist(artistName, dynasty, false);
            if (artistres[0].equals("-1")){
                res[1] += "add new artist failed! "+ artistres[1] + " skipping left operation... ";
                return res;
            } else {
                artistid = Integer.valueOf(artistres[0]);
                res[1] += ("add new artist successfully, its id is " + res[0]);
            }
        }

        long articleid = this.findArticle(artistid, articleTitle, "%" + articleContent + "%");
        if (articleid == ArticleDBOperate.TOO_MANY_MATCH){
            res[1] += " too many matches for article! skipping left operation...";
            return res;
        } else if (articleid == ArticleDBOperate.NOT_FOUND) {
            long articleres = this.insertArticle(artistid,articleTitle,articleContent,articleType,articleRate);
            if (articleres == -1) {
                res[1] += " cannot get article insertion status, skipping left operation...";
                return res;
            } else {
                articleid = articleres;
                res[1] += " add new article successfully, its id is " + articleres;
            }
        }

        int criticid = this.findArtist(commentAuthor, "%");
        if (criticid == ArticleDBOperate.TOO_MANY_MATCH){
            res[1] += " too many matches for comment author, skipping left operation...";
            return res;
        } else if (criticid == ArticleDBOperate.NOT_FOUND){
            String[] criticres = this.insertArtist(commentAuthor, "", false);
            if (criticres[0].equals("-1")){
                res[1] += "fail to add new comment author, skipping left operation...";
                return res;
            }else {
                criticid = Integer.valueOf(criticres[0]);
                res[1] += (" add new comment author successfully, its id is " + criticres[0]);
            }
        }

        try {
            PreparedStatement pst = connection.prepareStatement("insert into comments(article_id, critic_id, title, content) values(?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setLong(1, articleid);
            pst.setInt(2, criticid);
            pst.setString(3, commentTitle);
            pst.setString(4, commentContent);

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();

            if (rs.next())
                res[0] = String.valueOf(rs.getLong(1));

            rs.close();
            pst.close();
        } catch (SQLException e) {
            res[0] = "-1";
            res[1] += " "+e.getMessage();
            e.printStackTrace();
        }

        return res;
    }

}

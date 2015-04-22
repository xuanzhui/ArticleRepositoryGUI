package com.xz.article.gui;

import com.xz.article.db.ArticleDBOperate;
import com.xz.article.db.PostgreSQLImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

/**
 * Created by xuanzhui on 15/4/14.
 */
public class MainFrame extends JFrame {
    private ArticleDBOperate dbOperator;

    private JTextField artistNameField;
    private JTextField dynastyField;

    private JTextField articleTitleField;
    private JTextArea articleContent;
    private JComboBox articleRate;

    private JTextField commentTitleField;
    private JTextArea commentContent;
    private JComboBox commentRate;

    private JTextArea artistOperationEcho;

    public static void main(String[] args) {
        new ConnectionDialFrame(new MainFrame(new PostgreSQLImpl()));
    }

    public MainFrame(final ArticleDBOperate dbOperator){
        //this.setSize(400, 600);

        this.dbOperator = dbOperator;

        this.setTitle("Article Repository");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dbOperator.closeConnection();
            }
        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();

        constraint.insets = new Insets(5,5,5,5);

        //artist area
        JLabel artistLabel = new JLabel("------ Artist Area ------");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.gridwidth = 2;
        this.add(artistLabel, constraint);

        JLabel artistNameLabel = new JLabel("Artist Name");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 1;
        constraint.gridwidth = 1;
        this.add(artistNameLabel, constraint);

        artistNameField = new JTextField(10);
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 1;
        constraint.gridy = 1;
        this.add(artistNameField, constraint);

        JLabel dynastyLabel = new JLabel("Dynasty");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 2;
        constraint.gridy = 1;
        this.add(dynastyLabel, constraint);

        dynastyField = new JTextField(10);
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 3;
        constraint.gridy = 1;
        this.add(dynastyField, constraint);

        final JButton checkArtist = new JButton("Check Artist");

        checkArtist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!((JButton)e.getSource()).isEnabled()){
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkArtist.setEnabled(false);

                        String name = artistNameField.getText().trim();
                        if (name.length() == 0){
                            artistOperationEcho.setText("Artist Name is required!");
                            checkArtist.setEnabled(true);
                            return;
                        }

                        String nameDynasty = name;

                        String dynasty = dynastyField.getText().trim();
                        if (dynasty.length() == 0)
                            dynasty = "%";
                        else
                            nameDynasty = String.format("%s(%s)", name, dynasty);

                        artistOperationEcho.setText("Checking...");

                        if (dbOperator.findPoet(name, dynasty) != -1) {
                            artistOperationEcho.setText(String.format("%s already exists", nameDynasty));
                        }
                        else {
                            artistOperationEcho.setText(String.format("%s doesn't exist", nameDynasty));
                        }

                        checkArtist.setEnabled(true);
                    }
                }).start();
            }
        });

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 2;
        constraint.gridy = 2;
        constraint.gridwidth = 1;
        this.add(checkArtist, constraint);

        final JButton insertArtist = new JButton("Add Artist");

        insertArtist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!((JButton)e.getSource()).isEnabled()){
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        insertArtist.setEnabled(false);

                        String name = artistNameField.getText().trim();
                        if (name.length() == 0){
                            artistOperationEcho.setText("Artist Name is required!");
                            insertArtist.setEnabled(true);
                            return;
                        }

                        String nameDynasty = name;
                        String dynasty = dynastyField.getText().trim();
                        if (dynasty.length() == 0)
                            dynasty = "";
                        else
                            nameDynasty = String.format("%s(%s)", name, dynasty);

                        artistOperationEcho.setText("Inserting...");

                        String res[] = dbOperator.insertPoet(name, dynasty);
                        if (res[0].endsWith("-1")){
                            artistOperationEcho.setText("Insertion failed for "+nameDynasty + ", Error info: "+res[1]);
                        }else{
                            artistOperationEcho.setText("Insertion succeeded for "+nameDynasty + ", Artist id is " + res[0]);
                        }

                        insertArtist.setEnabled(true);
                    }
                }).start();

            }
        });

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 3;
        constraint.gridy = 2;
        constraint.gridwidth = 1;
        this.add(insertArtist, constraint);

        //article area
        JLabel separator1 = new JLabel("------ Article Area ------");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 3;
        constraint.gridwidth = 2;
        this.add(separator1, constraint);

        JLabel articleTitleLabel = new JLabel("Article Title");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 4;
        constraint.gridwidth = 1;
        this.add(articleTitleLabel, constraint);

        articleTitleField = new JTextField();
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 1;
        constraint.gridy = 4;
        constraint.gridwidth = 3;
        this.add(articleTitleField, constraint);

        JLabel articleContentLabel = new JLabel("Article Content");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 5;
        constraint.gridwidth = 2;
        this.add(articleContentLabel, constraint);

        articleContent = new JTextArea(5,20);
        articleContent.setLineWrap(true);
        articleContent.setWrapStyleWord(true);
        constraint.fill = GridBagConstraints.BOTH;
        constraint.gridx = 0;
        constraint.gridy = 6;
        constraint.gridwidth = 4;
        constraint.gridheight = 1;
        this.add(new JScrollPane(articleContent), constraint);

        JLabel titleRateLabel = new JLabel("Article Rate");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 7;
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        this.add(titleRateLabel, constraint);

        JComboBox articleRate = new JComboBox();
        articleRate.addItem("1 (very bad)");
        articleRate.addItem("2 (bad)");
        articleRate.addItem("3 (normal)");
        articleRate.addItem("4 (good)");
        articleRate.addItem("5 (very good)");

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 1;
        constraint.gridy = 7;
        this.add(articleRate, constraint);

        JButton checkArticle = new JButton("Check Article");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 2;
        constraint.gridy = 7;
        this.add(checkArticle, constraint);

        JButton insertArticle = new JButton("Add Article");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 3;
        constraint.gridy = 7;
        this.add(insertArticle, constraint);


        //comment area
        JLabel separator2 = new JLabel("------ Comment Area ------");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 8;
        constraint.gridwidth = 2;
        this.add(separator2, constraint);

        JLabel commentTitleLabel = new JLabel("Comment Title");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 9;
        constraint.gridwidth = 1;
        this.add(commentTitleLabel, constraint);

        commentTitleField = new JTextField();
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 1;
        constraint.gridy = 9;
        constraint.gridwidth = 3;
        this.add(commentTitleField, constraint);

        JLabel commentContentLabel = new JLabel("Comment Content");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 10;
        constraint.gridwidth = 2;
        this.add(commentContentLabel, constraint);

        commentContent = new JTextArea(6,20);
        commentContent.setLineWrap(true);
        commentContent.setWrapStyleWord(true);
        constraint.fill = GridBagConstraints.BOTH;
        constraint.gridx = 0;
        constraint.gridy = 11;
        constraint.gridwidth = 4;
        constraint.gridheight = 1;
        this.add(new JScrollPane(commentContent), constraint);

        JLabel commentRateLabel = new JLabel("Comment Rate");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 12;
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        this.add(commentRateLabel, constraint);

        JComboBox commentRate = new JComboBox();
        commentRate.addItem("1 (very bad)");
        commentRate.addItem("2 (bad)");
        commentRate.addItem("3 (normal)");
        commentRate.addItem("4 (good)");
        commentRate.addItem("5 (very good)");

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 1;
        constraint.gridy = 12;
        this.add(commentRate, constraint);

        JButton checkComment = new JButton("Check Comment");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 2;
        constraint.gridy = 12;
        this.add(checkComment, constraint);

        JButton insertComment = new JButton("Add Comment");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 3;
        constraint.gridy = 12;
        this.add(insertComment, constraint);

        //operation info
        JLabel separator3 = new JLabel("------ Operation Info ------");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 13;
        constraint.gridwidth = 2;
        this.add(separator3, constraint);

        artistOperationEcho = new JTextArea(2, 20);
        //make text area transparent
        artistOperationEcho.setOpaque(false);
        artistOperationEcho.setEditable(false);
        artistOperationEcho.setLineWrap(true);
        artistOperationEcho.setWrapStyleWord(true);
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 14;
        constraint.gridwidth = 4;
        this.add(new JScrollPane(artistOperationEcho), constraint);

        this.pack();

        this.setResizable(false);

        Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) (screen.getWidth() - this.getWidth()) / 2, (int) (screen.getHeight() - this.getHeight()) / 2);

        this.setVisible(false);
    }



    public String[] performConnection(String host, String port, String db, String username, String password){

        String[] res = dbOperator.getConnection(host,port,db,username,password);

        if (res[0].endsWith(ArticleDBOperate.CONN_SUCC)) {
            this.setVisible(true);

            Properties prop = new Properties();
            prop.setProperty("host", host);
            prop.setProperty("port", port);
            prop.setProperty("db", db);
            prop.setProperty("username", username);
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ArticleDBOperate.DB_CONF_CACHE),"UTF-8"));
                prop.store(bufferedWriter, "db connection cache");
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return res;
    }
}

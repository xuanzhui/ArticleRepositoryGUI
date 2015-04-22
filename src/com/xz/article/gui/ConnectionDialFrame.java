package com.xz.article.gui;

import com.xz.article.db.ArticleDBOperate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by xuanzhui on 15/4/15.
 */
public class ConnectionDialFrame extends JFrame {
    private MainFrame mainFrame;

    JTextField hostField = new JTextField(25);
    JTextField dbField = new JTextField(10);
    JTextField portField = new JTextField(6);
    JTextField userField = new JTextField(10);
    JTextField pwField = new JPasswordField(10);
    JProgressBar jProgressBar = new JProgressBar();

    public ConnectionDialFrame(final MainFrame mainFrame){
        this.mainFrame = mainFrame;

        //this.setSize(500, 600);
        this.setTitle("connect info!");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (mainFrame.isVisible() == false)
                    mainFrame.dispose();
            }
        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        //add padding Insets(int top, int left, int bottom, int right)
        constraint.insets = new Insets(5,5,5,5);

        JLabel hostLabel = new JLabel("Host");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 0;
        this.add(hostLabel, constraint);

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.gridwidth = 3;
        this.add(hostField, constraint);

        JLabel dbLabel = new JLabel("Database");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 1;
        constraint.gridwidth = 1;
        this.add(dbLabel, constraint);

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 1;
        constraint.gridy = 1;
        this.add(dbField, constraint);

        JLabel portLabel = new JLabel("Port");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 2;
        constraint.gridy = 1;
        this.add(portLabel, constraint);

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 3;
        constraint.gridy = 1;
        this.add(portField, constraint);

        JLabel userLabel = new JLabel("User Name");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 2;
        this.add(userLabel, constraint);

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 1;
        constraint.gridy = 2;
        this.add(userField, constraint);

        //load db config cache info
        if (Files.exists(Paths.get(ArticleDBOperate.DB_CONF_CACHE))){
            Properties prop = new Properties();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(ArticleDBOperate.DB_CONF_CACHE),"UTF-8"));
                prop.load(bufferedReader);
                this.hostField.setText(prop.getProperty("host",""));
                this.dbField.setText(prop.getProperty("db",""));
                this.portField.setText(prop.getProperty("port",""));
                this.userField.setText(prop.getProperty("username",""));
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JLabel pwLabel = new JLabel("Password");
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 2;
        constraint.gridy = 2;
        this.add(pwLabel, constraint);

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 3;
        constraint.gridy = 2;
        this.add(pwField, constraint);

        final JButton connectbtn = new JButton("Connect");
        connectbtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                connectbtn.setEnabled(false);

                jProgressBar.setVisible(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String host = hostField.getText().trim();
                        String db = dbField.getText().trim();
                        String port = portField.getText().trim();
                        String username = userField.getText().trim();
                        String password = pwField.getText().trim();

                        if (host.length() == 0 || db.length() == 0 || port.length() == 0 ||
                                username.length() == 0) {
                            JOptionPane.showMessageDialog(null, "You must input all necessary fields!", "Error Message", JOptionPane.ERROR_MESSAGE);
                            jProgressBar.setVisible(false);
                            connectbtn.setEnabled(true);
                            return;
                        }

                        String[] conninfo = mainFrame.performConnection(host, port, db, username, password);
                        if (conninfo[0].endsWith(ArticleDBOperate.CONN_SUCC))
                            dispose();
                        else {
                            JOptionPane.showMessageDialog(null, conninfo[1], "Error Message", JOptionPane.ERROR_MESSAGE);
                            jProgressBar.setVisible(false);
                            connectbtn.setEnabled(true);
                        }

                    }
                }).start();



            }
        });

        jProgressBar.setIndeterminate(true);
        jProgressBar.setVisible(false);
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 0;
        constraint.gridy = 3;
        constraint.gridwidth = 3;
        this.add(jProgressBar, constraint);

        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridx = 3;
        constraint.gridy = 3;
        constraint.gridwidth = 1;
        this.add(connectbtn, constraint);

        this.pack();
        this.setResizable(false);
        Dimension screen= Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) (screen.getWidth() - this.getWidth()) / 2, (int) (screen.getHeight() - this.getHeight()) / 2);

        this.setVisible(true);
    }

}

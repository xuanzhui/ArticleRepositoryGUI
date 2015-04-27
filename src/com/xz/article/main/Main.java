package com.xz.article.main;

import com.xz.article.db.PostgreSQLImpl;
import com.xz.article.gui.ConnectionDialFrame;
import com.xz.article.gui.MainFrame;

/**
 * Created by xuanzhui on 15/4/27.
 */
public class Main {
    public static void main(String[] args) {
        new ConnectionDialFrame(new MainFrame(new PostgreSQLImpl()));
    }
}

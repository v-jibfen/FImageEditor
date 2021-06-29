package com.github.vfeng.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by vfeng on 2021/6/29.
 */
public class HtmlConversion {

    // 在<span>标签中提取字体或颜色
    public static String extract(String str, String markName, String endTag) {
        String res = "";
        int start = str.indexOf(markName);
        if (start != -1) {
        str = str.substring(start + markName.length() + 1);
        int end = str.indexOf(endTag);
        res = str.substring(0, end);
        //System.out.println(res);
        }
        return res;
    }

    //<span>转化为<font>标签
    public static String spanConversion(String str) {
        int size = 2;// 设置字号
        String result = "";
        String color = extract(str, "color", "'");
        //System.out.println(color);
        String face = extract(str, "font-family", ";");
        //System.out.println(face);
        String text = "";
        int start = str.indexOf("color");
        if (start != -1) {
            str = str.substring(start + 6);
            start = str.indexOf("'>");
            str = str.substring(start + 2);
            int end = str.indexOf("</span>");
            if (end != -1) {
                text = str.substring(0, end);
                text = text.replace("<u>", "");
                text = text.replace("</u>", "");
                text = text.replace("\r\n", "");
                //System.out.println(text);
            }
        }
        if (color != "" && text != "") {
            result = "<font size=\"" + size + "\" color=\"" + color + "\" face='" + face + "'>" + text + "</font>";
        }
        return result;
    }

    //<p>标签转化为<li>标签
    public static String pConversion(String str) {
        String res = "";
        String[] list = str.split("<span");
        for (int i = 1; i < list.length; i++) {
            res += spanConversion(list[i]);
        }
        return "<li>" + res + "</li>";
    }

    //html5语法转化为html4语法
    public static void converse(String fileName) {
        //设置背景为黑色,<ol>为有序列表
        String content = "<table><tr><td bgcolor=#00000>\n<ol>";
        System.out.println(content);
        File file = new File(fileName);
        BufferedReader reader = null;
        String pMark = "";//<p>标签
        try {
            reader = new BufferedReader(new FileReader(file));
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                pMark += temp;
                if (pMark.contains("<p class") && pMark.contains("</p>")) {
                    content = HtmlConversion.pConversion(pMark);
                    System.out.println(content);
                    pMark = "";
                }
            }
            content = "<ol>\n</td></tr></table>";
            System.out.println(content);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
    }

    public static void main(String[] args) {
        String file = "C:/Users/username/Desktop/test.html";
        converse(file);
    }
}

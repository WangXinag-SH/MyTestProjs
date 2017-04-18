package com.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Rename {

    public static void main(String[] args) throws IOException {
        File root = new File("C:/Users/Administrator/Downloads/QQkongjian_100/r/w");
        File newRoot = new File("C:/Users/Administrator/Downloads/QQkongjian_100/r/w_rename");
        if(newRoot.exists()) {
            newRoot.delete();
        }
        newRoot.mkdir();

        File[] subFiles = root.listFiles();

        int index = 0;

        for(File file : subFiles) {
            String type = file.getName();
            type = type.substring(type.lastIndexOf(".") + 1, type.length());
            System.out.println(type);
            if(type.equals("png")) {
                File newFile = new File(newRoot, "image_" + index + "." + type);
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(newFile));
                DataInputStream dis = new DataInputStream(new FileInputStream(file));
                byte[] buf = new byte[1024 * 8];
                int len = 0;
                while ((len = dis.read(buf)) != -1) {
                    dos.write(buf, 0, buf.length);
                    dos.flush();
                }
                dos.close();
                dis.close();
                index++;
            }
        }
    }
}

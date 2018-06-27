package com.gaowh.NIO2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class fileAPI {
	public static void main(String[] args) {
        //PS: task 是桌面上的一个文本文件
        //获得path方法一,e:/logs/access.log
        Path path = FileSystems.getDefault().getPath("/home/conquer/Desktop", "task");

        System.out.println(path.getNameCount());
        //获得path方法二，用File的toPath()方法获得Path对象
        File file = new File("/home/conquer/Desktop/task");
        Path pathOther = file.toPath();// File 到 Paht的转换
        //0,说明这两个path是相等的
        System.out.println(path.compareTo(pathOther));
        //获得path方法三
        Path path3 = Paths.get("/home/conquer/Desktop", "task");
        System.out.println(path3.toString());

        //join two paths
        Path path4 = Paths.get("/home/conquer/Desktop");
        System.out.println("path4: " + path4.resolve("task"));
        System.out.println("--------------分割线---------------");
        try {
            if(Files.isReadable(path)){
                //注意此处的newBufferedRead的charset参数，如果和所要读取的文件的编码不一致，则会抛出异常
                //java的新特性，不用自己关闭流
                BufferedReader br = Files.newBufferedReader(path, Charset.defaultCharset());//new BufferedReader(new FileReader(new File("e:/logs/access.log")));//
                String line = "";
                while((line = br.readLine()) != null){
                    System.out.println(line);
                }
                br.close();
            }else{
                System.err.println("cannot readable");
            }
        } catch (IOException e) {
            System.err.println("error charset");
        }

    }

    public static void main0(String[] args) {
        /** nio2 */
        Path file = Paths.get("/home/conquer/Desktop/task");
        System.out.println("file name:" + file.getFileName());
        System.out.println("name count:" + file.getNameCount());
        System.out.println("parent:" + file.getParent() + " root:"
                + file.getRoot());


        File file1=file.toFile();

        System.out.println(file1.exists());


        //其他定义方式
        /**Define an Absolute Path*/
//        Path path = Paths.get("C:/rafaelnadal/tournaments/2009/BNP.txt");
//        Path path = Paths.get("C:/rafaelnadal/tournaments/2009", "BNP.txt");
//        Path path = Paths.get("C:", "rafaelnadal/tournaments/2009", "BNP.txt");
//        Path path = Paths.get("C:", "rafaelnadal", "tournaments", "2009", "BNP.txt");
//        /**Define a Path Relative to the File Store Root*/
//        Path path = Paths.get("/rafaelnadal/tournaments/2009/BNP.txt");
//        Path path = Paths.get("/rafaelnadal","tournaments/2009/BNP.txt");
//        /**Define a Path Relative to the Working Folder*/
//        Path path = Paths.get("rafaelnadal/tournaments/2009/BNP.txt");
//        Path path = Paths.get("rafaelnadal","tournaments/2009/BNP.txt");
    }
}

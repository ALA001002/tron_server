package com.bigo.tronserver;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bigo.tronserver.model.TranAddress;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class FileUtil {

    public static void writeData(Queue<TranAddress> data, String path) throws IOException {
        File file =new File(path);
        if(!file.exists()){
            file.createNewFile();
        }
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
        fileWriter.write(JSON.toJSONString(data));
        fileWriter.flush();
        fileWriter.close();
    }

    public static Queue<TranAddress> readData( String path) throws IOException {
        File file =new File(path);
        if(!file.exists()){
            file.createNewFile();
        }
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while((line=bufferedReader.readLine())!=null){
            stringBuilder.append(line);
        }
        bufferedReader.close();
        return JSON.parseObject(stringBuilder.toString(),new TypeReference<Queue<TranAddress>>(){});
    }

    public static void main(String[] args) throws IOException {
        Queue<TranAddress> data = new LinkedList<>();
        data.offer(TranAddress.builder().txId("123").address("23123").build());
        writeData(data,"asd.txt");
        Queue<TranAddress> tronAddresses = readData("asd.txt");
        System.out.println(tronAddresses);
    }
}

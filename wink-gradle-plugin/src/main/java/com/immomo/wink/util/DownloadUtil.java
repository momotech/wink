package com.immomo.wink.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class DownloadUtil {
    public static File downloadFile(String urlPath, String downloadDir) {
        File file = null;
        if(urlPath == null||urlPath.length()==0){
            return file;
        }
        try {
            String fileName = urlPath.substring(urlPath.lastIndexOf('/')+1);
            URL url = new URL(urlPath);
            URLConnection urlConnection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;// http的连接类

            httpURLConnection.setConnectTimeout(1000*5);//设置超时
            httpURLConnection.setRequestMethod("GET");//设置请求方式，默认是GET
            httpURLConnection.setRequestProperty("Charset", "UTF-8");// 设置字符编码
            httpURLConnection.connect();// 打开连接

            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

            String path = downloadDir +File.separator+ fileName;// 指定存放位置
            file = new File(path);
            // 校验文件夹目录是否存在，不存在就创建一个目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            OutputStream out = new FileOutputStream(file);
            int size = 0;

            byte[] b = new byte[2048];
            //把输入流的文件读取到字节数据b中，然后输出到指定目录的文件
            while ((size = bin.read(b)) != -1) {
                out.write(b, 0, size);
            }
            // 关闭资源
            bin.close();
            out.close();
            WinkLog.i("文件下载成功");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            WinkLog.i("文件下载失败");
        }

        return file;
    }

    public static File[] downloadFiles(List<String> urls, String downloadDir){
        File[] files = new File[urls.size()];
        for(int i=0; i<urls.size();i++){
            files[i] = downloadFile(urls.get(i),downloadDir);
        }
        return files;
    }

}

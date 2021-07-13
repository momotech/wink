package com.immomo.wink.utils;

import com.intellij.openapi.util.SystemInfo;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pengwei on 16/9/11.
 */
public final class Utils {

    public static final String BREAK_LINE = System.getProperty("line.separator");

    /**
     * 获取python安装目录
     *
     * @return
     */
    public static String getPythonLocation() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"python", "--version"});
            if (process.waitFor() == 0) {
                return "python";
            }
        } catch (Exception e) {
        }
        try {
            if (!SystemInfo.isWindows) {
                process = Runtime.getRuntime().exec(new String[]{"whereis", "python"});
                if (process != null && process.getInputStream() != null) {
                    String result = StreamUtil.inputStream2String(process.getInputStream());
                    if (notEmpty(result)) {
                        return result;
                    }
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    public static boolean notEmpty(String text) {
        return (text != null && text.trim().length() != 0);
    }

    /**
     * 打开浏览器
     *
     * @param url
     */
    public static void openUrl(String url) {
        if (SystemInfo.isWindows) {
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                URI uri = new URI(url);
                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                }
                if (desktop != null)
                    desktop.browse(uri);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 模拟键盘输入
     * @param r
     * @param key
     */
    public static void keyPressWithCtrl(Robot r, int key) {
        if (r == null) {
            return;
        }
        r.keyPress(KeyEvent.VK_CONTROL);
        r.keyPress(key);
        r.keyRelease(key);
        r.keyRelease(KeyEvent.VK_CONTROL);
        r.delay(100);
    }


    public static String getErrorString(Throwable e) {
        return getErrorString(null,e);
    }


    public static String getErrorString(String tag,Throwable e) {
        StringBuilder sb = new StringBuilder();
        if(tag!=null){
            sb.append(tag+"\n");
        }
        sb.append(e.getMessage()+"\n");
        StackTraceElement[] elements = e.getStackTrace();
        int shortLength = elements.length>5?5:elements.length;
        for(int i=0;i<shortLength;i++){
            sb.append(elements[i].toString()+"\n");
        }
        return sb.toString();
    }

    public static Map<String,String> stringArrayToMap(String[] arr) {
        HashMap<String,String> strMap=new HashMap<>();
        for(String str:arr){
            strMap.put(str,str);
        }
        return strMap;
    }

}

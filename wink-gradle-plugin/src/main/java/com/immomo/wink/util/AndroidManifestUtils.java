package com.immomo.wink.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AndroidManifestUtils {

    /**
     * 解析入口activity
     */
    private static String findLauncherActivity(Document doc) {
        Node activity = null;
        String sTem = "";
        NodeList categoryList = doc.getElementsByTagName("category");
        for (int i = 0; i < categoryList.getLength(); i++) {
            Node category = categoryList.item(i);
            NamedNodeMap attrs = category.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                if (attrs.item(j).getNodeName().equals("android:name")) {
                    if (attrs.item(j).getNodeValue().equals("android.intent.category.LAUNCHER")) {
                        activity = category.getParentNode().getParentNode();
                        break;
                    }
                }
            }
        }
        if (activity != null) {
            NamedNodeMap attrs = activity.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                if (attrs.item(j).getNodeName().equals("android:name")) {
                    sTem = attrs.item(j).getNodeValue();
                }
            }
        }
        return sTem;
    }

    public static String findLauncherActivity(String filePath, String packageName) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // 创建DocumentBuilder对象
            DocumentBuilder db = dbf.newDocumentBuilder();
            //加载xml文件
            Document document = null;
            try {
                document = db.parse(filePath);
                String launcherActivity = findLauncherActivity(document);
                if (launcherActivity.startsWith(".")) {
                    launcherActivity = packageName + launcherActivity;
                }
                return launcherActivity;
            } catch (SAXException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

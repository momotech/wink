package com.immomo.wink.patch;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Utils {

    public static String getVersionFolderPath(Context context) {
        String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/" + context.getPackageName() + "/patch_version/";
        File dexFile = new File(dexPath);
        if (dexFile.exists()) {
            return dexPath;
        }
        dexPath = "/sdcard/wink/patch_version/";
        File dexFile2 = new File(dexPath);
        if (dexFile2.exists()) {
            return dexPath;
        }
        return "";
    }

    public static File getVersionFile(Context context) {
        String patchName = FixDexUtil.getPatchVersion(context) + ".png";
        String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/" + context.getPackageName() + "/patch_version/" + patchName;

        Log.e("weijiangnan", "version" + patchName);
        File dexFile = new File(dexPath);
        if (dexFile.exists()) {
            return dexFile;
        }

        // 拷贝
        File dexFile2 = new File("/sdcard/wink/patch_version/" + patchName);
        if (dexFile2.exists()) {
            return dexFile2;
        }

        return null;
    }


    public static boolean deleteFile(File dirFile) {
        if (!dirFile.exists()) {
            return false;
        }
        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {
            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }
        return dirFile.delete();
    }

    public static boolean copyFile(File src, String destPath) {
        boolean result = false;
        if ((src == null) || (destPath== null)) {
            return result;
        }
        File dest= new File(destPath);
        if (dest!= null && dest.exists()) {
            dest.delete(); // delete file
        }
        try {
            dest.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;

        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dest).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}

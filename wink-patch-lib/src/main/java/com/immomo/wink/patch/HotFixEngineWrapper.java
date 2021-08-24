package com.immomo.wink.patch;

import android.content.Context;
import android.util.Log;


import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * @describe 热修复的步骤：
 *  step1：构建DexClassLoader来加载补丁文件；
 *  step2：通过反射BaseDexClassLoader和DexPathList这2个类去拿到Element数组（里面就是一个或多个dex文件）；
 *       step2.1:先反射BaseDexClassLoader中变量: DexPathList pathList；
 *       step2.2:再反射DexPathList中 变量 Element[] dexElements；
 *  step3：将已经加载的apk中的Element数组和补丁中的Element数组合并，把我们的补丁dex放在数组的最前面；
 *  step4：通过反射给PathList里面的Element[] dexElements赋值
 */
public final class HotFixEngineWrapper {
    private static final String BASE_DEX_CLASSLOADER = "dalvik.system.BaseDexClassLoader";
    private static final String PATH_LIST_FIELD = "pathList";
    private static final String DEX_ELEMENTS_FIELD = "dexElements";
//    private static String dexPath;
    private static String optPath;
    public static final HotFixEngineWrapper INSTANCE;

    private HotFixEngineWrapper() {
    }

    static {
        HotFixEngineWrapper var0 = new HotFixEngineWrapper();
        INSTANCE = var0;
//        dexPath = "";
        optPath = "";
    }
    private final Object getFieldValue(Object obj, Class clazz, String fieldName) {
        Field declaredField = null;
        System.out.println("getFieldValue clazz=" + clazz.getName() + ", fieldName=" + fieldName);
        try {
            declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            return declaredField.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final void setField(Object obj, Class clazz, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field var = clazz.getDeclaredField(field);
        Field declaredField = var;
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    private final Object getPathList(BaseDexClassLoader baseDexClassLoader) {
        Class var10000 = null;
        try {
            var10000 = Class.forName(BASE_DEX_CLASSLOADER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Class bdClazz = var10000;
        return this.getFieldValue(baseDexClassLoader, bdClazz, PATH_LIST_FIELD);
    }

    private final Object getDexElements(Object dexPathList) {
        return this.getFieldValue(dexPathList, dexPathList.getClass(), DEX_ELEMENTS_FIELD);
    }

    private final void findClass(Object loader) {
        Class var10000 = loader.getClass().getSuperclass();
        Method declareMethod_1 = null;
        try {
            declareMethod_1 = var10000 != null ? var10000.getDeclaredMethod("findClass", String.class) : null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (declareMethod_1 != null) {
            declareMethod_1.setAccessible(true);
        }

        Object classs = null;
        try {
            classs = declareMethod_1 != null ? declareMethod_1.invoke(loader, "com.google.samples.apps.sunflower.CpTest") : null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        StringBuilder var10001 = (new StringBuilder()).append("classss:").append(classs).append(" hash:");
        boolean var5 = false;
        Log.e("weijiangnan", var10001.append(classs != null ? classs.hashCode() : 0).toString());
    }

    // Version 文件用来给设备标记当前补丁版本号
    // 检测 Version.png 文件与 BuildConfig 版本是否相同，不同则删除对应 Version 文件
    public final void checkVersion(Context context) {
        File versionFile = Utils.getVersionFile(context);
        if (versionFile == null || !versionFile.exists()) {
            String versionPath = Utils.getVersionFolderPath(context);
            File versionFolder = new File(versionPath);
            Utils.deleteFile(versionFolder);
        }
    }

    public final void loadPatch( Context context) {
        File dexFile = FixDexUtil.getDexPatchFile(context);
        if (dexFile != null && dexFile.exists() && dexFile.canRead()) {
            StringBuilder var10000 = new StringBuilder();
            File var10001 = context.getFilesDir();
            optPath = var10000.append(var10001.getPath()).append("/opt_dex").toString();
            File optFile = new File(optPath);
            if (!optFile.exists()) {
                optFile.mkdirs();
            }

//            File dexFile = new File(dexFile2.getAbsolutePath());
            if (!dexFile.exists()) {
                Log.e("weijiangnan", "file no found");
            } else {
                Log.e("weijiangnan", "file found");
            }

            Log.e("weijiangnan", "1");
            ClassLoader var12 = context.getClassLoader();
            if (var12 == null) {
                throw new NullPointerException("null cannot be cast to non-null type dalvik.system.PathClassLoader");
            } else {
                PathClassLoader pathClassLoader = (PathClassLoader)var12;
                DexClassLoader dexClassLoader = new DexClassLoader(dexFile.getAbsolutePath(), optPath, (String)null, (ClassLoader)pathClassLoader);
                Object pathPathList = this.getPathList((BaseDexClassLoader)pathClassLoader);
                Object dexPathList = this.getPathList((BaseDexClassLoader)dexClassLoader);
                Log.e("weijiangnan", "2");
                Object pathElements = this.getDexElements(pathPathList);
                Object dexElements = this.getDexElements(dexPathList);
                Log.e("weijiangnan", "" + dexElements);
                Object combineElements = this.combineArray(dexElements, pathElements);
                Object pathList = this.getPathList((BaseDexClassLoader)pathClassLoader);
                try {
                    this.setField(pathList, pathList.getClass(), DEX_ELEMENTS_FIELD, combineElements);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                Log.e("weijiangnan", "3");
            }
        }
    }

    private final Object combineArray(Object pathElements, Object dexElements) {
        if (pathElements == null || dexElements == null)
            return null;
        Class clazz = pathElements.getClass().getComponentType();
        int peLength = Array.getLength(pathElements);
        int deLength = Array.getLength(dexElements);
        Log.e("weijiangnan", "deLength" + deLength);
        Object var10000 = Array.newInstance(clazz, peLength + deLength);
//        Intrinsics.checkNotNullExpressionValue(var10000, "Array.newInstance(clazz, peLength + deLength)");
        Object newArrays = var10000;
        System.arraycopy(pathElements, 0, newArrays, 0, peLength);
        System.arraycopy(dexElements, 0, newArrays, peLength, deLength);
        Log.e("weijiangnan", "newArrays" + newArrays);
        int index = 0;
        int var8 = Array.getLength(newArrays) - 1;
        if (index <= var8) {
            while(true) {
                Log.e("weijiangnan", "newArrays3" + Array.get(newArrays, index));
                if (index == var8) {
                    break;
                }

                ++index;
            }
        }

        return newArrays;
    }

}

package com.immomo.wink;

import com.immomo.wink.utils.FileWinkUtils;

import java.io.File;

public class Test {
    public static void main(String[] args) {
        //FileWinkUtils.checkPluginInstalled(new File("/Users/zhoukai/Desktop/momo-video/liveCamera/liveCamera"));
        try {
            FileWinkUtils.installPlugin(new File("/Users/zhoukai/Desktop/liveRender/LiveRender"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

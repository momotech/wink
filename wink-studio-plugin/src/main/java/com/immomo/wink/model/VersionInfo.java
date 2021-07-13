package com.immomo.wink.model;

import java.util.List;

public class VersionInfo {
    String ideaVersion;
    List<String> pluginVersion;

    public VersionInfo(String ideaVersion, List<String> pluginVersion){
        this.ideaVersion = ideaVersion;
        this.pluginVersion = pluginVersion;
    }

    public interface VersionCallBack{
        void onVersionReceive(String ideaVersion,List<String> pluginVersions);
    }
}

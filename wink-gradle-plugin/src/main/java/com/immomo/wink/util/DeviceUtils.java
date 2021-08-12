package com.immomo.wink.util;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeviceUtils {

    @NotNull
    public static List<String> getConnectingDevices() {
        List<String> devicesList = new ArrayList<>();

        String cmds = "";
        cmds += " source ~/.bash_profile";
        cmds += '\n' + "adb devices ";

        Utils.ShellResult shellResult = Utils.runShells(cmds);
        List<String> resultList = shellResult.getResult();
        if (resultList == null || resultList.size() <= 1) { // 没有设备连接
            WinkLog.i("USB 没有连接到设备");
        } else {
            List<String> devices = resultList.subList(1, resultList.size());
            System.out.println("adb_devices 111 : " + devices.toString());
            for (String deviceStr : devices) {
                String [] arr = deviceStr.split("\\s+");
                System.out.println(arr[0]);
//                Utils.runShells("adb -s " + arr[0] + " install ");
                if (!TextUtils.isEmpty(arr[0])) {
                    devicesList.add(arr[0]);
                }
            }
        }
        return devicesList;
    }

}

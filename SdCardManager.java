package com.coderminer.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;


public class SdCardManager {
    
    public static final String INNER = Environment.getExternalStorageDirectory().getAbsolutePath();
    
    public static List<String> getStoragePaths(Context cxt) {
        List<String> pathsList = new ArrayList<String>();
        StorageManager storageManager = (StorageManager) cxt.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
            method.setAccessible(true);
            Object result = method.invoke(storageManager);
            if (result != null && result instanceof String[]) {
                String[] pathes = (String[]) result;
                StatFs statFs;
                for (String path : pathes) {
                    if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                        statFs = new StatFs(path);
                        if (statFs.getBlockCount() * statFs.getBlockSize() != 0) {
     
                            pathsList.add(si);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            File externalFolder = Environment.getExternalStorageDirectory();
            if (externalFolder != null) {
				pathsList.add(externalFolder.getAbsolutePath());
            }
        }
        return pathsList;
    }
}
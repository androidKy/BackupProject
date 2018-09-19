package com.littlerich.holobackup.data;

import android.Manifest;
import android.os.Environment;

import java.io.File;

public class Constant {

    private static final String BASE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator;

    /**
     * 存储APP安装包：
     */
    public static final String BACKUP_FOLDER = BASE_PATH + "QX_Backup" + File.separator + "APP" + File.separator;

    /**
     * 存储APP数据：/app/data/包名
     * 已ROOT方可
     */
    public static final String BACKUP_FOLDER_APP_DATA = BACKUP_FOLDER + "data" + File.separator;

    // for permission android M (6.0)
    public static String[] ALL_REQUIRED_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

}

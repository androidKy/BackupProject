package com.batmobi;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * description:
 * author: kyXiao
 * created date: 2018/9/12
 */

public class BackupConstant {
    public static final String TAI_BACKUP = "tai";  //钛备份
    public static final String HELIUM = "helium";   //氦备份

    public static final int BACKUP_TIMEOUT = 15000; //备份超时的时间
    //压缩包存放的上级路径
    public static final String ZIP_DIR_NAME = "zipFile";

    public static String AID = ZIP_DIR_NAME;
    public static String UID = ZIP_DIR_NAME;
    //FTP的ip地址
    public static final String FTP_ADDRESS = "192.168.31.244";
    //FTP的文件夹
    public static final String FTP_BACKUP_PATH = "/RomBackUp/";

    //sdcard路径
    public static final String SDCARD_PATH = File.separator + "sdcard" + File.separator;
    //压缩包存放路径
    public static final String ZIP_FILE_PATH = SDCARD_PATH + ZIP_DIR_NAME + File.separator;
    //下载下来的文件存放路径
    public static final String DOWNLOAD_FILE_PATH = SDCARD_PATH;

    //app数据备份路径
    public static String getBackupFolder(String destDir) {
        String backupFolder = "";

        if (!TextUtils.isEmpty(destDir)) {
            backupFolder = destDir + File.separator;
        } else {
            /**
             * 存储APP数据：/backup/包名
             * 已ROOT方可
             */

            if (Environment.isExternalStorageEmulated()) {
                backupFolder = Environment.getExternalStorageDirectory().toString() + File.separator +
                        "backupData" + File.separator;
            } else {
                backupFolder = "/sdcard" + File.separator + "backupData" + File.separator;
            }
        }
        return backupFolder;
    }
}

package com.batmobi;

import android.content.Context;

import com.batmobi.backup.BackupForShell;
import com.batmobi.backup.IBackup;
import com.batmobi.download.DownloadImpl;
import com.batmobi.download.IDownload;
import com.batmobi.restore.IRestore;
import com.batmobi.restore.RestoreForShell;
import com.batmobi.upload.IUpload;
import com.batmobi.upload.UploadImpl;
import com.batmobi.zip.IZip;
import com.batmobi.zip.ZipImpl;


/**
 * description: 简单工厂
 * author: kyXiao
 * created date: 2018/9/12
 */

class SimpleFactory {
    private static final String TAG = "SimpleFactory";

    static IBackup createBackupObj(String backupWay) {

        IBackup backupObj = null;

        switch (backupWay) {
            case BackupConstant.TAI_BACKUP:
                backupObj = new BackupForShell();
                break;
            case BackupConstant.HELIUM:
                //backupObj = new BackupForHelium();
                break;
        }

        return backupObj;
    }

    static IRestore createRestoreObj(String restoreWay) {
      IRestore restoreObj = null;

        switch (restoreWay) {
            case BackupConstant.TAI_BACKUP:
                restoreObj = new RestoreForShell();
                break;
            case BackupConstant.HELIUM:
               // restoreObj = new RestoreForHelium();
                break;
        }

        return restoreObj;
    }

    static IZip createZiper(Context context){
        return ZipImpl.getInstance(context);
    }

    static IUpload createUploader()
    {
        return UploadImpl.getInstance();
    }

    static IDownload createDownloader()
    {
        return new DownloadImpl();
    }
}

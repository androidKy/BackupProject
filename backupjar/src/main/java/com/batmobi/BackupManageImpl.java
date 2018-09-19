package com.batmobi;

import android.content.Context;

import com.batmobi.backup.IBackup;
import com.batmobi.download.IDownload;
import com.batmobi.restore.IRestore;
import com.batmobi.upload.IUpload;
import com.batmobi.util.CommandUtil;
import com.batmobi.util.LogUtil;
import com.batmobi.util.ThreadUtil;
import com.batmobi.util.ZipUtils;
import com.batmobi.zip.IZip;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * description: 备份管理实现类
 * author: kyXiao
 * created date: 2018/9/12
 */

public class BackupManageImpl implements IManager {
    private static final String TAG = "BackupManageImpl";

    private Context mContext;

    private String BACKUP_WAY = BackupConstant.TAI_BACKUP;
    private String mDestDir = "";

    private IBackup mBackupObj = null;
    private IResponListener mResponListener;
    private List<String> mPackageNameList;
    private boolean mIsOperating = false;

    private static volatile BackupManageImpl mInstance;

    private BackupManageImpl() {

    }

    public static BackupManageImpl getInstance() {
        if (mInstance == null) {
            synchronized (BackupManageImpl.class) {
                if (mInstance == null)
                    mInstance = new BackupManageImpl();
            }
        }

        return mInstance;
    }

    @Override
    public BackupManageImpl setContext(Context context) {
        if (context != null)
            mContext = context.getApplicationContext();
        return this;
    }

    @Override
    public BackupManageImpl setBackupWay(String type) {
        BACKUP_WAY = type;
        return this;
    }

    @Override
    public BackupManageImpl setBackupDestDir(String destDir) {
        mDestDir = destDir;
        return this;
    }

    @Override
    public BackupManageImpl setUid(String uid) {
        BackupConstant.UID = uid;
        return this;
    }

    @Override
    public BackupManageImpl setAid(String aid) {
        BackupConstant.AID = aid;
        return this;
    }


    @Override
    public void backup(String packageName) {
        backup(packageName, null);
    }

    //备份完app数据到sdcard后
    //再压缩sdcard到zipFile目录下
    //然后上传到FTP,清楚备份的数据
    @Override
    public void backup(String packageName, IResponListener responListener) {
        List<String> packageNameList = new LinkedList<>();
        packageNameList.add(packageName);

        backup(packageNameList, responListener);
    }

    @Override
    public void backup(List<String> packageNameList, IResponListener responListener) {
        mResponListener = responListener;
        if (mContext == null) {
            //mResponListener.onResponFailed("未设置context");
            onFailed("未设置context");
            return;
        }
        LogUtil.out(TAG, "mIsOperating = " + mIsOperating);
        if (mIsOperating) {
            onFailed("正在备份...请等待完成再进行操作");
            return;
        }
        mIsOperating = true;

        backAppData(packageNameList);
    }

    /**
     * 1、下载服务器的备份压缩包
     * 2、解压到sdcard/zipDir
     * 3、清空sdcard数据并移动加压的数据到sdcard上
     * 4、恢复backupData目录下的app数据
     *
     * @param packageName
     */
    @Override
    public void restore(String packageName) {
        restore(packageName, null);
    }

    @Override
    public void restore(String packageName, IResponListener responListener) {
        List<String> packageNameList = new LinkedList<>();
        packageNameList.add(packageName);

        restore(packageNameList, responListener);
    }

    @Override
    public void restore(List<String> packageNameList, IResponListener responListener) {
        mResponListener = responListener;
        mPackageNameList = new LinkedList<>();
        mPackageNameList.clear();
        mPackageNameList.addAll(packageNameList);

        if (mIsOperating) {
            onFailed("正在进行恢复备份，请等待完成再次操作");
            return;
        }
        mIsOperating = true;

        download();
    }

    @Override
    public void removeBackup(String packageName) {
        removeBackup(packageName, null);
    }

    @Override
    public void removeBackup(String packageName, IResponListener responListener) {
        List<String> packageNameList = new LinkedList<>();
        packageNameList.add(packageName);
        removeBackup(packageNameList, responListener);
    }

    @Override
    public void removeBackup(List<String> packageNameList, IResponListener responListener) {

        if (mIsOperating) {
            onFailed("正在删除备份，请等待完成再次操作");
            return;
        }
        mIsOperating = true;

        mBackupObj = SimpleFactory.createBackupObj(BACKUP_WAY);
        mBackupObj.removeBackup(packageNameList, mDestDir, responListener);
    }

    /**
     * 备份app数据
     *
     * @param packageNameList
     */
    private void backAppData(List<String> packageNameList) {
        mBackupObj = SimpleFactory.createBackupObj(BACKUP_WAY);
        if (mBackupObj == null) {
            onFailed("暂时没有这种备份方式");
            return;
        }
        mBackupObj.startBackup(packageNameList, mDestDir, new IResponListener() {
            @Override
            public void onResponSuccess() {
                LogUtil.out(TAG, "备份APP数据成功");
                zipFile();
            }

            @Override
            public void onResponFailed(String msg) {
                LogUtil.error(TAG, "backAppData : 备份APP数据失败：" + msg);
                onFailed("onBackUpFailed：" + msg);
            }
        });
    }

    /**
     * 压缩
     */
    private void zipFile() {
        IZip zip = SimpleFactory.createZiper(mContext);
        zip.addZipListener(new IZip.IZipListener() {
            @Override
            public void onZipSuccess() {
                LogUtil.out(TAG, "压缩sdcard数据成功");
                uploadFile();
            }

            @Override
            public void onZipFailed(String failedMsg) {
                LogUtil.error(TAG, "压缩sdcard数据失败：" + failedMsg);
                onFailed("onZipFailed : " + failedMsg);
            }
        });
        zip.startZip(BackupConstant.SDCARD_PATH);
    }

    /**
     * 上传
     */
    private void uploadFile() {
        IUpload upload = SimpleFactory.createUploader();
        upload.upload(mContext, new IUpload.IUploadListener() {
            @Override
            public void onUploadSuccess() {
                ZipUtils.deleteFile(new File(BackupConstant.ZIP_FILE_PATH));
                onSucceed();
            }

            @Override
            public void onUploadFailed(String msg) {
                onFailed("onUploadFailed : " + msg);
            }
        });
    }


    /**
     * 下载
     */
    private void download() {
        IDownload download = SimpleFactory.createDownloader();
        download.addListener(new IDownload.IDownloadListener() {
            @Override
            public void onDownloadProcess(String msg, int progress) {
                // LogUtil.out(TAG,"");
            }

            @Override
            public void onDownloadSuccess(String fileName) {
                LogUtil.out(TAG, "onDownloadSuccess localFilePath : " + fileName);
                unZip(fileName);
            }

            @Override
            public void onDownloadFailed(String errorMsg) {
                LogUtil.out(TAG, "onDownloadFailed: " + errorMsg);
                onFailed("下载备份失败：" + errorMsg);
            }
        });
        download.download();
    }

    /**
     * 解压
     */
    private void unZip(String fileName) {
        File file = new File(BackupConstant.DOWNLOAD_FILE_PATH + fileName);
        if (file.exists()) {
            //删除sdcard上的原有数据，但不删除压缩包
            deleteFile(new File(BackupConstant.SDCARD_PATH), fileName);
            //解压到sdcard上
            ZipUtils.unZip(file.getAbsolutePath(), BackupConstant.DOWNLOAD_FILE_PATH);
            //移动到sdcard目录下
            removeData2sdcard(fileName);
        } else {
            onFailed("找不到解压的备份压缩包");
        }
    }

    /**
     * 移动数据到sdcard目录下
     */
    private void removeData2sdcard(final String fileName) {
        String command = "cp -ar " + "/sdcard/sdcard/*" + " " + BackupConstant.SDCARD_PATH;
        CommandUtil.sendCommand(command, new CommandUtil.OnResponListener() {
            @Override
            public void onSuccess(List<String> responList) {
                deleteZipPackage(fileName);
                recoverAppData();
            }

            @Override
            public void onFailed(String msg) {
                BackupManageImpl.this.onFailed("移动数据失败： " + msg);
            }
        });
    }

    /**
     * 删除压缩包
     *
     * @param zipFileName
     */
    private void deleteZipPackage(String zipFileName) {
        File file = new File(BackupConstant.SDCARD_PATH);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File file1 : files) {
                    String fileName = file1.getName();
                    if (fileName.equals("sdcard") || fileName.equals(zipFileName)) {
                        ZipUtils.deleteFile(file1);
                        file1.delete();
                    }
                }
            }
        }
    }

    /**
     * 恢复APP数据
     */
    private void recoverAppData() {
        IRestore restore = SimpleFactory.createRestoreObj(BackupConstant.TAI_BACKUP);
        restore.restore(mPackageNameList, mDestDir, new IResponListener() {
            @Override
            public void onResponSuccess() {
                onSucceed();
            }

            @Override
            public void onResponFailed(String msg) {
                onFailed(msg);
            }
        });
    }

    /**
     * 删除sdcard上的数据除了压缩包不删除
     *
     * @param path
     * @param zipFileName
     */
    private void deleteFile(File path, String zipFileName) {
        if (!path.exists())
            return;
        if (path.isFile()) {
            if (!path.getName().equals(zipFileName))
                path.delete();
            return;
        }
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteFile(files[i], zipFileName);
        }
        path.delete();
    }


    private void onFailed(final String msg) {
        mIsOperating = false;
        ThreadUtil.main(new Runnable() {
            @Override
            public void run() {
                if (mResponListener != null) {
                    mResponListener.onResponFailed(msg);
                }
            }
        });
    }

    private void onSucceed() {
        mIsOperating = false;
        ThreadUtil.main(new Runnable() {
            @Override
            public void run() {
                if (mResponListener != null)
                    mResponListener.onResponSuccess();
            }
        });
    }
}

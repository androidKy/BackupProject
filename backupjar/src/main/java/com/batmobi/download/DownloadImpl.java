package com.batmobi.download;

import android.text.TextUtils;

import com.batmobi.BackupConstant;
import com.batmobi.util.FTPManager;
import com.batmobi.util.ThreadUtil;
import com.batmobi.util.ZipUtils;

import java.io.File;

/**
 * description: 下载实现类
 * author: kyXiao
 * created date: 2018/9/18
 */

public class DownloadImpl implements IDownload {
    private static final String TAG = "DownloadImpl";

    private static final int DOWNLOAD_TIME_OUT = 5 * 60 * 1000; //下载超时时间默认5分钟
    private int mStartTime = 0;

    private IDownloadListener mDownloadListener;
    private String mDownloadPath;
    private String mNewFileName;

    @Override
    public void addListener(IDownloadListener downloadListener) {
        mDownloadListener = downloadListener;
    }

    @Override
    public void download() {
        //初始化存放下载下来的zip包的存放路径
        init();
        mDownloadPath = String.format("%s%s/%s/", BackupConstant.FTP_BACKUP_PATH,
                BackupConstant.UID, BackupConstant.AID);
        ThreadUtil.async(new DownloadTask());
    }

    private class DownloadTask implements Runnable {

        @Override
        public void run() {
            FTPManager ftpManager = null;

            try {
                ftpManager = new FTPManager();

                if (ftpManager.connect(BackupConstant.FTP_ADDRESS, "Anonymous", "")) {
                    //查询服务器此路径下最新的文件
                    mNewFileName = ftpManager.getNewestFileName(mDownloadPath);
                    if (TextUtils.isEmpty(mNewFileName)) {
                        onFailed("此uid/aid下没有可用文件");
                        return;
                    }
                    //开始下载
                    ftpManager.downloadFile(BackupConstant.DOWNLOAD_FILE_PATH, mDownloadPath + "/" + mNewFileName, new FTPManager.IProgressListener() {
                        @Override
                        public void onProgress(String msg, int progress) {
                            DownloadImpl.this.onProgress(msg, progress);
                        }

                        @Override
                        public void onSuccess(String filePath, int ts) {
                            onSucceed(mNewFileName);
                        }

                        @Override
                        public void onFailed(String errorMsg, int ts) {
                            DownloadImpl.this.onFailed(errorMsg);
                        }
                    });
                    ftpManager.closeFTP();
                } else {
                    onFailed("ftp连接失败");
                }
            } catch (Exception e) {
                e.getMessage();
                onFailed("下载过程出现异常：" + e.getMessage());
            }
        }
    }

    private void init() {
        File file = new File(BackupConstant.DOWNLOAD_FILE_PATH);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
    }


    private void onSucceed(String filePath) {
        if (mDownloadListener != null)
            mDownloadListener.onDownloadSuccess(filePath);
    }

    private void onFailed(String errorMsg) {
        if (mDownloadListener != null)
            mDownloadListener.onDownloadFailed(errorMsg);
    }

    private void onProgress(String msg, int progress) {
        if (mDownloadListener != null)
            mDownloadListener.onDownloadProcess(msg, progress);
    }
}

package com.batmobi.download;

import android.text.TextUtils;

import com.batmobi.BackupConstant;
import com.batmobi.util.FTPManager;
import com.batmobi.util.ThreadUtil;
import com.batmobi.util.ZipUtils;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;

/**
 * description: 下载实现类
 * author: kyXiao
 * created date: 2018/9/18
 */

public class DownloadImpl implements IDownload {
    private static final String TAG = "DownloadImpl";

    private IDownloadListener mDownloadListener;
    private String mDownloadPath;
    //private String mNewFileName;

    private String mFtpIp;
    private String mUid;
    private String mAid;
    private String mFileName;

    @Override
    public void setParams(String ftpIp, String uid, String aid, String fileName) {
        this.mFtpIp = ftpIp;
        this.mUid = uid;
        this.mAid = aid;
        this.mFileName = fileName;
    }

    @Override
    public void addListener(IDownloadListener downloadListener) {
        mDownloadListener = downloadListener;
    }

    @Override
    public void download() {
        //初始化存放下载下来的zip包的存放路径
        init();
        mDownloadPath = String.format("%s%s/%s/", BackupConstant.FTP_BACKUP_PATH,
                mUid, mAid);
        ThreadUtil.async(new DownloadTask());
    }

    private class DownloadTask implements Runnable {

        @Override
        public void run() {
            FTPManager ftpManager = null;

            try {
                ftpManager = new FTPManager();

                if (ftpManager.connect(mFtpIp, "Anonymous", "")) {
                    //查询服务器此路径下是否存在查询的文件
                    // mNewFileName = ftpManager.getNewestFileName(mDownloadPath);
                    boolean isFileExisting = false;
                    FTPFile[] ftpFiles = ftpManager.listFiles(mDownloadPath);
                    if (ftpFiles != null && ftpFiles.length > 0) {
                        for (FTPFile file : ftpFiles) {
                            if (file.getName().equals(mFileName)) {
                                isFileExisting = true;
                            }
                        }
                    } else {
                        isFileExisting = false;
                    }

                    if (!isFileExisting) {
                        onFailed("此uid/aid下没有可用文件");
                        return;
                    }

                    //开始下载
                    ftpManager.downloadFile(BackupConstant.DOWNLOAD_FILE_PATH, mDownloadPath + mFileName, new FTPManager.IProgressListener() {
                        @Override
                        public void onProgress(String msg, int progress) {
                            DownloadImpl.this.onProgress(msg, progress);
                        }

                        @Override
                        public void onSuccess(String filePath, int ts) {
                            onSucceed(mFileName);
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

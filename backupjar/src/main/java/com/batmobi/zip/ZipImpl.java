package com.batmobi.zip;

import android.content.Context;
import android.provider.Settings;

import com.batmobi.BackupConstant;
import com.batmobi.util.LogUtil;
import com.batmobi.util.ThreadUtil;
import com.batmobi.util.ZipUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * description:压缩实现类
 * author: kyXiao
 * created date: 2018/9/17
 */

public class ZipImpl implements IZip {
    private static final String TAG = "ZipImpl";

    private static volatile ZipImpl mInstance;
    private WeakReference<Context> mContextWeakReference;
    private IZipListener mZipListener;
    private boolean mIsZipping;

    private String uid;
    private String aid;

    private ZipImpl(Context context) {
        mContextWeakReference = new WeakReference<Context>(context);
    }

    public static ZipImpl getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ZipImpl.class) {
                if (mInstance == null) {
                    mInstance = new ZipImpl(context);
                }
            }
        }
        return mInstance;
    }

    @Override
    public void startZip(String filePath) {
        if (mIsZipping) {
           /* if (mZipListener != null)
                mZipListener.onZipFailed("正在压缩，请等待完成再执行压缩");*/
            onFailed("正在压缩，请等待完成再执行压缩");
            return;
        }

        final File sdcardFileList = new File(filePath);
        if (sdcardFileList.exists() && sdcardFileList.isDirectory()) {
            ThreadUtil.async(new Runnable() {
                @Override
                public void run() {
                    backupSdcard(sdcardFileList);
                }
            });
        }
    }

    @Override
    public void setFileNameParams(String uid, String aid) {
        this.uid = uid;
        this.aid = aid;
    }

    /**
     * 备份sdcard数据
     *
     * @param sdcardFileList
     */
    private void backupSdcard(File sdcardFileList) {
        File zipFilePath = new File(BackupConstant.ZIP_FILE_PATH);
        //判断之前是否有压缩过，如果有则删除重新创建，无则直接创建
        if (zipFilePath.exists()) {
            //zipFile.mkdir();
            ZipUtils.deleteFile(zipFilePath);
            zipFilePath.mkdirs();
        } else zipFilePath.mkdirs();

        mIsZipping = true;
        File zipFile = zipFile(sdcardFileList);
        mIsZipping = false;
        if (zipFile == null) {
            onFailed("压缩文件过程出现异常");
            return;
        }
        LogUtil.out(TAG, "压缩文件成功》》》》》");
        onSucceed(zipFile.getName());
    }

    private File zipFile(File sdcardFileList) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String tempFileName = String.format("%s%s_%s_%s.%s", BackupConstant.ZIP_FILE_PATH,uid,aid,
                dateFormat.format(new Date()), "zip.tmp");

       // tempFileName = BackupConstant.ZIP_FILE_PATH + "uid_aid_20181234567890" + ".zip.tmp";
        LogUtil.out(TAG, "zipFile 新文件名字：" + tempFileName);
        try {
            ZipUtils.zip(sdcardFileList, tempFileName);

            File zipedFile;
            new File(tempFileName).renameTo(zipedFile = new File(tempFileName.replace(".tmp", "")));

            return zipedFile;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(TAG, "压缩出错：" + e.getMessage());
        }
        return null;
    }


    private String getAndroidId() {
        String androidId = "";
        if (mContextWeakReference != null) {
            Context context = mContextWeakReference.get();
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return androidId;
    }

    @Override
    public boolean isZipping() {
        return mIsZipping;
    }

    @Override
    public void addZipListener(IZipListener zipListener) {
        mZipListener = zipListener;
    }

    private void onFailed(String message) {
        if (mZipListener != null)
            mZipListener.onZipFailed(message);
    }

    private void onSucceed(String zipFileName) {
        if (mZipListener != null)
            mZipListener.onZipSuccess(zipFileName);
    }
}

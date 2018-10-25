package com.batmobi.backup;

import android.text.TextUtils;


import com.batmobi.BackupConstant;
import com.batmobi.IResponListener;
import com.batmobi.util.CommandUtil;
import com.batmobi.util.LogUtil;
import com.batmobi.util.ThreadUtil;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * description: 备份管理
 * author: kyXiao
 * created date: 2018/9/12
 */

public class BackupForShell implements IBackup {
    private static final String TAG = "BackupForShell";

    private IResponListener mOnResponListener;
    private int mBackupCounted;
    private int mRemovedCount;

    @Override
    public void startBackup(String packageName, String destDir, IResponListener responListener) {
        if (TextUtils.isEmpty(packageName)) {
            if (responListener != null)
                responListener.onResponFailed("包名不能为空");
            return;
        }
        List<String> packageNameList = new LinkedList<>();
        packageNameList.add(packageName);
        startBackup(packageNameList, destDir, responListener);
    }

    @Override
    public void startBackup(List<String> packNameList, String destDir, IResponListener responListener) {
        this.mOnResponListener = responListener;
        if (packNameList == null || packNameList.size() == 0) {
            backupFailed("包名列表不能为空");
            return;
        }
        backupFiles(packNameList, destDir);
    }

    @Override
    public void removeBackup(String packageName, String destDir, IResponListener responListener) {
        if (TextUtils.isEmpty(packageName)) {
            if (responListener != null)
                responListener.onResponFailed("包名不能为空");
            return;
        }

        List<String> packageNameList = new LinkedList<>();
        packageNameList.add(packageName);
        removeBackup(packageNameList, destDir, responListener);
    }

    @Override
    public void removeBackup(List<String> packNameList, String destDir, IResponListener responListener) {
        this.mOnResponListener = responListener;
        if (packNameList == null || packNameList.size() == 0) {
            getResponListener().onResponFailed("包名不能为空。");
            return;
        }
        removeBackup(packNameList, destDir);
    }

    private void removeBackup(final List<String> packageNameList, final String destDir) {
        ThreadUtil.async(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtil.out(TAG, "开始删除备份》》》》");
                    String backupFileDir = BackupConstant.getBackupFolder(destDir);
                    LogUtil.out(TAG, "开始删除备份 备份存储的路径：" + backupFileDir);


                    File baseFile = new File(backupFileDir);
                    if (baseFile.exists()) {
                        mRemovedCount = 0;
                        final int packageNameSize = packageNameList.size();

                        for (String packageName : packageNameList) {
                            String packageDir = baseFile.getAbsolutePath() + "/" + packageName;
                            String deleteShell = "rm -r " + packageDir;
                            CommandUtil.sendCommand(deleteShell, new CommandUtil.OnResponListener() {
                                @Override
                                public void onSuccess(List<String> responList) {
                                    mRemovedCount++;
                                    if (mRemovedCount == packageNameSize)
                                        backupSucceed();
                                }

                                @Override
                                public void onFailed(String msg) {
                                    backupFailed(msg);
                                }
                            });
                        }
                    }

                    LogUtil.out(TAG, "删除备份》》》》》》");
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.error(TAG, e.getMessage());
                    backupFailed(e.getMessage());
                }
            }
        });
    }

    private void backupFiles(final List<String> packageNameList, final String destDir) {

        ThreadUtil.async(new Runnable() {
            @Override
            public void run() {
                try {
                    // removeBackup(packageName, destDir);
                    int packageNameCount = packageNameList.size();
                    LogUtil.out(TAG, "开始备份>>>>>>>");
                    String backupFileDir = BackupConstant.getBackupFolder(destDir);

                    LogUtil.out(TAG, "开始备份 存储的路径：" + backupFileDir);
                    File backupFile = new File(backupFileDir);
                    if (!backupFile.exists()) {
                        boolean mkdirResult = backupFile.mkdirs();
                    } else {

                    }
                    mBackupCounted = 0;
                    for (int i = 0; i < packageNameCount; i++) {
                        String packageName = packageNameList.get(i);
                        String command = "cp -ar /data/data/" + packageName + " " + backupFileDir;

                        CommandUtil.sendCommand(command, new CommandUtil.OnResponListener() {
                            @Override
                            public void onSuccess(List<String> responList) {
                                LogUtil.out(TAG, "onAllDownloadSuccess : 备份成功");
                                for (int i = 0; i < responList.size(); i++) {
                                    LogUtil.out(TAG, "onAllDownloadSuccess : " + responList.get(i));
                                }
                                mBackupCounted++;
                            }

                            @Override
                            public void onFailed(String msg) {
                                LogUtil.out(TAG, "onFailed 备份失败： " + msg);
                                backupFailed(msg);
                            }
                        });
                    }

                    if (mBackupCounted == packageNameCount) {
                        backupSucceed();
                    } else {
                        backupFailed("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.error(TAG, "备份失败 : " + e.getMessage());
                    backupFailed(e.getMessage());
                }
                LogUtil.out(TAG, "备份完成>>>>>>>");
            }
        });
    }

    private void backupFailed(final String msg) {
        mBackupCounted = 0;
        mRemovedCount = 0;
        getResponListener().onResponFailed(msg);
    }

    private void backupSucceed() {
        mBackupCounted = 0;
        mRemovedCount = 0;
        getResponListener().onResponSuccess("");
    }

    private IResponListener getResponListener() {
        if (mOnResponListener == null)
            mOnResponListener = new EmptyResponListener();
        return mOnResponListener;
    }




    private class EmptyResponListener implements IResponListener {

        @Override
        public void onResponSuccess(String msg) {

        }

        @Override
        public void onResponFailed(String msg) {

        }
    }
}

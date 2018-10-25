package com.batmobi;

import android.content.Context;
import android.hardware.input.InputManager;
import android.widget.ImageView;

import java.util.List;

/**
 * description: 备份管理类（对外）
 * author: kyXiao
 * created date: 2018/9/18
 */

public class BackupManage implements IManager {

    private IManager mBackupManageImpl;

    public BackupManage init() {
        mBackupManageImpl = BackupManageImpl.getInstance();
        return this;
    }

    @Override
    public IManager setContext(Context context) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.setContext(context);
        return this;
    }

    @Override
    public IManager setBackupWay(String backupWay) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.setBackupWay(backupWay);
        return this;
    }

    @Override
    public IManager setBackupDestDir(String destDir) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.setBackupDestDir(destDir);
        return this;
    }

    @Override
    public IManager setParams(String ftpIp, String uid, String aid, String fileName) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.setParams(ftpIp, uid, aid, fileName);
        return this;
    }

    @Override
    public void uploadFile(IResponListener responListener) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.uploadFile(responListener);
    }

   /* @Override
    public IManager setUid(String uid) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.setUid(uid);
        return this;
    }

    @Override
    public IManager setAid(String aid) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.setAid(aid);
        return this;
    }*/

    @Override
    public IManager backup(String packageName) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.backup(packageName);

        return this;
    }

    @Override
    public IManager backup(String packageName, IResponListener responListener) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.backup(packageName, responListener);

        return this;
    }

    @Override
    public IManager backup(List<String> packageNameList, IResponListener responListener) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.backup(packageNameList, responListener);

        return this;
    }

    @Override
    public void removeBackup(String packageName) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.removeBackup(packageName);
    }

    @Override
    public void removeBackup(String packageName, IResponListener responListener) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.removeBackup(packageName, responListener);
    }

    @Override
    public void removeBackup(List<String> packageNameList, IResponListener responListener) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.removeBackup(packageNameList, responListener);
    }

    @Override
    public void restore(String packageName) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.restore(packageName);
    }

    @Override
    public void restore(String packageName, IResponListener responListener) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.restore(packageName, responListener);
    }

    @Override
    public void restore(List<String> packageNameList, IResponListener responListener) {
        if (mBackupManageImpl != null)
            mBackupManageImpl.restore(packageNameList, responListener);
    }
}

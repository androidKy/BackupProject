package com.batmobi;

import android.content.Context;

import java.util.List;

/**
 * description:
 * author: kyXiao
 * created date: 2018/9/12
 */

public interface IManager {


    IManager setContext(Context context);

    /**
     * 设置备份的方式
     *
     * @param backupWay
     * @return
     */
    IManager setBackupWay(String backupWay);

    /**
     * 设置备份存储的路径
     *
     * @param destDir
     * @return
     */
    IManager setBackupDestDir(String destDir);

    /**
     * 设置uid
     *
     * @param uid
     * @return
     */
    IManager setUid(String uid);

    /**
     * 设置aid
     *
     * @param aid
     * @return
     */
    IManager setAid(String aid);

    /**
     * 传入需要备份的包名
     *
     * @param packageName
     */
    void backup(String packageName);

    void backup(String packageName, IResponListener responListener);

    void backup(List<String> packageNameList, IResponListener responListener);
    /**
     * 清楚备份
     *
     * @param packageName
     */
    void removeBackup(String packageName);

    void removeBackup(String packageName, IResponListener responListener);

    void removeBackup(List<String> packageNameList, IResponListener responListener);
    /**
     * 还原备份
     *
     * @param packageName
     */
    void restore(String packageName);

    void restore(String packageName, IResponListener responListener);

    void restore(List<String> packageNameList, IResponListener responListener);
}

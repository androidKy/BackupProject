package com.batmobi.backup;


import com.batmobi.IResponListener;

import java.util.List;

/**
 * description: 备份接口
 * author: kyXiao
 * created date: 2018/9/12
 */

public interface IBackup {
    /**
     * 开始进行备份
     *
     * @param packageName
     * @param destDir
     */
    void startBackup(String packageName, String destDir, IResponListener responListener);

    void startBackup(List<String> packNameList, String destDir, IResponListener responListener);

    /**
     * 清楚已完成的备份
     *
     * @param packageName
     */
    void removeBackup(String packageName, String destDir, IResponListener responListener);

    void removeBackup(List<String> packNameList, String destDir, IResponListener responListener);
}

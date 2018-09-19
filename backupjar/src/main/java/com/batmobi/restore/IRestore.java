package com.batmobi.restore;

import com.batmobi.IResponListener;

import java.util.List;

/**
 * description:
 * author: kyXiao
 * created date: 2018/9/12
 */

public interface IRestore {

    void restore(String packageName, String destDir, IResponListener responListener);

    void restore(List<String> packageNameList, String restoreDir, IResponListener responListener);
}

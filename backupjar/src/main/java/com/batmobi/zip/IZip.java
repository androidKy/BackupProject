package com.batmobi.zip;

/**
 * description: 压缩接口
 * author: kyXiao
 * created date: 2018/9/17
 */

public interface IZip {
    void startZip(String filePath);

    void setFileNameParams(String uid, String aid);

    boolean isZipping();

    void addZipListener(IZipListener zipListener);

    interface IZipListener {
        void onZipSuccess(String zipFileName);

        void onZipFailed(String failedMsg);
    }
}

package com.batmobi.upload;

import android.content.Context;

/**
 * description: 上传接口
 * author: kyXiao
 * created date: 2018/9/17
 */

public interface IUpload {
    void upload(Context context, IUploadListener uploadListener);

    interface IUploadListener {
        void onUploadSuccess();

        void onUploadFailed(String msg);
    }
}

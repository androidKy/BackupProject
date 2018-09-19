package com.batmobi;

/**
 * description: 结果回调接口
 * author: kyXiao
 * created date: 2018/9/12
 */

public interface IResponListener {

    void onResponSuccess();

    void onResponFailed(String msg);
}

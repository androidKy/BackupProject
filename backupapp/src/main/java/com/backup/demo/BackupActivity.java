package com.backup.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.batmobi.BackupConstant;
import com.batmobi.BackupManage;
import com.batmobi.BackupManageImpl;
import com.batmobi.IManager;
import com.batmobi.IResponListener;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

public class BackupActivity extends AppCompatActivity {
    private static final String TAG = "BackupActivity";
    private IManager backupManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void backupSamples(View view) {
        List<String> packageNameList = new ArrayList<>();
        packageNameList.add("com.batmobi.backup");

        backupManage = new BackupManage()
                .init()
                .setParams("192.168.31.244", "uid", "aid", "")
                .setContext(this)
                .backup(packageNameList, new IResponListener() {
                    @Override
                    public void onResponSuccess(String zipFileName) {
                        toast("备份成功");
                        Log.i(TAG, "备份成功 onResponSuccess: zipFileName = " + zipFileName);
                    }

                    @Override
                    public void onResponFailed(String msg) {
                        toast("备份失败 msg = " + msg);
                        Log.i(TAG, "onResponFailed: " + msg);
                    }
                });

        backupManage.uploadFile(new IResponListener() {
            @Override
            public void onResponSuccess(String zipFileName) {

            }

            @Override
            public void onResponFailed(String msg) {

            }
        });
    }

    public void restoreSamples(View view) {
        new BackupManage()
                .init()
                .setParams("192.168.31.244", "uid", "aid", "uid_aid_20180919095916.zip")
                .setContext(this)
                .restore("com.batmobi.backup", new IResponListener() {
                    @Override
                    public void onResponSuccess(String zipFileName) {
                        toast("恢复备份成功");
                        Log.i(TAG, "恢复备份成功 onResponSuccess: zipFileName = " + zipFileName);
                    }

                    @Override
                    public void onResponFailed(String msg) {
                        toast("恢复备份失败 msg = " + msg);
                        Log.i(TAG, "onResponFailed: " + msg);
                    }
                });
    }

    public void removeBackup(View view) {
       /* new BackupManageImpl()
                // .setBackupDestDir("/sdcard")
                .removeBackup("com.batmobi.backup", new IResponListener() {
                    @Override
                    public void onResponSuccess() {
                        toast("删除备份成功");
                    }

                    @Override
                    public void onResponFailed(String msg) {
                        toast("删除备份失败 msg = " + msg);
                    }
                });*/
    }
}

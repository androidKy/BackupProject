package com.batmobi.backup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

/**
 * description:
 * author: kyXiao
 * created date: 2018/9/11
 */

public class BackupActivity extends AppCompatActivity {
    private static final String TAG = "BackupActivity";
    private TextView tvBackup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_backup);

        tvBackup = (TextView) findViewById(R.id.tv_backup);

        //String path = getFilesDir().getPath();
        //   File file = getFilesDir();
        //Log.i(TAG, "onCreate: path = " + path);
    }


    @Override
    protected void onResume() {
        super.onResume();


       /* if (!isAllPermissionGranted(this)) {
           *//* Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*//*
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    124);
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 124) {
            for (int grantResult : grantResults) {
                Log.i(TAG, "onRequestPermissionsResult: grantResult = " + grantResult);
            }
        }
    }

    public boolean isAllPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            String permission2 = Manifest.permission.READ_EXTERNAL_STORAGE;
            // if (permission.length == 0) return false;
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, permission2) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
           /* for (String s : permission) {
            }*/
        }
        return true;
    }

    public void backup(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("ky", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < 10; i++) {
            editor.putString("data" + i, "backupdata : " + i);
        }

        editor.apply();


        try {
            File file = getFilesDir();
            if (file == null) {
                Log.i(TAG, "backup: 访问不了目录");
                Toast.makeText(this, "访问不了目录", Toast.LENGTH_SHORT).show();
            } else {
                String absolutePath = file.getAbsolutePath();
                Log.i(TAG, "backup: absolutePath = " + absolutePath);
                String path = file.getPath();
                Log.i(TAG, "backup: path = " + path);
                if (!file.exists()) {
                    file.mkdirs();
                    Log.i(TAG, "backup: 访问不了com.ky.demo");
                } else {
                    Log.i(TAG, "backup: 能访问com.ky.demo");
                    File packageFile = new File(absolutePath.substring(0, absolutePath.length() - 5));
                    if (packageFile.exists()) {
                        Log.i(TAG, "backup: packageNameDir = " + packageFile.getAbsolutePath());
                        if (packageFile.isDirectory()) {
                            File[] fileList = packageFile.listFiles();
                            for (int i = 0; i < fileList.length; i++) {
                                Log.i(TAG, "fileName = " + fileList[i].getName());
                            }
                            Log.i(TAG, "shared_prefsName : " + fileList[3].getName());
                            File[] prefFiles = fileList[3].listFiles();
                            if (prefFiles != null) {
                                Toast.makeText(this, "listFiles返回值不为空,length = " + prefFiles.length, Toast.LENGTH_LONG).show();
                                File xmlFile = new File(fileList[3].getAbsolutePath() + "/create.xml");
                                if (!xmlFile.exists()) {
                                    xmlFile.mkdirs();
                                }
                            } else {
                                Toast.makeText(this, "listFiles返回值为空", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Log.i(TAG, "backup: 不能访问");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restore(View view) {

        SharedPreferences sharedPreferences = getSharedPreferences("ky", Context.MODE_PRIVATE);

        int i = new Random().nextInt(10);
        Log.i(TAG, "restore: i = " + i);
        String data = sharedPreferences.getString("data" + i, "");
        Log.i(TAG, "restore: data = " + data);

        tvBackup.setText(data);
    }

    public void createNewFile(View view) {

        SharedPreferences sharedPreferences = getSharedPreferences("kyXiao1", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("xiaoky", "kyxiao");
        editor.apply();
    }
}

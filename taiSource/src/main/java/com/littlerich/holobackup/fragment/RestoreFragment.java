package com.littlerich.holobackup.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.littlerich.holobackup.R;
import com.littlerich.holobackup.adapter.RestoreListAdapter;
import com.littlerich.holobackup.data.AppConfig;
import com.littlerich.holobackup.data.Utils;
import com.littlerich.holobackup.model.RestoreModel;
import com.littlerich.holobackup.util.TimeUtil;
import com.google.android.gms.ads.AdView;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.littlerich.holobackup.data.Constant.BACKUP_FOLDER;
import static com.littlerich.holobackup.data.Constant.BACKUP_FOLDER_APP_DATA;

public class RestoreFragment extends Fragment {

    private static final String TAG = "restore";

    private ProgressBar progressBar;
    private ListView listView;
    private View view;
    public RestoreListAdapter rAdapter;
    private List<RestoreModel> apkList = new ArrayList<>();
    private LinearLayout lyt_not_found;
    int ThreadCount = 0;
    long usageTime = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_restore, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        lyt_not_found = (LinearLayout) view.findViewById(R.id.lyt_not_found);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogApkFileOption(i);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void refreshList() {
        apkList = Utils.loadBackupAPK(getActivity());
        rAdapter = new RestoreListAdapter(getActivity(), apkList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        // Capture ListView item click
        listView.setMultiChoiceModeListener(multiChoiceModeListener);
        listView.setAdapter(rAdapter);
        if (apkList.size() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }

    }

    private boolean mode_checkall = false;

    private void toogleCheckAll() {
        mode_checkall = !mode_checkall;
        for (int i = 0; i < rAdapter.getCount(); i++) {
            listView.setItemChecked(i, mode_checkall);
        }
        if (mode_checkall) {
            rAdapter.selectAll();
        } else {
            rAdapter.resetSelected();
        }
    }

    public ActionMode getActionMode() {
        return act_mode;
    }

    private ActionMode act_mode = null;
    private AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = listView.getCheckedItemCount();
            mode.setTitle(checkedCount + " selected");
            //Toast.makeText(getActivity().getApplicationContext(), checkedCount + " selected", Toast.LENGTH_SHORT).show();
            rAdapter.setSelected(position, checked);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.action_check_all:
                    toogleCheckAll();
                    return true;
                case R.id.action_restore:
                    restoreApkFiles(rAdapter.getSelected());
                    return true;
                case R.id.action_delete:
                    deleteApkFiles(rAdapter.getSelected());
                    refreshList();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.restore_context_menu, menu);
            mode.setTitle(listView.getCheckedItemCount() + " conversation selected");
            act_mode = mode;
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //MainActivity.toolbar.setVisibility(View.VISIBLE);
            // TODO Auto-generated method stub
            // bAdapter.removeSelection();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }
    };

    private void dialogApkFileOption(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final RestoreModel r = rAdapter.getItem(position);
        builder.setTitle("数据还原");
        ListView listView = new ListView(getActivity());
        listView.setPadding(25, 25, 25, 25);
        String[] stringArray = new String[]{"应用安装", "数据还原", "删除备份"};
        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, stringArray));
        builder.setView(listView);
        final AppCompatDialog dialog = builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                List<RestoreModel> selected_apk = new ArrayList<>();
                selected_apk.add(r);
                switch (i) {
                    case 0:
                        restoreApkFiles(selected_apk);
                        //restore
                        break;
                    case 1:
                        new FileRestoreTask(selected_apk).execute();
                        usageTime = System.currentTimeMillis();
                        //   installapp(r);
                        //share
                        break;
                    case 2:
                        deleteApkFiles(selected_apk);
                        refreshList();
                        //Delete file
                        break;
                }
            }
        });

        dialog.show();
    }

    private void installapp(RestoreModel r) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri fileUri = Uri.fromFile(r.getFile());
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void restoreApkFiles(List<RestoreModel> apklist) {
        for (RestoreModel restr : apklist) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.setDataAndType(Uri.fromFile(restr.getFile()), "application/vnd.android.package-archive");
            startActivity(intent);
        }
    }

    private void deleteApkFiles(List<RestoreModel> apklist) {
        for (RestoreModel restr : apklist) {
            if (restr.getFile().exists()) {
                restr.getFile().delete();
            }
        }
    }

    // give bottom space
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AdView mAdView = (AdView) view.findViewById(R.id.ad_view);
        if (AppConfig.ENABLE_ADSENSE && Utils.cekConnection(getActivity())) {
            mAdView.setVisibility(View.VISIBLE);
        } else {
            mAdView.setVisibility(View.GONE);
        }
    }

    private class FileRestoreTask extends AsyncTask<Void, Integer, File> {

        private ProgressDialog progress;
        private List<RestoreModel> selected_app;

        public FileRestoreTask(List<RestoreModel> selected_app) {
            this.selected_app = selected_app;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(getActivity());
            progress.setMessage("请稍后...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(false);
            progress.setTitle("数据恢复");
            progress.show();
        }

        @Override
        protected File doInBackground(Void... params) {


            for (int i = 0; i < selected_app.size(); i++) {

                try {
                    // publishProgress(selected_app.get(i).getPackageName());
                    File sdCard = Environment.getExternalStorageDirectory();
                    String pkgName = selected_app.get(i).getPkg_name();
                    String appUserId = "root";
                    //      File x = new File(sdCard.getAbsolutePath() + "/" + sdCardDirectory);
                    File x = new File(BACKUP_FOLDER_APP_DATA);
                    //  String cmd = "tar zxvf " + x.getAbsolutePath() + "/" + xdir + ".tar.gz -C /data/data";
                    File newDir = new File(x.getAbsolutePath() + "/" + pkgName);
                    //  List<String> res = RootTools.sendShell("rm -r " + "/data/data/" + xdir + "/*",20000);
                    List<String> outDetail = RootTools.sendShell("ls -l " + "/data/data/", 5000);

                    for (String s : outDetail) {
                        // Log.i(TAG, "文件信息:" + s);
                        if (s.endsWith(pkgName)) {
                            //    Log.e(TAG, "目标文件:" + s);
                            String[] split = s.split(" ");
                            for (String str : split) {
                                //       Log.v(TAG, "截取信息:" + str);
                                if (TextUtils.isEmpty(str)) {
                                    continue;
                                }
                                if (appUserId.equals(str)) {
                                    break;
                                } else {
                                    appUserId = str;
                                }
                            }
                            break;
                        }
                    }
                    Log.e(TAG, "该APP的UserID:" + appUserId);
                    //   Log.i(TAG, "已移除原有数据》》》》》》》》》》》》》》");
                    RootTools.sendShell("cp -r " + newDir.getAbsolutePath() + "/*" + " /data/data/" + pkgName, 60 * 1000);
                    //  Log.i(TAG, "已复制新数据》》》》》》》》》》》》》》" + "cp -r " + newDir.getAbsolutePath() + " /data/data/");
                    RootTools.sendShell("chmod -R 771 /data/data/" + pkgName, 15000);
                    //  RootTools.sendShell("chown -R u0_a143:u0_a143 /data/data/" + pkgName, 15000);//更改文件拥有者
                    //    res = RootTools.sendShell(cmd, 10000);
                    traverseFolder1("/data/data/" + pkgName, appUserId);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            while (ThreadCount > 0) {
                //监听线程是否处理完成
            }


            int i = 0;
            File outputFile = new File(BACKUP_FOLDER);
            ;
           /* while (selected_app.size() > i) {
                String filename = selected_app.get(i).getApp_name() + "_" + selected_app.get(i).getVersion_name() + ".apk";
                outputFile = new File(BACKUP_FOLDER);
                if (!outputFile.exists()) {
                    outputFile.mkdirs();
                }
                File apk = new File(outputFile.getPath() + "/" + filename);
                try {
                    apk.createNewFile();
                    InputStream in = new FileInputStream(selected_app.get(i).getFile());
                    OutputStream out = new FileOutputStream(apk);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                publishProgress(i);
                i++;
            }*/
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ky", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("dataKey", "data").apply();

            Log.i(TAG, "backup data = " + sharedPreferences.getString("dataKey", ""));
            return outputFile;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //progress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File result) {
            if (progress != null) {
                progress.dismiss();
                usageTime = System.currentTimeMillis() - usageTime;
                Toast.makeText(getContext(), "恢复耗时：" + TimeUtil.formatTime(usageTime), Toast.LENGTH_LONG).show();
            }

            if (result != null) {
                /*AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setCancelable(false);
                alert.setTitle("备份完成");
                alert.setMessage("存储位置: " + BACKUP_FOLDER);
                alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dg, int arg1) {
                        bAdapter.resetSelected();
                        bAdapter.notifyDataSetChanged();
                        refresh(false);
                        dg.dismiss();
                    }
                });
                alert.show();*/
            } else {
                Toast.makeText(getActivity(), "应用还原失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void traverseFolder1(String path, final String userId) throws Exception {
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        List<String> fileDetail = RootTools.sendShell("ls -l " + path, 5000);
        if (fileDetail.size() > 0) {
            final LinkedList<String> list = new LinkedList<>(); //文件夹管理器

            for (String file2 : fileDetail) {
                if (file2.contains("lib ")) {
                    continue;
                }
                String[] f = file2.split(" ");
                String fileName = path + "/" + f[f.length - 1];
                if (file2.startsWith("d")) {
                    list.add(fileName);
                    folderNum++;
                } else {
                    fileNum++;
                }

                RootTools.sendShell("chown " + userId + ":" + userId + " " + fileName, 10000);
                f = null;
            }

            ThreadCount = list.size();
            while (!list.isEmpty()) {
                String submodule = list.removeFirst();
                disposedUid(userId, submodule);
            }

        } /*else {
            System.out.println("文件不存在!");
        }*/
        //   System.out.println("文件夹共有:" + folderNum + ",文件共有:" + fileNum);

    }

    private void disposedUid(final String userId, final String submodule) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LinkedList<String> subFileManager = new LinkedList<>();
                    List<String> tempSubFile = RootTools.sendShell("ls -l " + submodule, 5000);
                    for (String subFile : tempSubFile) {
                        String[] temp = subFile.split(" ");
                        String fileName = submodule + "/" + temp[temp.length - 1];
                        if (subFile.startsWith("d")) {
                            subFileManager.add(fileName);
                        }
                        Log.i(TAG, "traverseFolder1: disposeUserId = " + userId + " fileName = " + fileName);
                        RootTools.sendShell("chown " + userId + ":" + userId + " " + fileName, 10000);
                        //     Log.e(TAG, "子线程处理完成》" + "chown " + userId + ":" + userId + " " + fileName);
                    }
                    tempSubFile = null;

                    String[] files;
                    String tempFilePath;
                    List<String> tempSubFiled;
                    while (!subFileManager.isEmpty()) {
                        tempFilePath = subFileManager.removeFirst();
                        tempSubFiled = RootTools.sendShell("ls -l " + tempFilePath, 5000);
                        for (String file2 : tempSubFiled) {
                            String[] f = file2.split(" ");
                            String fileName = tempFilePath + "/" + f[f.length - 1];
                            if (file2.startsWith("d")) {
                                subFileManager.add(fileName);
                            }
                            RootTools.sendShell("chown " + userId + ":" + userId + " " + fileName, 10000);
                            //      Log.e(TAG, "子线程" + Thread.currentThread() + ">" + "chown " + userId + ":" + userId + " " + fileName);
                        }

                    }
                } catch (Exception e) {
                    Log.e(TAG, "文件恢复出现异常");
                }
                finishThread();
            }
        }).start();
    }

    public synchronized void finishThread() {
        ThreadCount--;
    }


}

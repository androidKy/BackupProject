# BackupProject
Android备份工具：（设备必须是已root)
> 1、备份数据：备份app数据到sdcard上，然后再将sdcard的数据全部压缩成压缩包，再上传到服务器。
>
> 2、还原数据：从服务器下载压缩包并进行解压，清空sdcard，加压下载的压缩包恢复sdcard数据，最后还原app数据。
>
> 技术要点：执行Linux命令、压缩和解压、FTP文件上传和文件下载。
 
---
# 用法
* 备份

      new BackupManage()
          .init()
          .setContext(context)
          .setUid("uid")  //used for generate a path of server
          .setAid("aid")  //used for generate a path of server
          .backup("packageName", new IResponListener() {
              @Override
              public void onResponSuccess() {
                 Log.i(TAG, "备份成功 onResponSuccess: ");
              }

             @Override
             public void onResponFailed(String msg) {
                 Log.i(TAG, "onResponFailed: " + msg);
              }
          });

* 还原   

       new BackupManage()
          .init()
          .setContext(context)
          .setUid("uid")  //used for generate a path of server
          .setAid("aid")  //used for generate a path of server
          .restore("packageName", new IResponListener() {
               @Override
               public void onResponSuccess() {
                   Log.i(TAG, "恢复备份成功 onResponSuccess: ");
               }

               @Override
               public void onResponFailed(String msg) {
                   Log.i(TAG, "onResponFailed: " + msg);
               }
           });

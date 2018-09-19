package com.batmobi.util;

import com.batmobi.BackupConstant;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * description:Ftp工具类
 * author: diff
 * date: 2018/1/22.
 */
public class FTPManager {
    private static final String TAG = "FTPManager";

    FTPClient ftpClient = null;

    public FTPManager() {
        ftpClient = new FTPClient();
    }

    /**
     * 连接到ftp服务器
     *
     * @param address
     * @param userName
     * @param pwd
     * @return
     * @throws Exception
     */
    public synchronized boolean connect(String address, String userName, String pwd) throws Exception {
        boolean bool = false;
        if (ftpClient.isConnected()) {//判断是否已登陆
            ftpClient.disconnect();
        }
        ftpClient.setDataTimeout(30 * 60 * 1000);
        ftpClient.setConnectTimeout(10 * 60 * 1000);
        ftpClient.setDefaultTimeout(30 * 60 * 1000);
        ftpClient.setControlEncoding("utf-8");
        ftpClient.connect(address);
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            if (ftpClient.login(userName, pwd)) {
                bool = true;
                LogUtil.out(TAG, "ftp连接成功");
            }
        }
        return bool;
    }

    /**
     * 创建文件夹
     *
     * @param path
     * @return
     * @throws Exception
     */
    public boolean createDirectory(String path) throws Exception {
        boolean bool = false;
        String directory = path.substring(0, path.lastIndexOf("/") + 1);
        int start = 0;
        int end = 0;
        if (directory.startsWith("/")) {
            start = 1;
        }
        end = directory.indexOf("/", start);
        while (true) {
            String subDirectory = directory.substring(start, end);
            LogUtil.out(TAG, "当前FTP目录：" + ftpClient.printWorkingDirectory());
            LogUtil.out(TAG, "createDirectory,subDirectory:" + subDirectory);

            if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                LogUtil.out(TAG, "createDirectory,changeWorkingDirectory failed,makeDirectory:" + subDirectory);
                ftpClient.makeDirectory(subDirectory);
                ftpClient.changeWorkingDirectory(subDirectory);
                bool = true;
            } else {
                LogUtil.out(TAG, "createDirectory,changeWorkingDirectory success:" + subDirectory);
                //ftpClient.changeWorkingDirectory(subDirectory);
            }
            start = end + 1;
            end = directory.indexOf("/", start);
            if (end == -1) {
                break;
            }
        }
        return bool;
    }

    /**
     * 定位到根目录
     */
    private void resetToParentDirectory() {
        try {
            LogUtil.out(TAG, "resetToParentDirectory，当前目录：" + ftpClient.printWorkingDirectory());
            if ("/".equals(ftpClient.printWorkingDirectory())) {
                LogUtil.out(TAG, "resetToParentDirectory，已经是根目录，return");
                return;
            }
            boolean result = ftpClient.changeToParentDirectory();
            if (result) {
                resetToParentDirectory();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 实现上传文件的功能
     *
     * @param localPath
     * @param serverPath
     * @param progressListener
     * @throws Exception
     */
    public synchronized void uploadFile(String localPath, String serverPath, IProgressListener progressListener)
            throws Exception {
        // 上传文件之前，先判断本地文件是否存在
        long startTime = System.currentTimeMillis();
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            LogUtil.out(TAG, "本地文件不存在");
            if (progressListener != null) {
                progressListener.onFailed(String.format("上传失败，本地文件不存在：%s", localPath), (int) ((System.currentTimeMillis() - startTime) / 1000));
            }
            return;
        }
        LogUtil.out(TAG, "本地文件存在，名称为：" + localFile.getName());
        resetToParentDirectory();
        createDirectory(serverPath); // 如果文件夹不存在，创建文件夹
        //  ftpClient.changeWorkingDirectory("/RomBackUp/F-013/133e270dbd50e194/");
        LogUtil.out(TAG, "当前FTP工作目录：" + ftpClient.printWorkingDirectory());
        LogUtil.out(TAG, "服务器文件存放路径：" + serverPath + localFile.getName());
        String fileName = localFile.getName();
        // 如果本地文件存在，服务器文件也在，上传文件，这个方法中也包括了断点上传
        long localSize = localFile.length(); // 本地文件的长度
        ftpClient.enterLocalPassiveMode();
        FTPFile[] files = ftpClient.listFiles(fileName);
        long serverSize = 0;
        if (files.length == 0) {
            LogUtil.out(TAG, "服务器文件不存在");
            serverSize = 0;
        } else {
            LogUtil.out(TAG, "服务器文件存在");
            serverSize = files[0].getSize(); // 服务器文件的长度
        }
        if (localSize <= serverSize) {
            if (ftpClient.deleteFile(fileName)) {
                LogUtil.out(TAG, "服务器文件存在,删除文件,开始重新上传");
                serverSize = 0;
            }
        }
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
        // 进度
        long step = localSize / 100;
        long process = 0;
        long currentSize = 0;
        // 好了，正式开始上传文件
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setRestartOffset(serverSize);
        raf.seek(serverSize);
        OutputStream output = ftpClient.appendFileStream(fileName);
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = raf.read(b)) != -1) {
            output.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                if (process % 10 == 0) {
                    LogUtil.out(TAG, "上传进度：" + process);
                    if (progressListener != null) {
                        progressListener.onProgress(String.format("%s,上传中...", localFile), (int) process);
                    }
                }
            }
        }
        output.flush();
        output.close();
        raf.close();
        int ts = (int) ((System.currentTimeMillis() - startTime) / 1000);
        LogUtil.out(TAG, fileName + ",耗时：" + ts + "秒");

        if (ftpClient.completePendingCommand()) {
            LogUtil.out(TAG, "文件上传成功");
            if (progressListener != null) {
                progressListener.onSuccess(localPath, ts);
            }
        } else {
            LogUtil.out(TAG, "文件上传失败");
            if (progressListener != null) {
                progressListener.onFailed("ftpClient.completePendingCommand():false", ts);
            }
        }
    }

    /**
     * 进度监听
     */
    public interface IProgressListener {
        void onProgress(String msg, int progress);

        void onSuccess(String filePath, int ts);

        void onFailed(String errorMsg, int ts);
    }

    /**
     * 实现下载文件功能，可实现断点下载
     *
     * @param localPath
     * @param serverPath
     * @param progressListener
     * @throws Exception
     */
    public synchronized void downloadFile(String localPath, String serverPath, IProgressListener progressListener)
            throws Exception {
        long startTime = System.currentTimeMillis();
        LogUtil.out(TAG, "downloadFile,localPath:" + localPath + ",serverPath:" + serverPath);
        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            LogUtil.out(TAG, "服务器文件不存在");
            if (progressListener != null) {
                progressListener.onFailed(String.format("服务器文件不存在:%s", serverPath), (int) ((System.currentTimeMillis() - startTime) / 1000));
            }
            return;
        }
        LogUtil.out(TAG, "远程文件存在,名字为：" + files[0].getName());
        localPath = localPath + files[0].getName();
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
        File localFile = new File(localPath);
        long localSize = 0;
        if (localFile.exists()) {
            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
            if (localSize >= serverSize) {
                LogUtil.out(TAG, "本地文件存在，不用重新下载");
                if (progressListener != null) {
                    progressListener.onSuccess("本地文件存在，不用重新下载", (int) ((System.currentTimeMillis() - startTime) / 1000));
                }
                return;
            } else {
                LogUtil.out(TAG, "断点续传，继续上次下载");
                if (progressListener != null) {
                    progressListener.onProgress(String.format("%s,断点续传，继续上次下载", serverPath), 1);
                }
            }
        }
        // 进度
        long step = serverSize / 100;
        long process = 0;
        long currentSize = 0;
        // 开始准备下载文件
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        OutputStream out = new FileOutputStream(localFile, true);
        ftpClient.setRestartOffset(localSize);
        LogUtil.out(TAG, "serverPath:" + serverPath);
//        InputStream input = ftpClient.retrieveFileStream(new String(serverPath.getBytes("UTF-8"), "ISO-8859-1"));
        InputStream input = ftpClient.retrieveFileStream(serverPath);
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = input.read(b)) != -1) {
            out.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                if (process % 10 == 0) {
                    LogUtil.out(TAG, "下载进度：" + process);
                    if (progressListener != null) {
                        progressListener.onProgress(String.format("下载中:%s", serverPath), (int) process);
                    }
                }
            }
        }
        out.flush();
        out.close();
        input.close();
        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
        if (ftpClient.completePendingCommand()) {
            LogUtil.out(TAG, "文件下载成功");
            if (progressListener != null) {
                progressListener.onProgress(String.format("%s,文件下载成功", serverPath), 100);
                int ts = (int) ((System.currentTimeMillis() - startTime) / 1000);
                progressListener.onSuccess(serverPath, ts);
            }
        } else {
            LogUtil.out(TAG, "文件下载失败");
            if (progressListener != null) {
                progressListener.onFailed(String.format("%s,文件下载失败", serverPath), (int) ((System.currentTimeMillis() - startTime) / 1000));
            }
        }
    }

    /**
     * 获取指定目录下的最新的文件名字
     *
     * @param directorName
     * @return
     */
    public String getNewestFileName(String directorName) {
        String name = null;
        try {
            FTPFile[] ftpFiles = listFiles(directorName);
            //预防拉取失败，100秒后重试一次
            if (ftpFiles == null && ftpFiles.length == 0) {
                Thread.sleep(100);
                ftpFiles = listFiles(directorName);
            }
            FTPFile newEstFile = null;
            for (FTPFile ftpFile : ftpFiles) {
                if (newEstFile == null) {
                    newEstFile = ftpFile;
                } else {
                    newEstFile = newEstFile.getTimestamp().compareTo(ftpFile.getTimestamp()) > 0 ? newEstFile : ftpFile;
                }
                LogUtil.out(TAG, String.format("getNewestFile:name:%s:time:%s", ftpFile.getName(), ftpFile.getTimestamp().getTime().toString()));
            }
            if (newEstFile != null) {
                name = newEstFile.getName();
            }
            LogUtil.out(TAG, String.format("getNewestFile,newestFile is:%s:%s", newEstFile.getName(), newEstFile.getTimestamp().getTime().toString()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        LogUtil.out(TAG, "getNewestFile:" + name);
        return name;
    }

    public FTPFile[] listFiles(String directorName) {
        FTPFile[] ftpFiles = null;
        try {
            LogUtil.out(TAG, "listFiles:" + directorName);
            LogUtil.out(TAG, "listFiles:printWorkingDirectory:" + ftpClient.printWorkingDirectory());
            ftpClient.enterLocalPassiveMode();
            ftpFiles = ftpClient.listFiles(directorName, new FTPFileFilter() {
                @Override
                public boolean accept(FTPFile ftpFile) {
                    return ftpFile.isFile();
                }
            });
        } catch (IOException e) {
            LogUtil.out(TAG, "listFiles exception:" + e.getMessage());
            e.printStackTrace();
        }

        return ftpFiles;
    }


    /**
     * 如果ftp上传打开，就关闭掉
     *
     * @throws Exception
     */
    public void closeFTP() throws Exception {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }
}

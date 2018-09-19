package com.batmobi.util;


import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * description:
 * author: kyXiao
 * created date: 2018/9/13
 */

public class CommandUtil {

    public static void sendCommand(String command) {
        sendCommand(command, null);
    }

    public static void sendCommand(String command, OnResponListener responListener) {
        List<String> dataList = new LinkedList<>();
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        DataInputStream dataErrorInputStream = null;
        Process process = null;
        StringBuilder errorLine = new StringBuilder();
        try {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataErrorInputStream = new DataInputStream(process.getErrorStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(dataInputStream));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(dataErrorInputStream));


            dataOutputStream.writeBytes(command + "\n");
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit \n");
            dataOutputStream.flush();

            String line = reader.readLine();
            String line_error;
            for (line_error = errReader.readLine(); line != null; line = reader.readLine()) {
                if (!TextUtils.isEmpty(line))
                    dataList.add(line);

                if (!TextUtils.isEmpty(line_error))
                    errorLine.append(line_error).append("\n");
            }

            if (responListener != null) {
                if (dataList.size() > 0 || TextUtils.isEmpty(errorLine.toString())) {
                    responListener.onSuccess(dataList);
                } else if (!TextUtils.isEmpty(errorLine.toString()))
                    responListener.onFailed(errorLine.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            responListener.onFailed(e.getMessage());
        } finally {
            closeStream(dataInputStream);
            closeStream(dataOutputStream);
            closeStream(dataErrorInputStream);
           /* if (process != null) {
                process.destroy();
            }*/
        }
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnResponListener {
        void onSuccess(List<String> responList);

        void onFailed(String msg);
    }
}

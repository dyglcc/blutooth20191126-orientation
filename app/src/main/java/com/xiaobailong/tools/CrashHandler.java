package com.xiaobailong.tools;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by dongyuangui on 15-5-26.
 */

/**
 * 统计crash
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";

    // Thread.getDefaultUncaughtExceptionHandler(); 系统默认的处理所有子线程异常，可以通过覆盖捕获异常
    private static CrashHandler instance;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context context;
    private boolean ENABLE = false;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void setEnable(boolean ENABLE) {
        this.ENABLE = ENABLE;
    }

    public void run(Context context) {
        this.context = context;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (!(defaultHandler instanceof CrashHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        try {
            if (defaultHandler != null) {
                if (causeByAppadhoc(throwable)) {
                    saveCrashInfo2File(throwable);
                }
                defaultHandler.uncaughtException(thread, throwable);
            }
        } catch (Throwable e) {
            T.e(e);
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private boolean saveCrashInfo2File(Throwable ex) {


        StringBuffer sb = new StringBuffer();

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String fileName = "crash-adhoc.log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crash/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(path, fileName);

                if (file.exists())
                    file.delete();

                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                T.i(TAG, "saveCrashInfo2File: " + sb.toString());
                fos.close();
            }
            return true;
        } catch (Throwable e) {
            T.e(e);
        }
        return false;
    }

    /**
     * 保存错误信息到StringBuilder中
     * return true cause crash by appadhoc
     * return false cause crash not by appadhoc
     */
    public boolean causeByAppadhoc(Throwable ex) {
        PrintWriter printWriter = null;
        Writer writer = null;
        try {
            writer = new StringWriter();
            printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            return containAdhoc(writer.toString());
        } catch (Throwable t) {
            T.e(t);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable e) {
                    T.e(e);
                }
            }
        }
        return false;
    }

    public boolean containAdhoc(String errrtrace) {
        return true;
    }

    public void onDestory() {
        context = null;
        ENABLE = false;
        instance = null;
    }
}

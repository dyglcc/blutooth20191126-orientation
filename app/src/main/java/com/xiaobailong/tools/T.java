package com.xiaobailong.tools;

import android.text.TextUtils;
import android.util.Log;


public class T {
    public static final boolean DEBUG = true;
    static final String AppName = "ADHOC_SDK";
    private static final int MAX_LENGTH = 4000;

    public static void i(String string) {
        if (string == null) {
            return;
        }
        if (DEBUG) {
            Log.i(AppName, string);
        }
    }

    public static void i(String tag, String string) {
        if (TextUtils.isEmpty(string))
            return;

        if (!DEBUG) {
            return;
        }

        int index = 0;
        String sub;

        while (index < string.length()) {
            //--------------------------------------------------------------------------------------java的字符不允许指定超过总的长度end
            if (string.length() <= index + MAX_LENGTH) {
                sub = string.substring(index);
            } else {
                sub = string.substring(index, index + MAX_LENGTH);
            }
            index += MAX_LENGTH;
            Log.i(tag, sub);
        }

    }

    public static void w(String tag, String string) {
        if (string == null || tag == null) {
            return;
        }
//        if (DEBUG) {
        Log.w(tag, string);
//        } else {
//            System.err.println(string);
//        }
    }

    public static void w(String string) {
        w(AppName, string);
    }

//    public static void a(int num) {
//        if (DEBUG) {
//            Log.d(AppName, Integer.toString(num));
//        }
//    }

    public static void e(String tag, String string) {
        if (TextUtils.isEmpty(string))
            return;

        if (!DEBUG)
            return;

        int index = 0;
        String sub;
        if (string.length() < MAX_LENGTH) {
            Log.e(tag, string);
            return;
        }

        while (index < string.length()) {
            //--------------------------------------------------------------------------------------java的字符不允许指定超过总的长度end
            if (string.length() < index + MAX_LENGTH) {
                sub = string.substring(index);
            } else {
                sub = string.substring(index, index + MAX_LENGTH);
            }
            index += MAX_LENGTH;
            Log.e(tag, sub);
        }
    }

    public static void eLess(String tag, String value) {
        if (TextUtils.isEmpty(value))
            return;

        if (!DEBUG)
            return;

        Log.e(tag, value);
    }


    public static void e(Exception exception) {
        if (exception == null || exception.toString() == null) {
            return;
        }
        if (DEBUG) {
            if (exception != null) {
                exception.printStackTrace();
            }
            Log.e(AppName, exception.toString());
        } else {
            System.err.println(exception.toString());
        }
    }
    public static void e_debug(Throwable exception) {
        if (exception == null || exception.toString() == null) {
            return;
        }
        if (DEBUG) {
            if (exception != null) {
                exception.printStackTrace();
            }
        }
    }

    public static void e(Error error) {
        if (error == null || error.toString() == null) {
            return;
        }
        if (DEBUG) {
            error.printStackTrace();
            Log.e(AppName, error.toString());
        } else {
            System.err.println(error.toString());
        }
    }

    public static void e(Throwable throwable) {
        if (throwable == null || throwable.toString() == null) {
            return;
        }
        if (DEBUG) {
            throwable.printStackTrace();
            Log.e(AppName, throwable.toString());
        } else {
            System.err.println(throwable.toString());
        }
    }

    public static void d(String msg) {
        if (msg == null) {
            return;
        }
        if (DEBUG) {
            Log.d(AppName, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (msg == null || tag == null) {
            return;
        }
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }
}

package com.wecareapp.android.utilities;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.wecareapp.android.Constants;

/**
 * Created by Karthik Kompelli
 */
public class Loggers {

    private static final Gson gson;

    public static boolean ENABLE_LOGGERS = true;
    private static String TAG = Constants.APP_NAME;
    private static boolean ENABLE_WARN = true;
    private static boolean ENABLE_VERBOSE = true;
    private static boolean ENABLE_DEBUG = true;
    private static boolean ENABLE_INFO = true;
    private static boolean ENABLE_ERRORS = true;

    static {
        gson = new Gson();
    }

    public static void verbose(String msg) {
        if (ENABLE_LOGGERS && ENABLE_VERBOSE) {
            Log.v(TAG, msg);
        }
    }

    public static void debug(String msg) {
        if (ENABLE_LOGGERS && ENABLE_DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void info(String msg) {
        if (ENABLE_LOGGERS && ENABLE_INFO) {
            Log.i(TAG, msg);
        }
    }

    public static void error(String msg) {
        if (ENABLE_LOGGERS && ENABLE_ERRORS) {
            Log.e(TAG, TextUtils.isEmpty(msg) ? "" : msg);
        }
    }

    public static void error(String tag, String msg) {
        if (ENABLE_LOGGERS && ENABLE_ERRORS) {
            Log.e(tag, msg);
        }
    }

    public static void error(String tag, Object msg) {
        if (ENABLE_LOGGERS && ENABLE_ERRORS) {
            try {
                Log.e(tag, gson.toJson(msg));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void error(String tag, String msg, Exception e) {
        if (ENABLE_LOGGERS && ENABLE_ERRORS) {
            Log.e(tag, msg, e);
        }
    }

    public static void warn(String tag, String msg, Exception exception) {
        if (ENABLE_LOGGERS && ENABLE_WARN) {
            Log.w(tag, msg, exception);
        }
    }

    public static String getTAG() {
        return TAG;
    }

    public static void setTAG(String TAG) {
        Loggers.TAG = TAG;
    }

    public static boolean isEnableLoggers() {
        return ENABLE_LOGGERS;
    }

    public static void setEnableLoggers(boolean enableLoggers) {
        ENABLE_LOGGERS = enableLoggers;
    }

    public static boolean isEnableVerbose() {
        return ENABLE_VERBOSE;
    }

    public static void setEnableVerbose(boolean enableVerbose) {
        ENABLE_VERBOSE = enableVerbose;
    }

    public static boolean isEnableDebug() {
        return ENABLE_DEBUG;
    }

    public static void setEnableDebug(boolean enableDebug) {
        ENABLE_DEBUG = enableDebug;
    }

    public static boolean isEnableInfo() {
        return ENABLE_INFO;
    }

    public static void setEnableInfo(boolean enableInfo) {
        ENABLE_INFO = enableInfo;
    }

    public static boolean isEnableErrors() {
        return ENABLE_ERRORS;
    }

    public static void setEnableErrors(boolean enableErrors) {
        ENABLE_ERRORS = enableErrors;
    }

}

package com.wecareapp.android.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonCode {

    private Context context;
    private Activity activity;
    public Bitmap resizedbitmap = null;

    public CommonCode(Context _Context) {
        this.context = _Context;
        this.activity = (Activity) context;
    }

    /**
     * This is used to check Internet connection is available or not
     *
     * @return
     * @author Imran 05-Oct-2014 03:33:15 PM
     */
    public boolean checkInternet() {
        try {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            //if there is a network
            if (activeNetwork != null) {
                //if connected to wifi or mobile data plan
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return true;
                }
            }


//					ConnectivityManager connectivity = (ConnectivityManager) context
//					.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//			if (connectivity != null) {
//				NetworkInfo[] info = connectivity.getAllNetworkInfo();
//				if (info != null)
//					for (int i = 0; i < info.length; i++)
//						// check if network is connected or device is in range
//						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//							return true;
//						}
//			}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This is used to show toast no internet message
     *
     * @author Harshal 22-Dec-2014 02:30:48 PM
     */
    public void showNoInternetConnection() {
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //get message from string.xml
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show();
                Log.e("Error", "No internet connection");
            }
        });
    }

    /**
     * This is used to check email format
     *
     * @param email
     * @return
     * @author Harshal 22-Dec-2014 02:11:51 PM
     */
    public boolean checkEmail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * This is used to check name format
     *
     * @param name
     * @return
     * @author Harshal 22-Dec-2014 02:11:51 PM
     */
    public boolean checkName(String name) {
        boolean isValid = false;

        String expression = "^[a-z]{2,15}$";
        CharSequence inputStr = name;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * This is used to show error message when there is problem in calling webservice
     *
     * @author Harshal 22-Dec-2014 3:59:18 PM
     */
    public void showWebserviceErrorMessage() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, "Error Calling Webservice", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This is used to hide keyboard
     *
     * @param view
     * @author Harshal Dec 26, 2014 1:12:31 PM
     */
    public void hideKeyboard() {
        Activity activity = (Activity) context;
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * This is used to show keyboard
     *
     * @param view
     * @author Harshal Dec 26, 2014 1:12:31 PM
     */
    public void showKeyboard() {
        Activity activity = (Activity) context;
        InputMethodManager lManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void acquireWifiNetwork() {
//		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		WifiLock wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "MyWifiLock");
//		wifiLock.acquire();

        //uses-permission android:name="android.permission.WAKE_LOCK";
    }

}


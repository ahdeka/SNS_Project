package com.example.sns_project;

import android.app.Activity;
import android.widget.Toast;

public class Util {
    public Util(){/**/}

    public static void showToast(Activity activity, String msg){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }
}

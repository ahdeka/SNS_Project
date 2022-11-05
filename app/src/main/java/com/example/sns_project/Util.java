package com.example.sns_project;

import android.app.Activity;
import android.util.Patterns;
import android.widget.Toast;

public class Util {
    public Util(){/**/}

    public static void showToast(Activity activity, String msg){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean isStorageUri(String uri){
        return Patterns.WEB_URL.matcher(uri).matches() && uri.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-3731f.appspot.com/o/posts");
    }

    public static String storageUriToName(String uri){
        return uri.split("\\?")[0].split("%2F")[uri.split("\\?")[0].split("%2F").length - 1];
    }
}

package com.xiaobailong.bluetoothfaultboardcontrol;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.xiaobailong.tools.ConstValue;

import java.io.File;

/**
 * Created by dongyuangui on 2016/11/3.
 */

public class BaseActivity extends Activity{
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = this.getSharedPreferences(ConstValue.SP_NAME,0);
               int orentation = sharedPreferences.getInt(ConstValue.xbl_orentation,1);
               setRequestedOrientation(orentation);
        File file =new File(ConstValue.get_title_File());
        String titleStr = MainActivity.readTitleStr(this,file);
        if(titleStr !=null && !titleStr.equals("")){
            getActionBar().setTitle(titleStr);
        }
    }
}

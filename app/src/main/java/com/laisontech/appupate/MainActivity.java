package com.laisontech.appupate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.laisontech.appupdatelibrary.AppUpdateManager;
import com.laisontech.appupdatelibrary.interfaces.OnAppUpdateListener;

public class MainActivity extends AppCompatActivity implements OnAppUpdateListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new AppUpdateManager(this, this).queryAppInfoFromServer();
    }

    @Override
    public void startCheckUpdate() {
        Toast.makeText(this, "开始下载", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void checkUpdateFinished() {
        Toast.makeText(this, "结束下载", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void checkUpdateError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finishedDialog(boolean forceCancel) {

    }
}

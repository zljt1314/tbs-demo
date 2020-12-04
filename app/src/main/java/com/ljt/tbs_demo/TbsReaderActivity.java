package com.ljt.tbs_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.ljt.tbs.SuperFileView;

import java.io.File;

public class TbsReaderActivity extends AppCompatActivity {

    private SuperFileView superFileView;
    private File mfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tbs_reader);

        superFileView = findViewById(R.id.sfv_content);

        superFileView.setOnGetFilePathListener(new SuperFileView.OnGetFilePathListener() {
            @Override
            public void onGetFilePath(SuperFileView mSuperFileView2) {
                mSuperFileView2.displayFile(mfile);
            }
        });

        String[] list = new String[2];
        list[0] = "android.permission.WRITE_EXTERNAL_STORAGE";
        list[1] = "android.permission.READ_EXTERNAL_STORAGE";
        //判断用户是否已经授权，未授权则向用户申请授权，已授权则直接进行呼叫操作
        if (ContextCompat.checkSelfPermission(this, list[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(list, 105);
            }
        } else {
            openFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        openFile();
    }

    private void openFile() {
        if(!TbsApplication.mIsInitTBSSuccess) {
            Toast.makeText(this, "TBS未加载成功，等待或者用其他方式打开！", Toast.LENGTH_SHORT).show();
            return;
        }
        File filePath = Environment.getExternalStorageDirectory();
        mfile = new File(filePath, "/Android/data/com.tencent.mm/MicroMsg/Download/简历.docx");
        if(!mfile.exists()) {
            Toast.makeText(this, mfile.getAbsolutePath() + "不存在！", Toast.LENGTH_SHORT).show();
            return;
        }
        superFileView.show();
    }

    @Override
    protected void onDestroy() {
        if (superFileView != null) {
            superFileView.onStopDisplay();
        }
        super.onDestroy();
    }
}
package com.ljt.tbs_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Administrator
 */
public class MainActivity extends AppCompatActivity {

    private Button btnReadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnReadFile = findViewById(R.id.btn_read_file);

        btnReadFile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TbsReaderActivity.class));
            }
        });
    }

}
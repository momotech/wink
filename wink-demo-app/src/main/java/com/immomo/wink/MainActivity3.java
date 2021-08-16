package com.immomo.wink;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.immomo.wink.utils.ZZ;

@Route(path = "/com/Activity333")
public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Log.e("Test","12321");
        Toast.makeText(this, "Activity3", Toast.LENGTH_SHORT).show();
    }
}
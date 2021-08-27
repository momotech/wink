package com.immomo.wink;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;

@Route(path = "/com/Activity333")
public class MainActivity3 extends AppCompatActivity {

    //打开注释验证 EventBus
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(MessageEvent event) {
//        Log.d("EventBus", "MainActivity 33333 onMessageEvent run !!!");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Log.e("Test","121231113");
        Toast.makeText(this, "Activity331233 ===", Toast.LENGTH_SHORT).show();

        findViewById(R.id.button).setOnClickListener(v -> {
            //打开注释验证 EventBus
//            EventBus.getDefault().post(new MessageEvent());
        });

        //打开注释验证 EventBus
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //打开注释验证 EventBus
//        EventBus.getDefault().register(this);
    }
}
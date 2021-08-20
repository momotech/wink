package com.immomo.wink;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.immomo.wink.utils.ZZ;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@Route(path = "/com/Activity1")
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button btn;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d("EventBus", "MainActivity onMessageEvent run !!!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
        System.out.println("Route.class.getCanonicalName() : " + Route.class.getCanonicalName());
        Button button = findViewById(R.id.button);
        button.setBackgroundColor(Color.BLACK);

        TextView textView = findViewById(R.id.textView);
        textView.setText(new Test111().getAAA() + "2" + new ZZ().getKK());
//        textView.setText(Tools.getTitle() + "xx1");

        textView.setOnClickListener((v)->{
            ARouter.getInstance().build("/com/Activity22").navigation();
            EventBus.getDefault().post(new MessageEvent());
        });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ARouter.getInstance().build("/com/Activity333").navigation();
                return false;
            }
        });
//        loadMusic(R.raw.audio_match);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void loadMusic(int rawID){
        Log.i("rawID", "rawId=" + rawID);
        Resources resources = this.getResources();

        AssetFileDescriptor afd = resources.openRawResourceFd(rawID);
    }
}
package com.immomo.wink;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.immomo.wink.utils.ZZ;

@Route(path = "/com/Activity1")
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setBackgroundColor(Color.BLACK);

        TextView textView = findViewById(R.id.textView);
        textView.setText(new Test111().getAAA() + "2" + new ZZ().getKK());
//        textView.setText(Tools.getTitle() + "xx1");

        textView.setOnClickListener((v)->{
            Toast.makeText(this, "221" + new ZZ().getKK(), Toast.LENGTH_SHORT).show();
        });
//        loadMusic(R.raw.audio_match);
    }

    private void loadMusic(int rawID){
        Log.i("rawID", "rawId=" + rawID);
        Resources resources = this.getResources();

        AssetFileDescriptor afd = resources.openRawResourceFd(rawID);
    }
}
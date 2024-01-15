package com.immomo.wink

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = "/com/Activity22")
class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        Toast.makeText(this, "Activity222222==", Toast.LENGTH_SHORT).show()
        Log.e("Test", "123");
        EventBus.getDefault().register(this)

        findViewById<View>(R.id.button).setOnClickListener {
            EventBus.getDefault().post(MessageEvent())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        Log.d("EventBus", "MainActivity 222222222222222222 onMessageEvent run !!!")
    }
}
package com.immomo.wink

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/com/Activity22")
class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Toast.makeText(this, "Activity222222==", Toast.LENGTH_SHORT).show()
        Log.e("Test", "123");
    }
}
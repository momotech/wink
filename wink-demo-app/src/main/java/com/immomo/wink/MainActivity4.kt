package com.immomo.wink

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Route
//import com.immomo.wink.databinding.ActivityMain4Binding
import org.greenrobot.eventbus.EventBus

@Route(path = "/com/Activity4")
class MainActivity4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
//        val bindingBinding : ActivityMain4Binding = DataBindingUtil.setContentView(this, R.layout.activity_main4)
//        bindingBinding.cartoon = Cartoon()
    }

    fun doClick(view: View){
        Toast.makeText(this, "111111111111111", Toast.LENGTH_SHORT).show()
    }
}
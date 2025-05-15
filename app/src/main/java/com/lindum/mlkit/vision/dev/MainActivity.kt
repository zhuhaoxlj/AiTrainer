package com.lindum.mlkit.vision.dev

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.lindum.mlkit.vision.dev.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.customTabBar.setOnTabSelectedListener(object :
            CustomTabBarView.OnTabSelectedListener {
            override fun onTabSelected(position: Int) {
                when (position) {
                    0 -> {
                        // 动作库
                        Toast.makeText(this@MainActivity, "动作库", Toast.LENGTH_SHORT).show()
                        // TODO: 切换到动作库Fragment
                    }

                    1 -> {
                        // 中间按钮
                        CameraXLivePreviewActivity.start(this@MainActivity)
                    }

                    2 -> {
                        // 日记
                        Toast.makeText(this@MainActivity, "日记", Toast.LENGTH_SHORT).show()
                        // TODO: 切换到日记Fragment
                    }
                }
            }
        })
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
} 
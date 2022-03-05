package com.example.iot_finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.iot_finalproject.adapter.ViewPagerAdapter
import com.example.iot_finalproject.databinding.ActivityMainBinding
import com.example.iot_finalproject.fragment.FirstFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: ViewPagerAdapter
    private val fragments = arrayOf<Fragment>(
        FirstFragment()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setViewPager()
    }

    private fun setViewPager() {
        binding.run {
            adapter = ViewPagerAdapter(fragments, supportFragmentManager)
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 3
        }
    }
}
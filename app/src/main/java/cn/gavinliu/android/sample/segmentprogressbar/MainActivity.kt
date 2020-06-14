package cn.gavinliu.android.sample.segmentprogressbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        segment.setData(arrayListOf(3, 3, 3, 3))

        btn_plus.setOnClickListener { segment.progressPlus() }
        btn_minus.setOnClickListener { segment.progressMinus() }
    }
}
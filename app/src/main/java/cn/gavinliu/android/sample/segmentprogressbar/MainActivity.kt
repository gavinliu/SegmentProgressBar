package cn.gavinliu.android.sample.segmentprogressbar

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.gavinliu.android.widget.SegmentProgressBar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        segment.setData(
            arrayListOf(
                SegmentProgressBar.Segment(progress = 3, color = Color.parseColor("#fccb3c")),
                SegmentProgressBar.Segment(progress = 3, color = Color.parseColor("#fb9d15")),
                SegmentProgressBar.Segment(progress = 3, color = Color.parseColor("#f96712"))
            )
        )

        btn_plus.setOnClickListener { segment.progressPlus() }
        btn_minus.setOnClickListener { segment.progressMinus() }
    }
}
package com.tilseier.starsforprogressbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var addInt = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        btn_change_progress.setOnClickListener {
            if (horizontal_progress_bar.progress >= horizontal_progress_bar.max) {
                addInt = -10
            } else if (horizontal_progress_bar.progress <= 0) {
                addInt = 10
            }
            horizontal_progress_bar.progress += addInt
            progress_stars.setProgress(horizontal_progress_bar.progress)
        }

    }
}

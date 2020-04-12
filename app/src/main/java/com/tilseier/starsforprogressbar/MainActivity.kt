package com.tilseier.starsforprogressbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var addProgress = 10
    var addProgress2 = 10
    var addProgress3 = 10
    var addProgress4 = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addProgress = horizontal_progress_bar.max / 10
        addProgress2 = horizontal_progress_bar_2.max / 10
        addProgress3 = horizontal_progress_bar_3.max / 10
        addProgress4 = horizontal_progress_bar_4.max / 10

        btn_change_progress.setOnClickListener {
            //first progress bar
            if (horizontal_progress_bar.progress >= horizontal_progress_bar.max) {
                addProgress = -(horizontal_progress_bar.max / 10)
            } else if (horizontal_progress_bar.progress <= 0) {
                addProgress = (horizontal_progress_bar.max / 10)
            }
            horizontal_progress_bar.progress =
                if (horizontal_progress_bar.progress + addProgress < horizontal_progress_bar.max) horizontal_progress_bar.progress + addProgress else horizontal_progress_bar.max
            progress_stars.setProgress(horizontal_progress_bar.progress)

            //second progress bar
            if (horizontal_progress_bar_2.progress >= horizontal_progress_bar_2.max) {
                addProgress2 = -(horizontal_progress_bar_2.max / 10)
            } else if (horizontal_progress_bar_2.progress <= 0) {
                addProgress2 = (horizontal_progress_bar_2.max / 10)
            }
            horizontal_progress_bar_2.progress =
                if (horizontal_progress_bar_2.progress + addProgress2 < horizontal_progress_bar_2.max) horizontal_progress_bar_2.progress + addProgress2 else horizontal_progress_bar_2.max
            progress_stars_2.setProgress(horizontal_progress_bar_2.progress)

            //third progress bar
            if (horizontal_progress_bar_3.progress >= horizontal_progress_bar_3.max) {
                addProgress3 = -(horizontal_progress_bar_3.max / 10)
            } else if (horizontal_progress_bar_3.progress <= 0) {
                addProgress3 = (horizontal_progress_bar_3.max / 10)
            }
            horizontal_progress_bar_3.progress =
                if (horizontal_progress_bar_3.progress + addProgress3 < horizontal_progress_bar_3.max) horizontal_progress_bar_3.progress + addProgress3 else horizontal_progress_bar_3.max
            progress_stars_3.setProgress(horizontal_progress_bar_3.progress)

            //fourth progress bar
            if (horizontal_progress_bar_4.progress >= horizontal_progress_bar_4.max) {
                addProgress4 = -(horizontal_progress_bar_4.max / 10)
            } else if (horizontal_progress_bar_4.progress <= 0) {
                addProgress4 = (horizontal_progress_bar_4.max / 10)
            }
            horizontal_progress_bar_4.progress =
                if (horizontal_progress_bar_4.progress + addProgress4 < horizontal_progress_bar_4.max) horizontal_progress_bar_4.progress + addProgress4 else horizontal_progress_bar_4.max
            progress_stars_4.setProgress(horizontal_progress_bar_4.progress)

            Log.e("MAIN_ACTIVITY",  """"
                progress_stars: ${progress_stars.getAchievementsCount()}"
                progress_stars_2: ${progress_stars_2.getAchievementsCount()}"
                progress_stars_3: ${progress_stars_3.getAchievementsCount()}"
                progress_stars_4: ${progress_stars_4.getAchievementsCount()}"
                """".trimMargin()
                )
        }

    }
}

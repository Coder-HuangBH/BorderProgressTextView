package com.hbh.simpleproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hbh.borderprogresstextview.BorderProgressTextView

class MainActivity : AppCompatActivity() {

    private lateinit var bp_tv1 : BorderProgressTextView
    private lateinit var bp_tv2 : BorderProgressTextView
    private lateinit var bp_tv3 : BorderProgressTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bp_tv1 = findViewById(R.id.bp_tv1)
        bp_tv2 = findViewById(R.id.bp_tv2)
        bp_tv3 = findViewById(R.id.bp_tv3)

        bp_tv1.setListener(object : BorderProgressTextView.OnEventListener {
            var count = 0
            override fun onAnimationRepeat() {
                count++
                bp_tv1.text = "X$count"
            }

            override fun onAnimationEnd() {
                count = 0
                bp_tv1.text = "X END"
            }
        })

        bp_tv2.setListener(object : BorderProgressTextView.OnEventListener {
            var count = 0
            override fun onAnimationRepeat() {
                count++
                bp_tv2.text = "X$count"
            }

            override fun onAnimationEnd() {
                count = 0
                bp_tv2.text = "X END"
            }
        })

        bp_tv3.setListener(object : BorderProgressTextView.OnEventListener {
            var count = 0
            override fun onAnimationRepeat() {
                count++
                bp_tv3.text = "X$count"
            }

            override fun onAnimationEnd() {
                count = 0
                bp_tv3.text = "X END"
            }
        })

        bp_tv1.setOnClickListener {
            bp_tv1.text = "X START"
            bp_tv2.text = "X START"
            bp_tv3.text = "X START"
            bp_tv1.startProgressAnimation()
            bp_tv2.startProgressAnimation()
            bp_tv3.startProgressAnimation()
        }


        var view = BorderProgressTextView(this)
    }
}
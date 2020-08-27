package de.unilandau.unild_zeiterfassung

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_watch.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime


class WatchFragment : Fragment() {


    lateinit var chronometer: Chronometer
    lateinit var pauseChronometer: Chronometer
    lateinit var v: View
    lateinit var timeBegin : LocalDateTime
    lateinit var timeEnd : LocalDateTime

    private val dayFragment = DayFragment()


    @SuppressLint("SetTextI18n")
    @ExperimentalTime
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v = inflater.inflate(R.layout.fragment_watch, container, false)

        var running = false
        val firstRun = true
        val job = ""
        val annotation = ""
        var offset: Long = 0
        var pauseOffset: Long = 0
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

        chronometer = v.findViewById(R.id.chronometer)
        pauseChronometer = v.findViewById(R.id.textViewPause)

        v.startButton.setOnClickListener {
            if (!running) {
                if (firstRun) {
                    visibility()
                    timeBegin = LocalDateTime.now()
                    val text = timeBegin.format(formatter)
                    val parsedDate = LocalDateTime.parse(text,formatter)
                    val dateBegin = parsedDate.toLocalTime()
                    v.textViewDate.text = dateBegin.toString()
                    v.textViewDateValue.text = text
                } else {
                    pauseChronometer.stop()
                    pauseOffset = SystemClock.elapsedRealtime() - pauseChronometer.base
                }
                chronometer.base = SystemClock.elapsedRealtime() - offset
                chronometer.start()
                running = true
            }
        }

        v.dayFragmentButton.setOnClickListener() {
            timeBegin =  LocalDateTime.now()
            timeEnd =  LocalDateTime.now()
            pauseOffset = 0

            val timeRecording = TimeRecording(timeBegin.toString(),timeEnd.toString(), pauseOffset.toString(), job, annotation)

            val db = DBHandler(v.context)

            db.insertData(timeRecording)
            val data = db.readLastData()
            var dataId  = ""
            for (i in 0 until data.size){
                dataId = data[i].id.toString()
            }

            val args = Bundle()
            args.putString("id",dataId)
            dayFragment.arguments = args

            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, dayFragment)
            fragmentTransaction.commit()

        }


        v.pauseButton.setOnClickListener {
            if (running) {
                pauseChronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                pauseChronometer.start()
                offset = SystemClock.elapsedRealtime() - chronometer.base
                v.pauseButton.text = "Pause beenden"
                running = false
            } else {
                pauseOffset = SystemClock.elapsedRealtime() - pauseChronometer.base
                pauseChronometer.stop()
                v.pauseButton.text = "Pause starten"
                running = true
            }
        }

        v.stopButton.setOnClickListener {
            chronometer.stop()
            pauseChronometer.stop()
            if (pauseOffset> 0 ) {
                pauseOffset = SystemClock.elapsedRealtime() - pauseChronometer.base
            }
            timeEnd = LocalDateTime.now()
            val timeRecording = TimeRecording(timeBegin.toString(),timeEnd.toString(), pauseOffset.toString(), job, annotation)

            val db = DBHandler(v.context)
            db.insertData(timeRecording)
            val data = db.readLastData()
            var dataId  = ""
                for (i in 0 until data.size){
                     dataId = data.get(i).id.toString()
                }

            val args = Bundle()
            args.putString("id",dataId)
            dayFragment.arguments = args

            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, dayFragment)
            fragmentTransaction.commit()

        }




        return v
    }
    private fun visibility(){
        v.startButton.visibility = View.INVISIBLE
        v.dayFragmentButton.visibility = View.INVISIBLE
        v.chronometer.visibility = View.VISIBLE
        v.textViewPause.visibility = View.VISIBLE
        v.textViewDay.visibility = View.VISIBLE
        v.textViewDateValue.visibility = View.VISIBLE
        v.textViewText.visibility = View.VISIBLE
        v.textViewDate.visibility = View.VISIBLE
        v.pauseButton.visibility = View.VISIBLE
        v.stopButton.visibility = View.VISIBLE
        v.textViewPauseText.visibility = View.VISIBLE
        v.progressBar.visibility = View.VISIBLE
    }

}


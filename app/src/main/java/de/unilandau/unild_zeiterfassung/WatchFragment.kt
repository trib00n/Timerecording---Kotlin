package de.unilandau.unild_zeiterfassung

import android.icu.util.TimeUnit
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_day.*
import kotlinx.android.synthetic.main.fragment_watch.view.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.ExperimentalTime


class WatchFragment : Fragment() {


    lateinit var chronometer: Chronometer
    lateinit var pauseChronometer: Chronometer
    lateinit var v: View
    lateinit var timeBegin : LocalDateTime
    lateinit var timeEnd : LocalDateTime

    private val workFragment = WorkFragment()
    private val dayFragment = DayFragment()




    @ExperimentalTime
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v = inflater.inflate(R.layout.fragment_watch, container, false)


        //  val date = Calendar.getInstance().time
        // val formatter = SimpleDateFormat.getDateTimeInstance() //or use getDateInstance()
        //  val formatedDate = formatter.format(date)
        var running = false
        var firstRun = true
        var job = ""
        var annotation = ""
        var Offset: Long = 0
        var pauseOffset: Long = 0
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")


        chronometer = v.findViewById(R.id.chronometer)
        pauseChronometer = v.findViewById(R.id.textViewPause)

        v.startButton.setOnClickListener {
            if (!running) {
                if (firstRun) {
                    visibility()
                    timeBegin = LocalDateTime.now()
                    var text = timeBegin.format(formatter)
                    var parsedDate = LocalDateTime.parse(text,formatter)
                    Log.d("Zeit:" , text)
                    Log.d("Zeit:" , parsedDate.toString())
                    var dateBegin = parsedDate.toLocalTime()


                    v.textViewDate.text = dateBegin.toString()

                    v.textViewDateValue.text = text
                } else {

                    pauseChronometer.stop()
                    pauseOffset = SystemClock.elapsedRealtime() - pauseChronometer.base
                    // v.textViewPause.text = "Pausen: $minutes Minuten $pause"
                }

                chronometer.base = SystemClock.elapsedRealtime() - Offset
                chronometer.start()
                running = true
            }
        }

        v.dayFragmentButton.setOnClickListener() {

            timeBegin =  LocalDateTime.now()
            timeEnd =  LocalDateTime.now()
            pauseOffset = 0


            var timeRecording = TimeRecording(timeBegin.toString(),timeEnd.toString(), pauseOffset.toString(), job, annotation)

            var db = DBHandler(v.context)

            db.insertData(timeRecording)


            var data = db.readLastData()
            var dataId : String = ""

            for (i in 0 until data.size){
                dataId = data.get(i).id.toString()
            }

            Log.d("Mein Log", "DataID: " + dataId)

            Log.d("MeinLog", timeRecording.toString())

            val args = Bundle()
            args.putString("id",dataId)
            dayFragment.arguments = args



            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.fragment_container, dayFragment)
            fragmentTransaction?.commit()

        }


        v.pauseButton.setOnClickListener {
            if (running) {
                //chronometer.stop()
                pauseChronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                pauseChronometer.start()
                Offset = SystemClock.elapsedRealtime() - chronometer.base
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


            Log.d("MeinLog", "pauseChronometer" + pauseChronometer.base)
            Log.d("MeinLog", "SystemClock.elapsedRealtime() " +SystemClock.elapsedRealtime() )
            Log.d("MeinLog", "pauseOffset" + pauseOffset)
            if (pauseOffset> 0 ) {
                Log.d("MeinLog", "Jap" + pauseOffset)
                pauseOffset = SystemClock.elapsedRealtime() - pauseChronometer.base
            }

            Log.d("MeinLog", "pauseOffset2" + pauseOffset)

            timeEnd = LocalDateTime.now();


            var timeRecording = TimeRecording(timeBegin.toString(),timeEnd.toString(), pauseOffset.toString(), job, annotation)

            var db = DBHandler(v.context)

            db.insertData(timeRecording)


            var data = db.readLastData()
            var dataId : String = ""

                for (i in 0 until data.size){
                     dataId = data.get(i).id.toString()
                }

            Log.d("MeinLog", "DataID" + dataId)
            Log.d("MeinLog", timeRecording.toString())

            val args = Bundle()
            args.putString("id",dataId)
            dayFragment.arguments = args

            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.fragment_container, dayFragment)
            fragmentTransaction?.commit()

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


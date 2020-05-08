package de.unilandau.unild_zeiterfassung

import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_watch.view.*
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class WatchFragment : Fragment() {


    lateinit var chronometer: Chronometer
    lateinit var pauseChronometer: Chronometer
    lateinit var v: View
    lateinit var timeBegin : LocalDateTime
    lateinit var timeEnd : LocalDateTime

    private val workFragment = WorkFragment()
    private val dayFragment = DayFragment()



    @RequiresApi(Build.VERSION_CODES.N)
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
        var Offset: Long = 0
        var pauseOffset: Long = 0
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")


        chronometer = v.findViewById(R.id.chronometer)
        pauseChronometer = v.findViewById(R.id.textViewPause)

        v.startButton.setOnClickListener {
            if (!running) {
                if (firstRun) {

                    timeBegin = LocalDateTime.now();
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
                v.startButton.visibility = View.INVISIBLE
                v.workFragmentButton.visibility = View.INVISIBLE
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
                chronometer.base = SystemClock.elapsedRealtime() - Offset
                chronometer.start()
                running = true
            }
        }

        v.workFragmentButton.setOnClickListener() {
            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.fragment_container, workFragment)
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
            Offset = SystemClock.elapsedRealtime() - chronometer.base
            pauseOffset = SystemClock.elapsedRealtime() - pauseChronometer.base

            timeEnd = LocalDateTime.now();
            Log.d("Gemessene Zeit:", chronometer.base.toString())
            Log.d("Pause Zeit:", pauseChronometer.base.toString())

           var timeRecording = TimeRecording(timeBegin.toString(),timeEnd.toString(),pauseChronometer.base.toString())

            var db = DBHandler(v.context)

            db.insertData(timeRecording)


            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.fragment_container, dayFragment)
            fragmentTransaction?.commit()

        }




        return v
    }


}


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
    lateinit var timeBeginEnd : LocalDateTime


    private val dayFragment = DayFragment()


    @SuppressLint("SetTextI18n")
    @ExperimentalTime
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v = inflater.inflate(R.layout.fragment_watch, container, false)

        var running = true
        val db = DBHandler(v.context)
        val job = ""
        val annotation = ""
        var offset: Long = 0
        var pauseOffset: Long = 0
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")


        chronometer = v.findViewById(R.id.chronometerTime)
        pauseChronometer = v.findViewById(R.id.chronometerPause)

        /* Anzeige wechselt auf Zeitmessungs, Startzeit wird festgelegt, ChronometerTime wird gestartet */
        v.startButton.setOnClickListener {
                visibility()
                timeBegin = LocalDateTime.now()
                val ft = timeBegin.format(formatter)
                val parsedDate = LocalDateTime.parse(ft,formatter)
                val dateBegin = parsedDate.toLocalTime()
                v.textViewStartTimeValue.text = dateBegin.toString()
                chronometer.base = SystemClock.elapsedRealtime() - offset
                chronometer.start()
        }
        /* Stoppt und startet ChronometerPause */
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

        /* Beendet ChronometerTime und ChronometerPause, Legt Endezeit und Pausezeit fest, Schreibt Daten in Datenbank */
        v.stopButton.setOnClickListener {
            chronometer.stop()
            pauseChronometer.stop()
            if (pauseOffset> 0 ) {
                pauseOffset = SystemClock.elapsedRealtime() - pauseChronometer.base
            }
            timeEnd = LocalDateTime.now()
            val timeRecording = TimeRecording(timeBegin.toString(),timeEnd.toString(), pauseOffset.toString(), job, annotation)
            db.insertData(timeRecording)
            openDayFragment()
        }

        /* Legt BeginEndezeit auf jetzt und Pausezeit auf 0, Schreibt Daten in Datenbank, Sucht die letzte ID und öffnet damit das DayFragment   */
        v.createEntryButton.setOnClickListener() {
            timeBeginEnd =  LocalDateTime.now()
            pauseOffset = 0
            val timeRecording = TimeRecording(timeBeginEnd.toString(),timeBeginEnd.toString(), pauseOffset.toString(), job, annotation)
            db.insertData(timeRecording)
            openDayFragment()
        }

        return v
    }


    /* Button zur Wahl zwischen Zeitmessung und Eintrag hinzufügen werden unsichtbar. Alle anderen Elemente werden Sichtbar für Zeitmessungsfunktion */
    private fun visibility(){
        v.startButton.visibility = View.INVISIBLE
        v.createEntryButton.visibility = View.INVISIBLE
        v.chronometerTime.visibility = View.VISIBLE
        v.chronometerPause.visibility = View.VISIBLE
        v.textViewPause.visibility = View.VISIBLE
        v.textViewMeasure.visibility = View.VISIBLE
        v.pauseButton.visibility = View.VISIBLE
        v.stopButton.visibility = View.VISIBLE
        v.divider1.visibility = View.VISIBLE
        v.divider2.visibility = View.VISIBLE
    }

    /* Sucht die letzte ID und öffnet damit das DayFragment */
    private fun openDayFragment() {
        val db = DBHandler(v.context)
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

}


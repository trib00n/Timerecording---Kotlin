package de.unilandau.unild_zeiterfassung

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_day.view.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import java.io.File
import java.io.FileInputStream
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class SettingsFragment : Fragment() {
    lateinit var v: View
    private val TAG = "SettingsFragment"
    var parseFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    var dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_settings, container, false)
        var buttonExport = v.findViewById<Button>(R.id.buttonExport)
        var checkBox = v.findViewById<CheckBox>(R.id.checkBox)
        val FILE_NAME = "Zeiterfassung.csv"
        val file = File(v.context.externalCacheDir, FILE_NAME)
        if (file.exists()) {
            file.delete()
        } else {
            file.createNewFile()
        }




        v.buttonExport.setOnClickListener() {

            val CSV_HEADER = "Anfangsdatum, Anfangszeitpunkt, Endedatum, Endezeitpunkt,Pause,Arbeitszeit,Tätigkeits,Anmerkung"
            file.appendText(CSV_HEADER)
            file.appendText('\n'.toString())

            //  var uri =  FileProvider.getUriForFile(v.context,"de.unilandau.unild_zeiterfassung.fileprovider",file)
            // Datenbank lesen
            var db = DBHandler(v.context)
            var data = db.readDataByDate("2020-08-01", "2020-08-23")

            //var data =  db.readAllData()
            if (data != null) {
                for (i in 0 until data.size) {
                    var id = data.get(i).id
                    var beginTime = data.get(i).begin
                    var endTime = data.get(i).end
                    var parsedBegin = LocalDateTime.parse(beginTime,parseFormatter)
                    var parsedEnd = LocalDateTime.parse(endTime,parseFormatter)
                    var formattedBeginTime = parsedBegin.format(timeFormatter)
                    var formattedBeginDate = parsedBegin.format(dateFormatter)
                    var formattedEndTime = parsedEnd.format(timeFormatter)
                    var formattedEndDate = parsedEnd.format(dateFormatter)

                    var pauseTime = data.get(i).pause
                    var parsedPause = Duration.ofMillis(pauseTime.toLong())
                    var ltPause = LocalTime.ofNanoOfDay( parsedPause.toNanos())
                    var formattedParsedPause = ltPause.format(timeFormatter)

                    var timeWorked = calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                    var job = data.get(i).job.replace("[\\n\\t ]","")
                    var annotation = data.get(i).annotation.replace("[\\n\\t ]","")
                    file.appendText(formattedBeginDate.toString())
                    file.appendText(','.toString())
                    file.appendText(formattedBeginTime.toString())
                    file.appendText(','.toString())
                    file.appendText(formattedEndDate.toString())
                    file.appendText(','.toString())
                    file.appendText(formattedEndTime.toString())
                    file.appendText(','.toString())
                    file.appendText(formattedParsedPause.toString())
                    file.appendText(','.toString())
                    file.appendText(timeWorked)
                    file.appendText(','.toString())

                    file.appendText(job)
                    file.appendText(','.toString())
                    file.appendText(annotation)
                    file.appendText('\n'.toString())

                    if(checkBox.isChecked){
                        db.deleteData(id.toString())
                    }

                }
            }










            file.setReadable(true, false);
            val readResult = FileInputStream(file).bufferedReader().use { it.readText() }
            val path = Uri.fromFile(file)
            Log.d(TAG,"readResult=$readResult,file=$file,path=$path")
          val sendIntent = Intent()
          sendIntent.action = Intent.ACTION_SEND
          sendIntent.type = "message/rfc822";
          sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Zeiterfassung")
          sendIntent.putExtra(Intent.EXTRA_TEXT, "Sehr geehrte Damen und Herren, \n Anbei finden Sie die Zeiterfassung!\n Mit freundlichen Grüßen,\n Ihr URZ-Team")
          sendIntent.setPackage("com.google.android.gm");
          sendIntent.putExtra(Intent.EXTRA_STREAM,path)
          sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          startActivityForResult(Intent.createChooser(sendIntent, "E-Mail versenden..."),12)
        }

        return v
    }

    private fun calculateTimeValue(parsedBegin: LocalDateTime, parsedEnd: LocalDateTime, parsedPause: Duration): String {
        var diff = Duration.between(parsedBegin,parsedEnd)
        Log.d(TAG, "diff: "+ diff.toString())
        var diffMinusPause = diff - parsedPause
        var hours = diffMinusPause.toHours()
        var minutes = ((diffMinusPause.seconds % (60*60)) / 60)
        var fh = if(hours<10  &&  hours>=0){
            "0$hours"
        } else {
            hours.toString()
        }
        var fm = if(minutes<10 &&  minutes>=0){
            "0$minutes"
        } else {
            minutes.toString()
        }
        return "$fh:$fm h"
    }


}



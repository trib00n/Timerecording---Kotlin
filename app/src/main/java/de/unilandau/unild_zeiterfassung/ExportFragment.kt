package de.unilandau.unild_zeiterfassung

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_settings.view.*
import java.io.File
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class ExportFragment : Fragment() {
    lateinit var v: View

    var parseFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")


    @SuppressLint("SetTextI18n", "SetWorldReadable")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_settings, container, false)


       // var buttonExport = v.findViewById<Button>(R.id.buttonExport)
        val checkBox = v.findViewById<CheckBox>(R.id.checkBox)
        val textViewAmount = v.findViewById<TextView>(R.id.textViewAmount)

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        var pickedBeginDate = "2000-01-01"
        var pickedEndDate = "2100-01-01"
        v.editTextBegin.text = "01.01.2000"
        v.editTextEnd.text = "01.01.2100"

        textViewAmount.text = calculateAmount(pickedBeginDate, pickedEndDate)

        val fileName = "Zeiterfassung.csv"
        val file = File(v.context.externalCacheDir, fileName)
        if (file.exists()) file.delete() else file.createNewFile()
        file.setReadable(true, false)
        val path = Uri.fromFile(file)

        v.editTextBegin.setOnClickListener {
            val dpd = DatePickerDialog(v.context,
                DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                    val realMonth = mMonth + 1
                    val fm = addLeadingZeros(realMonth.toString())
                    val fd = addLeadingZeros(mDay.toString())

                    pickedBeginDate = "$mYear-$fm-$fd"
                    v.editTextBegin.text = "$fd.$fm.$mYear"
                    v.textViewAmount.text = calculateAmount(pickedBeginDate, pickedEndDate)

                }, year, month, day
            )
            dpd.show()
        }

        v.editTextEnd.setOnClickListener {
            val dpd = DatePickerDialog(v.context,
                DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDay ->
                    val realMonth = mMonth + 1
                    val fm = addLeadingZeros(realMonth.toString())
                    val fd = addLeadingZeros(mDay.toString())

                    pickedEndDate = "$mYear-$fm-$fd"
                    v.editTextEnd.text = "$fd.$fm.$mYear"

                    v.textViewAmount.text = calculateAmount(pickedBeginDate, pickedEndDate)
                }, year, month, day
            )
            dpd.show()
        }

        v.buttonExport.setOnClickListener {

            val csvHeader = "Anfangsdatum, Anfangszeitpunkt, Endedatum,Endezeitpunkt,Pause,Arbeitszeit,Tätigkeits,Anmerkung"
            file.appendText(csvHeader)
            file.appendText('\n'.toString())


            val db = DBHandler(v.context)
            val data = db.readDataByDate(pickedBeginDate, pickedEndDate)

            for (i in 0 until data.size) {
                val id = data[i].id
                val beginTime = data[i].begin
                val endTime = data[i].end
                val parsedBegin = LocalDateTime.parse(beginTime, parseFormatter)
                val parsedEnd = LocalDateTime.parse(endTime, parseFormatter)
                val pauseTime = data[i].pause
                val parsedPause = Duration.ofMillis(pauseTime.toLong())
                val ltPause = LocalTime.ofNanoOfDay(parsedPause.toNanos())

                file.appendText( parsedBegin.format(dateFormatter).toString())
                file.appendText(','.toString())
                file.appendText( parsedBegin.format(timeFormatter).toString())
                file.appendText(','.toString())
                file.appendText(parsedEnd.format(dateFormatter).toString())
                file.appendText(','.toString())
                file.appendText(parsedEnd.format(timeFormatter).toString())
                file.appendText(','.toString())
                file.appendText(ltPause.format(timeFormatter).toString())
                file.appendText(','.toString())
                file.appendText(calculateTimeValue(parsedBegin, parsedEnd, parsedPause))
                file.appendText(','.toString())
                file.appendText(data[i].job.replace("[\\n\\t ]", ""))
                file.appendText(','.toString())
                file.appendText(data[i].annotation.replace("[\\n\\t ]", ""))
                file.appendText('\n'.toString())

                if (checkBox.isChecked) db.deleteData(id.toString())

            }
            sendMailWithAttachment(path)
            calculateAmount(pickedBeginDate,pickedEndDate)
        }

        return v
    }

    private fun calculateTimeValue(parsedBegin: LocalDateTime, parsedEnd: LocalDateTime, parsedPause: Duration): String {
        val diff = Duration.between(parsedBegin, parsedEnd)
        val diffMinusPause = diff - parsedPause
        val hour = diffMinusPause.toHours()
        val minute = ((diffMinusPause.seconds % (60 * 60)) / 60)
        val fh = addLeadingZeros(hour.toString())
        val fm = addLeadingZeros(minute.toString())
        return "$fh:$fm h"
    }

    private fun calculateAmount(pickedBeginDate: String, pickedEndDate: String): String {
        val db = DBHandler(v.context)
        val data = db.readDataByDate(pickedBeginDate, pickedEndDate)
        return data.size.toString()
    }

    private fun addLeadingZeros(str: String) : String {
        return if (str.toInt() in 0..9) "0$str" else str
    }

    private fun sendMailWithAttachment(path: Uri){
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "message/rfc822"
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Zeiterfassung")
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Sehr geehrte Damen und Herren, \n Anbei finden Sie die Zeiterfassung!\n Mit freundlichen Grüßen,\n Ihr URZ-Team"
        )
        sendIntent.setPackage("com.google.android.gm")
        sendIntent.putExtra(Intent.EXTRA_STREAM, path)
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(Intent.createChooser(sendIntent, "E-Mail versenden..."), 12)
    }


}



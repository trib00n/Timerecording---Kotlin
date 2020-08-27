package de.unilandau.unild_zeiterfassung


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_day.view.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class DayFragment : Fragment() {
    lateinit var v: View
    lateinit var parsedBegin : LocalDateTime
    lateinit var parsedEnd : LocalDateTime
    lateinit var parsedPause : Duration
    lateinit var formattedParsedPause : String
    var job : String = ""
    var annotation : String = ""

  //  private val workFragment : Fragment = WorkFragment()
    val TAG = "DayFragment"
    var parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    var dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var dateFormatterLocalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")


    @RequiresApi(Build.VERSION_CODES.N)


 // wird benötigt da es Probleme mit dem Zurücksetzten von EditView bei Fragments gibt
 // https://stackoverflow.com/questions/13303469/edittext-settext-not-working-with-fragment
    override fun onResume() {
        super.onResume()

        val id = arguments?.getString("id")

        // Datenbank lesen
        val db = DBHandler(v.context)
        val data = id?.let { db.readDataById(it) }
        if (data != null) {
            for (i in 0 until data.size) {
                annotation = data[i].annotation
                job = data.get(i).job
            }
            v.editTextAcitvity.setText(job)
            v.editTextAnnotation.setText(annotation)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v = inflater.inflate(R.layout.fragment_day, container, false)

        val id = arguments?.getString("id")

        var beginTime = ""
        var endTime = ""
        var pauseTime = ""
        var job = ""
        var annotation = ""
        var pickedBeginDate : String
        var pickedEndDate : String
        val formattedEndTime : String
        val formattedEndDate : String
        val formattedBeginTime  : String
        val formattedBeginDate : String

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        clearTextViews()

        // Datenbank lesen
        val db = DBHandler(v.context)
        val data = id?.let { db.readDataById(it) }
        if (data != null) {
            for (i in 0 until data.size){
                beginTime = data.get(i).begin
                endTime = data.get(i).end
                pauseTime = data.get(i).pause
                job = data.get(i).job
                annotation = data.get(i).annotation
            }

            parsedBegin = LocalDateTime.parse(beginTime,parseFormatter)
            parsedEnd = LocalDateTime.parse(endTime,parseFormatter)

            formattedBeginTime = parsedBegin.format(timeFormatter)
            formattedBeginDate = parsedBegin.format(dateFormatter)
            formattedEndTime = parsedEnd.format(timeFormatter)
            formattedEndDate = parsedEnd.format(dateFormatter)

            v.editTextAcitvity.setText(job)
            v.editTextAnnotation.setText(annotation)

            parsedPause = Duration.ofMillis(pauseTime.toLong())
            val ltPause = LocalTime.ofNanoOfDay( parsedPause.toNanos())
            formattedParsedPause = ltPause.format(timeFormatter)

            calculateTimeValue(parsedBegin,parsedEnd,parsedPause)


            // Anzeige Anfangsdatum und Zeit
            v.editTextBeginDate.text = formattedBeginDate
            v.editTextBeginTime.text = formattedBeginTime
            // Anzeige Endedatum und Zeit
            v.editTextEndDate.text = formattedEndDate
            v.editTextEndTime.text = formattedEndTime
            // Anzeige Pause
            v.editTextPause.text = formattedParsedPause
        }




        v.buttonDelete.setOnClickListener(){
            val builder = AlertDialog.Builder(v.context)
            builder.setTitle("Löschen")
            builder.setMessage("Wollen Sie diesen Eintrag wirklich löschen?")
            builder.setPositiveButton("Ja") { _, _ ->
                Toast.makeText(v.context, "Eintrag wurde gelöscht!", Toast.LENGTH_SHORT).show()
                db.deleteData(id)
                val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragment_container, EditFragment())
                fragmentTransaction.commit()
            }
            builder.setNegativeButton("Nein") { _, _ ->
                Toast.makeText(v.context, "Eintrag nicht gelöscht!", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }


        v.buttonSave.setOnClickListener(){
            if (id != null) {
                saveData(id,parsedBegin,parsedEnd,pauseTime)
                Toast.makeText(context, "Speichern erfolgreich", Toast.LENGTH_SHORT).show()
            }
        }




        v.editTextBeginDate.setOnClickListener{
            val dpd = DatePickerDialog(v.context,DatePickerDialog.OnDateSetListener{view, mYear, mMonth, mDay ->
                val realMonth = mMonth+1

                val fm = if (realMonth<10){
                    "0$realMonth"
                } else {
                    realMonth.toString()
                }
                val fd = if (mDay<10){
                    "0$mDay"
                } else {
                    mDay.toString()
                }
                pickedBeginDate = "$mYear-$fm-$fd"

                val parsedBeginString =  LocalDateTime.parse(pickedBeginDate+'T'+parsedBegin.format(timeFormatter)+ ".001",parseFormatter)
                parsedBegin = parsedBeginString

                v.editTextBeginDate.text = "$fd.$fm.$mYear"

                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                if (id != null) {
                    saveData(id, parsedBegin, parsedEnd, pauseTime)
                }
            }, year,month,day)

            dpd.show()
        }



        v.editTextEndDate.setOnClickListener{
            val dpd = DatePickerDialog(v.context,DatePickerDialog.OnDateSetListener{view, mYear, mMonth, mDay ->
                val realMonth = mMonth+1
                val fm = if (realMonth<10){
                    "0$realMonth"
                } else {
                    realMonth.toString()
                }
                val fd = if (mDay<10){
                    "0$mDay"
                } else {
                    mDay.toString()
                }
                pickedEndDate = "$mYear-$fm-$fd"

                val parsedEndString =  LocalDateTime.parse(pickedEndDate+'T'+parsedEnd.format(timeFormatter)+".001",parseFormatter)
                parsedEnd = parsedEndString
                v.editTextEndDate.text = "$fd.$fm.$mYear"

                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                if (id != null) {
                    saveData(id, parsedBegin, parsedEnd, pauseTime)
                }
            }, year,month,day)

            dpd.show()
        }


        v.editTextBeginTime.setOnClickListener {
            val tsl = TimePickerDialog.OnTimeSetListener(){ view: TimePicker?, hour: Int, minute: Int ->
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)

                val fh = if (hour<10){
                     "0$hour"
                 } else {
                    hour.toString()
                }
                val fm = if (minute<10){
                    "0$minute"
                } else {
                    minute.toString()
                }

                v.editTextBeginTime.text = "$fh:$fm:00"

                val pickedBeginTime =  "$fh:$fm:00.001"
                val parsedBeginString =  LocalDateTime.parse(parsedBegin.format(dateFormatterLocalDate)+'T'+pickedBeginTime,parseFormatter)
                parsedBegin = parsedBeginString

                Log.d(TAG, "parsedBeginStringTP: "  + parsedBeginString+ parsedEnd+ parsedPause)

                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                if (id != null) {
                    saveData(id, parsedBegin, parsedEnd, pauseTime)
                }
            }
            TimePickerDialog(v.context, tsl, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        v.editTextEndTime.setOnClickListener {
            val tsl = TimePickerDialog.OnTimeSetListener(){ view: TimePicker?, hour: Int, minute: Int ->
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)

                val fh = if (hour<10){
                    "0$hour"
                } else {
                    hour.toString()
                }
                val fm = if (minute<10){
                    "0$minute"
                } else {
                    minute.toString()
                }

                v.editTextEndTime.text = "$fh:$fm:00"

                val pickedEndTime =  "$fh:$fm:00.001"
                val parsedEndString =  LocalDateTime.parse(parsedEnd.format(dateFormatterLocalDate)+'T'+pickedEndTime,parseFormatter)
                parsedEnd = parsedEndString
                Log.d(TAG, "parsedBeginStringTP: "  + parsedEndString+ parsedEnd+ parsedPause)

                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                if (id != null) {
                    saveData(id, parsedBegin, parsedEnd, pauseTime)
                }
            }
            TimePickerDialog(v.context, tsl, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }


        v.editTextPause.setOnClickListener {
            val tsl = TimePickerDialog.OnTimeSetListener(){ view: TimePicker?, hour: Int, minute: Int ->
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)

                val fh = if (hour<10){
                    "0$hour"
                } else {
                    hour.toString()
                }
                val fm = if (minute<10){
                    "0$minute"
                } else {
                    minute.toString()
                }

                val pickedPauseTime =  "$fh:$fm:00"
                v.editTextPause.text = pickedPauseTime
                val parsedPickedPauseTime = LocalTime.parse(pickedPauseTime,timeFormatter)
                val parsedPickedPauseTimeMilis = parsedPickedPauseTime.toNanoOfDay()/ 1000000
                parsedPause = Duration.ofMillis(parsedPickedPauseTimeMilis)
                pauseTime = parsedPickedPauseTimeMilis.toString()
                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                if (id != null) saveData(id, parsedBegin, parsedEnd,pauseTime)
            }
            TimePickerDialog(v.context, tsl, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        return v
    }


    private fun saveData(id: String, parsedBegin: LocalDateTime, parsedEnd: LocalDateTime, pauseTime: String){
        val db = DBHandler(v.context)

        val job = v.editTextAcitvity.text.toString()
        val annotation = v.editTextAnnotation.text.toString()

        db.updateData(id,parsedBegin.toString(),parsedEnd.toString(),pauseTime,job,annotation)
    }


    private fun clearTextViews() {
        v.editTextAnnotation.text.clear()
        v.editTextBeginTime.text = ""
        v.editTextEndDate.text = ""
        v.editTextEndTime.text = ""
        v.editTextPause.text = ""
        v.editTextPause.text = ""
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTimeValue(parsedBegin: LocalDateTime, parsedEnd: LocalDateTime, parsedPause: Duration){

        val diff = Duration.between(parsedBegin,parsedEnd)
        val diffMinusPause = diff - parsedPause
        val hours = diffMinusPause.toHours()
        val minutes = ((diffMinusPause.seconds % (60*60)) / 60)

        val fh = if(hours in 0..9){
            "0$hours"
        } else {
            hours.toString()
        }

        val fm = if(minutes in 0..9){
            "0$minutes"
        } else {
            minutes.toString()
        }

        if (fh.toInt()<0 || fm.toInt()<0){
            val builder = AlertDialog.Builder(v.context)
            builder.setTitle("ACHTUNG")
            builder.setMessage("Die berechnete  Arbeitszeit ist negativ! Bitte ändern Sie die angegbenen Daten!")
            builder.setNeutralButton("OK") { dialog, which ->
                Toast.makeText(v.context,
                    "OK", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }
        // Anzeige Arbeitszeit insgesamt
        v.textViewTimeValue.text = "$fh:$fm h"
    }


}
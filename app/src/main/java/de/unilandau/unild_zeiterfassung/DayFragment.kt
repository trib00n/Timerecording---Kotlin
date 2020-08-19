package de.unilandau.unild_zeiterfassung


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.*
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
        Log.d(TAG, "ID=$id")


        // Datenbank lesen
        var db = DBHandler(v.context)
        var data = id?.let { db.readDataById(it) }
        if (data != null) {
            for (i in 0 until data.size) {
                annotation = data.get(i).annotation
                job = data.get(i).job
            }
            v.editTextAcitvity.setText(job)
            v.editTextAnnotation.setText(annotation)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v = inflater.inflate(R.layout.fragment_day, container, false)


        val id = arguments?.getString("id")
        Log.d(TAG, "ID=$id")

        var beginTime :  String = ""
        var endTime :  String = ""
        var pauseTime :  String = ""
        var job : String = ""
        var annotation : String = ""
        var pickedBeginDate = ""
        var pickedEndDate = ""
        var formattedEndTime :  String = ""
        var formattedEndDate :  String = ""

        var formattedBeginTime :  String = ""
        var formattedBeginDate :  String = ""



        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        clearTextViews()

        // Datenbank lesen
        var db = DBHandler(v.context)
        var data = id?.let { db.readDataById(it) }
        if (data != null) {
            for (i in 0 until data.size){
                beginTime = data.get(i).begin
                endTime = data.get(i).end
                pauseTime = data.get(i).pause
                job = data.get(i).job
                annotation = data.get(i).annotation
            }

            Log.d(TAG,"beginTime" + beginTime)
            Log.d(TAG,"endTime" + endTime)
            Log.d(TAG,"pauseTime" + pauseTime)
            Log.d(TAG,"job:" + job)
            Log.d(TAG,"annotation" + annotation)

            parsedBegin = LocalDateTime.parse(beginTime,parseFormatter)
            parsedEnd = LocalDateTime.parse(endTime,parseFormatter)



             v.editTextAcitvity.setText(job)

            v.editTextAnnotation.setText(annotation)

            Log.d(TAG,"parsedBegin" + parsedBegin)
            Log.d(TAG,"parsedEnd" + parsedEnd)
            parsedPause = Duration.ofMillis(pauseTime.toLong())
            var ltPause = LocalTime.ofNanoOfDay( parsedPause.toNanos())
            formattedParsedPause = ltPause.format(timeFormatter)
            Log.d(TAG, "parsedPause"+ parsedPause)
            Log.d(TAG, "PauseNanos"+ ltPause)
            Log.d(TAG, "parsedPause"+ parsedPause)
            Log.d(TAG, "formattedParsedPause"+ formattedParsedPause)
            calculateTimeValue(parsedBegin,parsedEnd,parsedPause)

            // var diffPauseDummy = Duration.between(parsedPause,parsedDummy)
            //    var diff = diffBeginEnd.plus(diffPauseDummy)

            formattedEndTime = parsedEnd.format(timeFormatter)
            formattedEndDate = parsedEnd.format(dateFormatter)

            formattedBeginTime = parsedBegin.format(timeFormatter)
            formattedBeginDate = parsedBegin.format(dateFormatter)

            //var formattedPause = parsedPause.format(timeFormatter)


            //Log.d(TAG, "FormattedPause: " + formattedPause)


            // Anzeige Anfangsdatum und Zeit
            v.editTextBeginDate.setText(formattedBeginDate.toString())
            v.editTextBeginTime.setText(formattedBeginTime.toString())
            // Anzeige Endedatum und Zeit
            v.editTextEndDate.setText(formattedEndDate.toString())
            v.editTextEndTime.setText(formattedEndTime.toString())
            // Anzeige Pause
            v.editTextPause.text = formattedParsedPause
        }




        v.buttonDelete.setOnClickListener(){
            val builder = AlertDialog.Builder(v.context)
            builder.setTitle("Löschen")
            builder.setMessage("Wollen Sie diesen Eintrag wirklich löschen?")
            builder.setPositiveButton("Ja") { dialog, which ->
                Toast.makeText(v.context, "Eintrag wurde gelöscht!", Toast.LENGTH_SHORT).show()
                db.deleteData(id)
                val fragmentTransaction = fragmentManager?.beginTransaction()
                fragmentTransaction?.replace(R.id.fragment_container, WorkFragment())
                fragmentTransaction?.commit()
            }
            builder.setNegativeButton("Nein") { dialog, which ->
                Toast.makeText(v.context, "Eintrag nicht gelöscht!", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }


        v.buttonSave.setOnClickListener(){
            if (id != null) {
                saveData(id,parsedBegin,parsedEnd,pauseTime)
            }
        }




        v.editTextBeginDate.setOnClickListener{
            val dpd = DatePickerDialog(v.context,DatePickerDialog.OnDateSetListener{view, mYear, mMonth, mDay ->
                var realMonth = mMonth+1
                var fm = ""
                var fd = ""
                if (realMonth<10){
                    fm = "0$realMonth"
                } else {
                    fm = realMonth.toString()
                }
                if (mDay<10){
                    fd = "0$mDay"
                } else {
                    fd = mDay.toString()
                }
                pickedBeginDate = "$mYear-$fm-$fd"

                var parsedBeginString =  LocalDateTime.parse(pickedBeginDate+'T'+parsedBegin.format(timeFormatter)+ ".001",parseFormatter)
                parsedBegin = parsedBeginString

                v.editTextBeginDate.text = "$fd.$fm.$mYear"

                   Log.d(TAG, "parsedBeginString: "  + parsedBeginString)
                //  var formattedEndDate = pickedBeginDate.format(dateFormatter)
                  Log.d(TAG, "Bla: "  + parsedBeginString+ parsedEnd+ parsedPause)
                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                if (id != null) {
                    saveData(id, parsedBegin, parsedEnd, pauseTime)
                }
            }, year,month,day)

            dpd.show()
        }



        v.editTextEndDate.setOnClickListener{
            val dpd = DatePickerDialog(v.context,DatePickerDialog.OnDateSetListener{view, mYear, mMonth, mDay ->
                var realMonth = mMonth+1
                var fm = ""
                var fd = ""
                if (realMonth<10){
                    fm = "0$realMonth"
                } else {
                    fm = realMonth.toString()
                }
                if (mDay<10){
                    fd = "0$mDay"
                } else {
                    fd = mDay.toString()
                }
                pickedEndDate = "$mYear-$fm-$fd"

                var parsedEndString =  LocalDateTime.parse(pickedEndDate+'T'+parsedEnd.format(timeFormatter)+".001",parseFormatter)
                parsedEnd = parsedEndString
                v.editTextEndDate.text = "$fd.$fm.$mYear"

                Log.d(TAG, "parsedEndString: "  + parsedEndString)
                //  var formattedEndDate = pickedBeginDate.format(dateFormatter)
                Log.d(TAG, "Bla: "  + parsedEndString+ parsedEnd+ parsedPause)
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

                var fh = if (hour<10){
                     "0$hour"
                 } else {
                    hour.toString()
                }
                var fm = if (minute<10){
                    "0$minute"
                } else {
                    minute.toString()
                }

                v.editTextBeginTime.text = "$fh:$fm:00"

                var pickedBeginTime =  "$fh:$fm:00.001"
                var parsedBeginString =  LocalDateTime.parse(parsedBegin.format(dateFormatterLocalDate)+'T'+pickedBeginTime,parseFormatter)
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

                var fh = if (hour<10){
                    "0$hour"
                } else {
                    hour.toString()
                }
                var fm = if (minute<10){
                    "0$minute"
                } else {
                    minute.toString()
                }

                v.editTextEndTime.text = "$fh:$fm:00"

                var pickedEndTime =  "$fh:$fm:00.001"
                var parsedEndString =  LocalDateTime.parse(parsedEnd.format(dateFormatterLocalDate)+'T'+pickedEndTime,parseFormatter)
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

                var fh = if (hour<10){
                    "0$hour"
                } else {
                    hour.toString()
                }
                var fm = if (minute<10){
                    "0$minute"
                } else {
                    minute.toString()
                }



                var pickedPauseTime =  "$fh:$fm:00"
                v.editTextPause.text = pickedPauseTime
                var parsedPickedPauseTime = LocalTime.parse(pickedPauseTime,timeFormatter)
                var parsedPickedPauseTimeMilis = parsedPickedPauseTime.toNanoOfDay()/ 1000000
                parsedPause = Duration.ofMillis(parsedPickedPauseTimeMilis)
                pauseTime = parsedPickedPauseTimeMilis.toString()

                Log.d(TAG, "pickedPauseTime: "  + pickedPauseTime+ "parsedPickedPauseTime: "  + parsedPickedPauseTime+ "parsedPickedPauseTimeNano: "  + parsedPickedPauseTimeMilis+ "parsedPause: "  + parsedPause)


                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)

                if (id != null) {
                    saveData(id, parsedBegin, parsedEnd,pauseTime)
                }
            }
            TimePickerDialog(v.context, tsl, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }




        return v
    }


    private fun saveData(id: String, parsedBegin: LocalDateTime, parsedEnd: LocalDateTime, pauseTime: String){
        var db = DBHandler(v.context)


        var job = v.editTextAcitvity.text.toString()
        var annotation = v.editTextAnnotation.text.toString()

        Log.d(TAG, "ja man:" + job)

        db.updateData(id,parsedBegin.toString(),parsedEnd.toString(),pauseTime,job,annotation)
    }


    private fun clearTextViews() {
       // v.editTextAcitvity.text.clear()
        v.editTextAnnotation.text.clear()
        v.editTextBeginTime.text = ""
        v.editTextEndDate.text = ""
        v.editTextEndTime.text = ""
        v.editTextPause.text = ""
        v.editTextPause.text = ""
    }

    private fun calculateTimeValue(parsedBegin: LocalDateTime, parsedEnd: LocalDateTime, parsedPause: Duration){

        Log.d(TAG, "parsedBegin: " + parsedBegin + "parsedEnd: " + parsedEnd + "parsedPause: " + parsedPause )
        var diff = Duration.between(parsedBegin,parsedEnd)
        Log.d(TAG, "diff: "+ diff.toString())
        var diffMinusPause = diff - parsedPause
        var hours = diffMinusPause.toHours()
        var minutes = ((diffMinusPause.seconds % (60*60)) / 60)
        var seconds = (diffMinusPause.seconds % 60)
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
/*
        var fs = if(seconds<10 &&  seconds>=0 ){
            "0$seconds"
        } else {
            seconds.toString()
        }
*/
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
        v.textViewTimeValue.text = fh +":"+ fm+" h"
    }


}
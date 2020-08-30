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
        /*Leert die Textviews*/
        clearTextViews()
        /*Übernimmt Argument ID*/
        val id = arguments?.getString("id")
        /*Diese Variabelen werden von verschiednen Funktionen geändert und gelesen*/
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
        /* Wird für Picker benörigt */
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        // Datenbank initial lesen
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
            // Anzeige Tätigkeit und Anmerkung
            v.editTextAcitvity.setText(job)
            v.editTextAnnotation.setText(annotation)
            // Aus Datenbank gelesene Begin und End-Datum werden zu LocalDateTime umgewandelt und formatiert
            parsedBegin = LocalDateTime.parse(beginTime,parseFormatter)
            parsedEnd = LocalDateTime.parse(endTime,parseFormatter)
            formattedBeginTime = parsedBegin.format(timeFormatter)
            formattedBeginDate = parsedBegin.format(dateFormatter)
            formattedEndTime = parsedEnd.format(timeFormatter)
            formattedEndDate = parsedEnd.format(dateFormatter)
            // Aus Datenbank gelesene Pause wird in LocalTime umgewandelt und formatiert
            parsedPause = Duration.ofMillis(pauseTime.toLong())
            val ltPause = LocalTime.ofNanoOfDay( parsedPause.toNanos())
            formattedParsedPause = ltPause.format(timeFormatter)
            // Anzeige Anfangsdatum und Zeit
            v.editTextBeginDate.text = formattedBeginDate
            v.editTextBeginTime.text = formattedBeginTime
            // Anzeige Endedatum und Zeit
            v.editTextEndDate.text = formattedEndDate
            v.editTextEndTime.text = formattedEndTime
            // Anzeige Pause
            v.editTextPause.text = formattedParsedPause
            // Berechnung von Arbeitszeit
            calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
        }

        // Löscht Eintrag aus Datenbank mit vorherriger Abfrage
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

        // Eingetragene Daten werden gelesen und in DB gespeichert
        v.buttonSave.setOnClickListener(){
            if (id != null) {
                saveData(id,parsedBegin,parsedEnd,pauseTime)
                Toast.makeText(context, "Speichern erfolgreich", Toast.LENGTH_SHORT).show()
            }
        }
        // Öffnet DatePicker Anfangsdatum
        v.editTextBeginDate.setOnClickListener{
            val dpd = DatePickerDialog(v.context,DatePickerDialog.OnDateSetListener{ _, mYear, mMonth, mDay ->
                // Ausgewählter Monat muss um 1 ergänzt werden, da der Picker bei 0 änfangt zu zählen
                val realMonth = mMonth+1
                // Hinzufügen von 0 bei Werten zwischen 0-10 da es sonst zu formatierungs Fehlern kommt
                val fm = addLeadingZeros(realMonth.toString())
                val fd = addLeadingZeros(mDay.toString())
                // Ausgwähltes Datum für LocalDateTime formatieren
                pickedBeginDate = "$mYear-$fm-$fd"
                // Anzeige ausgwähltes Datum
                v.editTextBeginDate.text = "$fd.$fm.$mYear"
                // Umwandlung in LocalDateTime
                val parsedBeginString =  LocalDateTime.parse(pickedBeginDate+'T'+parsedBegin.format(timeFormatter)+ ".001",parseFormatter)
                // Setzen von Wert da von anderen Funktionen zugegriffen werden muss
                parsedBegin = parsedBeginString
                // Berechnung von Arbeitszeit
                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                // Daten speichern
                if (id != null) saveData(id, parsedBegin, parsedEnd, pauseTime)
            }, year,month,day)
            dpd.show()
        }


        // Öffnet DatePicker Endedatum
        v.editTextEndDate.setOnClickListener{
            val dpd = DatePickerDialog(v.context,DatePickerDialog.OnDateSetListener{ _, mYear, mMonth, mDay ->
                // Ausgewählter Monat muss um 1 ergänzt werden, da der Picker bei 0 änfangt zu zählen
                val realMonth = mMonth+1
                // Hinzufügen von 0 bei Werten zwischen 0-10 da es sonst zu formatierungs Fehlern kommt
                val fm = addLeadingZeros(realMonth.toString())
                val fd = addLeadingZeros(mDay.toString())
                // Ausgwähltes Datum für LocalDateTime formatieren
                pickedEndDate = "$mYear-$fm-$fd"
                // Anzeige ausgwähltes Datum
                v.editTextEndDate.text = "$fd.$fm.$mYear"
                // Umwandlung in LocalDateTime
                val parsedEndString =  LocalDateTime.parse(pickedEndDate+'T'+parsedEnd.format(timeFormatter)+".001",parseFormatter)
                // Setzen von Wert da von anderen Funktionen zugegriffen werden muss
                parsedEnd = parsedEndString
                // Berechnung von Arbeitszeit
                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                // Daten speichern
                if (id != null) saveData(id, parsedBegin, parsedEnd, pauseTime)
            }, year,month,day)
            dpd.show()
        }

        // Öffnet TimePicker Anfangszeit
        v.editTextBeginTime.setOnClickListener {
            val tsl = TimePickerDialog.OnTimeSetListener(){ _: TimePicker?, hour: Int, minute: Int ->
                // Ausgewählte Stunden und Minuten werden gelesen
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                // Hinzufügen von 0 bei Werten zwischen 0-10 da es sonst zu formatierungs Fehlern kommt
                val fh = addLeadingZeros(hour.toString())
                val fm = addLeadingZeros(minute.toString())
                // Ausgwähltes Zeit für LocalDateTime formatieren
                val pickedBeginTime =  "$fh:$fm:00.001"
                // Anzeige ausgwähltes Zeit
                v.editTextBeginTime.text = "$fh:$fm:00"
                // Umwandlung in LocalDateTime
                val parsedBeginString =  LocalDateTime.parse(parsedBegin.format(dateFormatterLocalDate)+'T'+pickedBeginTime,parseFormatter)
                // Setzen von Wert da von anderen Funktionen zugegriffen werden muss
                parsedBegin = parsedBeginString
                // Berechnung von Arbeitszeit
                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                // Daten speichern
                if (id != null) saveData(id, parsedBegin, parsedEnd, pauseTime)
            }
            TimePickerDialog(v.context, tsl, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
        // Öffnet TimePicker Endzeit
        v.editTextEndTime.setOnClickListener {
            val tsl = TimePickerDialog.OnTimeSetListener(){ _: TimePicker?, hour: Int, minute: Int ->
                // Ausgewählte Stunden und Minuten werden gelesen
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                // Hinzufügen von 0 bei Werten zwischen 0-10 da es sonst zu formatierungs Fehlern kommt
                val fh = addLeadingZeros(hour.toString())
                val fm = addLeadingZeros(minute.toString())
                // Anzeige ausgwähltes Zeit
                v.editTextEndTime.text = "$fh:$fm:00"
                // Ausgwähltes Zeit für LocalDateTime formatieren
                val pickedEndTime =  "$fh:$fm:00.001"
                // Umwandlung in LocalDateTime
                val parsedEndString =  LocalDateTime.parse(parsedEnd.format(dateFormatterLocalDate)+'T'+pickedEndTime,parseFormatter)
                // Setzen von Wert da von anderen Funktionen zugegriffen werden muss
                parsedEnd = parsedEndString
                // Berechnung von Arbeitszeit
                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                // Daten speichern
                if (id != null) saveData(id, parsedBegin, parsedEnd, pauseTime)
            }
            TimePickerDialog(v.context, tsl, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        // Öffnet TimePicker Pausezeit
        v.editTextPause.setOnClickListener {
            val tsl = TimePickerDialog.OnTimeSetListener(){ _: TimePicker?, hour: Int, minute: Int ->
                // Ausgewählte Stunden und Minuten werden gelesen
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                // Hinzufügen von 0 bei Werten zwischen 0-10 da es sonst zu formatierungs Fehlern kommt
                val fh = addLeadingZeros(hour.toString())
                val fm = addLeadingZeros(minute.toString())
                // Ausgwähltes Zeit für LocalDateTime formatieren
                val pickedPauseTime =  "$fh:$fm:00"
                // Anzeige ausgwähltes Zeit
                v.editTextPause.text = pickedPauseTime
                // Umwandlung in LocalTime
                val parsedPickedPauseTime = LocalTime.parse(pickedPauseTime,timeFormatter)
                // Wird benötigt um Dauer richtig zu berechnen
                val parsedPickedPauseTimeMilis = parsedPickedPauseTime.toNanoOfDay()/ 1000000
                // Umwandlung in Dauer
                parsedPause = Duration.ofMillis(parsedPickedPauseTimeMilis)
                // Setzen von Wert da von anderen Funktionen zugegriffen werden muss
                pauseTime = parsedPickedPauseTimeMilis.toString()
                // Berechnung von Arbeitszeit
                calculateTimeValue(parsedBegin,parsedEnd,parsedPause)
                // Daten speichern
                if (id != null) saveData(id, parsedBegin, parsedEnd,pauseTime)
            }
            TimePickerDialog(v.context, tsl, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        return v
    }

    /*Speichert Daten in Datenbank */
    private fun saveData(id: String, parsedBegin: LocalDateTime, parsedEnd: LocalDateTime, pauseTime: String){
        val db = DBHandler(v.context)

        val job = v.editTextAcitvity.text.toString()
        val annotation = v.editTextAnnotation.text.toString()

        db.updateData(id,parsedBegin.toString(),parsedEnd.toString(),pauseTime,job,annotation)
    }

    /*Setzt Anzeigen zurück*/
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
        // Dauer zwischen Anfangszeit und Endzeit berechnen
        val diff = Duration.between(parsedBegin,parsedEnd)
        // Pausedauer von Arbeitszeit abziehen
        val diffMinusPause = diff - parsedPause
        // Stunde und Minute aus Duration berechnen
        val hour = diffMinusPause.toHours()
        val minute = ((diffMinusPause.seconds % (60*60)) / 60)
        // Hinzufügen von 0 bei Werten zwischen 0-10 da es sonst zu formatierungs Fehlern kommt
        val fh = addLeadingZeros(hour.toString())
        val fm = addLeadingZeros(minute.toString())
        // Wenn negative Zahlen wird eine Warnung ausgegeben
        if (fh.toInt()<0 || fm.toInt()<0){
            val builder = AlertDialog.Builder(v.context)

            builder.setTitle("ACHTUNG")
            builder.setMessage("Die berechnete  Arbeitszeit ist negativ! Bitte ändern Sie die angegbenen Daten!")
            builder.setNeutralButton("OK") { _, _ ->
                Toast.makeText(v.context,
                    "OK", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }
        // Anzeige Arbeitszeit insgesamt
        v.textViewTimeValue.text = "$fh:$fm h"
    }

    /* Hinzufügen von fehlenden Nullen */
    private fun addLeadingZeros(str: String) : String {
        return if (str.toInt() in 0..9) "0$str" else str
    }

}
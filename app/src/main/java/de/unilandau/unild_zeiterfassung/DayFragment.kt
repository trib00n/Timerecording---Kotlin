package de.unilandau.unild_zeiterfassung


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_day.view.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DayFragment : Fragment() {
    lateinit var v: View

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v = inflater.inflate(R.layout.fragment_day, container, false)

        v.editText.text=""
        var beginTime :  String = ""

        var db = DBHandler(v.context)
        var data = db.readAllData()


        for (i in 0 until data.size){

             beginTime = data.get(i).begin
        }

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")


        var parsedDate = LocalDateTime.parse(beginTime,formatter)

        Log.d("MeinLog:", parsedDate.toString())

        v.editText.append("${parsedDate.dayOfMonth.toString()}.${parsedDate.month.toString()}.${parsedDate.year.toString()}")

        v.buttonDelete.setOnClickListener(){
            db.deleteData(2)
        }

        return v
    }


}
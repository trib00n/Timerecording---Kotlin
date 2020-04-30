package de.unilandau.unild_zeiterfassung


import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_day.view.*

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

        var db = DBHandler(v.context)
        var data = db.readAllData()
        for (i in 0..(data.size-1)){
            v.editText.append(data.get(i).id.toString() + data.get(i).date.toString() +  data.get(i).begin.toString() + data.get(i).end.toString() + data.get(i).pause.toString() + "\n")
        }

        v.buttonDelete.setOnClickListener(){
            db.deleteData(2)
        }

        return v
    }


}
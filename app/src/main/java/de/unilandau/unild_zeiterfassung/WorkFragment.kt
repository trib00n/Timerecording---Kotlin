package de.unilandau.unild_zeiterfassung

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class WorkFragment : Fragment() {
    lateinit var v: View
    var TAG = "WorkFragment"
    var parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    var dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dayFragment = DayFragment()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_work, container, false)
        var listview = v.findViewById<ListView>(R.id.listView)

        var list = mutableListOf<Model>()

        var db = DBHandler(v.context)
        var data = db.readAllData()

        var dataId : String
        var arraylist=ArrayList<String>()
        for (i in 0 until data.size){

            dataId = data.get(i).id.toString()
            arraylist.add(dataId)
            var beginTime = data.get(i).begin
            var endTime = data.get(i).end
            var pauseTime = data.get(i).pause
            var job = data.get(i).job
            var annotation = data.get(i).annotation

            var parsedBegin = LocalDateTime.parse(beginTime,parseFormatter)
            var formattedBeginDate = parsedBegin.format(dateFormatter)

            Log.d(TAG, "ID Lesen:" + dataId)

            list.add(Model(job, "Anfangsdatum: " + formattedBeginDate, R.drawable.ic_delete ))

            listview.setOnItemClickListener{
                    _:AdapterView<*>, v, position, id ->

                val args = Bundle()
                Log.d(TAG, "ID Lesen:" + dataId + "arraylist:" + arraylist.get(position) + "position: " + position + "id: " + id)
                args.putString("id", arraylist.get(position))
                dayFragment.arguments = args

                val fragmentTransaction = fragmentManager?.beginTransaction()
                fragmentTransaction?.replace(R.id.fragment_container, dayFragment)
                fragmentTransaction?.commit()

            }


        }




        listview.adapter = Adapter(v.context, R.layout.row_listview, list )


        Log.d("MeinLog", "Hier")


        return v
    }



}
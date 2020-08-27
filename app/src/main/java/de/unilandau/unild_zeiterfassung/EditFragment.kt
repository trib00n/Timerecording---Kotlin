package de.unilandau.unild_zeiterfassung

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.ListView
import androidx.fragment.app.Fragment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class EditFragment : Fragment() {
    lateinit var v: View
    var parseFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dayFragment = DayFragment()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_work, container, false)
        val listview = v.findViewById<ListView>(R.id.listView)

        val list = mutableListOf<Model>()

        val db = DBHandler(v.context)
        val data = db.readAllData()

        var dataId : String
        val al=ArrayList<String>()
        for (i in 0 until data.size){

            dataId = data.get(i).id.toString()
            al.add(dataId)
            val beginTime = data.get(i).begin
            val job = data.get(i).job
            val parsedBegin = LocalDateTime.parse(beginTime,parseFormatter)
            val formattedBeginDate = parsedBegin.format(dateFormatter)

            list.add(Model(job, "Anfangsdatum: $formattedBeginDate"))
            listview.setOnItemClickListener{
                    _:AdapterView<*>, _, position, _ ->

                val args = Bundle()
                args.putString("id", al[position])
                dayFragment.arguments = args

                val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragment_container, dayFragment)
                fragmentTransaction.commit()

            }
            listview.adapter = Adapter(v.context, R.layout.row_listview, list )

        }

        return v
    }



}
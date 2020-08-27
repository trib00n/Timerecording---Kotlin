package de.unilandau.unild_zeiterfassung

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class Adapter (var mCtx: Context, var resources:Int, var items:List<Model>): ArrayAdapter<Model>(mCtx,resources,items){
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater:LayoutInflater = LayoutInflater.from(mCtx)
        val view:View = layoutInflater.inflate(resources,null)
        val titleTextView:TextView = view.findViewById(R.id.textView1)
        val descriptionTextView:TextView = view.findViewById(R.id.textView2)
        val mItem:Model = items[position]
        titleTextView.text = mItem.title
        descriptionTextView.text=  mItem.description
        return view
    }
}
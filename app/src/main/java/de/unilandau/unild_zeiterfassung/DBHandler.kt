package de.unilandau.unild_zeiterfassung

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.sql.Time

val DATABASE_NAME = "TimeRecordSystem"
val TABLE_NAME = "TimeRecord"
val COL_ID = "id"
val COL_BEGIN = "begin"
val COL_END = "end"
val COL_PAUSE = "pause"
private val TAG = "MeinLog:"

class DBHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {


        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_BEGIN + " STRING," +
                COL_END + " DATETIME," +
                COL_PAUSE + " TIME)"

        Log.i(TAG, createTable)
        Log.i(TAG, "Tabelle erstellt")
        db?.execSQL(createTable)


    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun insertData(TimeRecording: TimeRecording) {
        val db = this.writableDatabase
        var cv = ContentValues()
        cv.put(COL_BEGIN, TimeRecording.begin)
        cv.put(COL_END, TimeRecording.end)
        cv.put(COL_PAUSE, TimeRecording.pause)
        var result = db.insert(TABLE_NAME, null, cv)
        if (result == -1.toLong()) {
            Toast.makeText(context, "Fehler beim Speichern", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Speichern erfolgreich", Toast.LENGTH_SHORT).show()
        }
    }

    fun readAllData(): MutableList<TimeRecording> {
        var list: MutableList<TimeRecording> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from " + TABLE_NAME
        val result = db.rawQuery(query, null)
        Log.d("MeinLog:", "Lesen")

        if (result.moveToFirst()) {
            do {
                var timeRecording = TimeRecording()
                timeRecording.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                timeRecording.begin = result.getString(result.getColumnIndex(COL_BEGIN))
                Log.d("MeinLog:", timeRecording.begin)
                timeRecording.end = result.getString(result.getColumnIndex(COL_END))
                timeRecording.pause = result.getString(result.getColumnIndex(COL_PAUSE))
                list.add(timeRecording)
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }

    fun updateData() {

        val db = this.writableDatabase
        val query = "Select * from " + TABLE_NAME
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                var cv = ContentValues()
                cv.put(COL_BEGIN,result.getInt(result.getColumnIndex(COL_BEGIN))+1)
          //      db.update(TABLE_NAME,cv, COL_ID + "=? AND" + COL_DATE + "+?", arrayOf(result.getString(result.getColumnIndex(COL_ID)), result.getString(result.getColumnIndex(
          //          COL_DATE))))
            } while (result.moveToNext())
        }
        result.close()
        db.close()

    }


    fun deleteData(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, COL_ID+"=?", arrayOf(id.toString()))
        db.close()

    }
}


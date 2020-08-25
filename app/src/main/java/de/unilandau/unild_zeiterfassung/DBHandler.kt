package de.unilandau.unild_zeiterfassung

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.sql.Time
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val DATABASE_NAME = "TimeRecordSystem"
val TABLE_NAME = "TimeRecord"
val COL_ID = "id"
val COL_BEGIN = "begin"
val COL_END = "end"
val COL_PAUSE = "pause"
val COL_JOB = "job"
val COL_ANNO = "annotation"
private val TAG = "DBHandler"

class DBHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {


        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_BEGIN + " DATETIME," +
                COL_END + " DATETIME," +
                COL_PAUSE + " DATETIME," +
                COL_JOB + " STRING,"+
                COL_ANNO + " STRING)"

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
        cv.put(COL_JOB, TimeRecording.job)

        Log.d(TAG, "TimeRecording.job: " + TimeRecording.job)
        Log.d(TAG, " TimeRecording.annotation: " +  TimeRecording.annotation)
        cv.put(COL_ANNO, TimeRecording.annotation)
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
                timeRecording.job = result.getString(result.getColumnIndex(COL_JOB))
                timeRecording.annotation = result.getString(result.getColumnIndex(COL_ANNO))
                list.add(timeRecording)
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }

    fun readLastData(): MutableList<TimeRecording> {
        var list: MutableList<TimeRecording> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from " + TABLE_NAME
        val result = db.rawQuery(query, null)
        Log.d("MeinLog:", "Lesen")

        if (result.moveToLast()) {
            do {
                var timeRecording = TimeRecording()
                timeRecording.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                timeRecording.begin = result.getString(result.getColumnIndex(COL_BEGIN))
                Log.d("MeinLog:", timeRecording.begin)
                timeRecording.end = result.getString(result.getColumnIndex(COL_END))
                timeRecording.pause = result.getString(result.getColumnIndex(COL_PAUSE))
                timeRecording.job = result.getString(result.getColumnIndex(COL_JOB))
                timeRecording.annotation = result.getString(result.getColumnIndex(COL_ANNO))
                list.add(timeRecording)
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }



    fun readDataById(id: String): MutableList<TimeRecording> {
        var list: MutableList<TimeRecording> = ArrayList()
        val db = this.readableDatabase

        Log.d(TAG, "")

        val query = "SELECT * FROM $TABLE_NAME WHERE id=$id"
        val result = db.rawQuery(query, null)
        Log.d("MeinLog", query.toString())

     if (result.moveToFirst()) {
         do {
             var timeRecording = TimeRecording()
               timeRecording.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                timeRecording.begin = result.getString(result.getColumnIndex(COL_BEGIN))
                Log.d("MeinLog:", timeRecording.begin)
                timeRecording.end = result.getString(result.getColumnIndex(COL_END))
                timeRecording.pause = result.getString(result.getColumnIndex(COL_PAUSE))
                timeRecording.job = result.getString(result.getColumnIndex(COL_JOB))
                timeRecording.annotation = result.getString(result.getColumnIndex(COL_ANNO))
                list.add(timeRecording)
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }


    fun readDataByDate(begin: String, end: String): MutableList<TimeRecording> {
        var list: MutableList<TimeRecording> = ArrayList()
        var parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        val db = this.readableDatabase
        var beginTime = LocalDateTime.parse(begin+"T00:00:00.000",parseFormatter)
        var endTime = LocalDateTime.parse(end+"T23:59:59.999",parseFormatter)
        // val query = "SELECT * FROM $TABLE_NAME WHERE DATE(substr(begin,1,4) ||substr(begin ,6,2)||substr(begin ,9,2)) BETWEEN  DATE(20200821) AND DATE(20200821)"
        val query = "SELECT * FROM $TABLE_NAME "
        Log.d("readDataByDate", query.toString())
        val result = db.rawQuery(query, null)
        Log.d("readDataByDateResult", result.toString())

        if (result.moveToFirst()) {
            do {
                var beginStr = result.getString(result.getColumnIndex(COL_BEGIN))
                var Time = LocalDateTime.parse(beginStr,parseFormatter)


                if ((Time.isAfter(beginTime) || Time.isEqual(beginTime)) && (Time.isBefore(endTime) || Time.isEqual(endTime))) {
                    var timeRecording = TimeRecording()
                    timeRecording.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                    timeRecording.begin = result.getString(result.getColumnIndex(COL_BEGIN))
                    Log.d("readDataByDate:", "Ja")
                    Log.d("readDataByDate:", timeRecording.begin)
                    timeRecording.end = result.getString(result.getColumnIndex(COL_END))
                    timeRecording.pause = result.getString(result.getColumnIndex(COL_PAUSE))
                    timeRecording.job = result.getString(result.getColumnIndex(COL_JOB))
                    timeRecording.annotation = result.getString(result.getColumnIndex(COL_ANNO))
                    list.add(timeRecording)
                }

            } while (result.moveToNext())
        }
        Log.d("readDataByDate:","Nein")
        result.close()
        db.close()
        return list
    }


    fun updateData(id: String, begin: String, end: String, pause: String, job: String, annotation: String): Int {

/*
        val COL_ID = "id"
        val COL_BEGIN = "begin"
        val COL_END = "end"
        val COL_PAUSE = "pause"
 */

        Log.d(TAG, "updateData!")
        Log.d(TAG, "job: "+ job)
        val cv = ContentValues()
        cv.put(COL_BEGIN, begin)
        cv.put(COL_END, end)
        cv.put(COL_PAUSE, pause)
        cv.put(COL_JOB, job)
        cv.put(COL_ANNO, annotation)
        val whereclause = "$COL_ID=?"
        return this.writableDatabase.update(TABLE_NAME, cv, whereclause, arrayOf(id))

    }


    fun deleteData(id: String?) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, COL_ID+"=?", arrayOf(id.toString()))
        db.close()

    }
}


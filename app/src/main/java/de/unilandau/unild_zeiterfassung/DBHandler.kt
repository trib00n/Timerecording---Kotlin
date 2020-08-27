package de.unilandau.unild_zeiterfassung

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val DATABASE_NAME = "TimeRecordSystem"
const val TABLE_NAME = "TimeRecord"
const val COL_ID = "id"
const val COL_BEGIN = "begin"
const val COL_END = "end"
const val COL_PAUSE = "pause"
const val COL_JOB = "job"
val COL_ANNO = "annotation"

class DBHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    @SuppressLint("SQLiteString")
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_BEGIN + " DATETIME," +
                COL_END + " DATETIME," +
                COL_PAUSE + " DATETIME," +
                COL_JOB + " STRING,"+
                COL_ANNO + " STRING)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) = Unit

    fun insertData(TimeRecording: TimeRecording) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_BEGIN, TimeRecording.begin)
        cv.put(COL_END, TimeRecording.end)
        cv.put(COL_PAUSE, TimeRecording.pause)
        cv.put(COL_JOB, TimeRecording.job)
        cv.put(COL_ANNO, TimeRecording.annotation)

        val result = db.insert(TABLE_NAME, null, cv)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Fehler beim Speichern", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Speichern erfolgreich", Toast.LENGTH_SHORT).show()
        }
    }

    fun readAllData(): MutableList<TimeRecording> {
        val list: MutableList<TimeRecording> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $TABLE_NAME"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val timeRecording = TimeRecording()
                timeRecording.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                timeRecording.begin = result.getString(result.getColumnIndex(COL_BEGIN))
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
        val list: MutableList<TimeRecording> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $TABLE_NAME"
        val result = db.rawQuery(query, null)
        if (result.moveToLast()) {
            do {
                val timeRecording = TimeRecording()
                timeRecording.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                timeRecording.begin = result.getString(result.getColumnIndex(COL_BEGIN))
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
        val list: MutableList<TimeRecording> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE id=$id"
        val result = db.rawQuery(query, null)
     if (result.moveToFirst()) {
         do {
             val timeRecording = TimeRecording()
             timeRecording.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
             timeRecording.begin = result.getString(result.getColumnIndex(COL_BEGIN))
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
        val list: MutableList<TimeRecording> = ArrayList()
        val parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val db = this.readableDatabase
        val beginTime = LocalDateTime.parse(begin+"T00:00:00.000",parseFormatter)
        val endTime = LocalDateTime.parse(end+"T23:59:59.999",parseFormatter)
        val query = "SELECT * FROM $TABLE_NAME "
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val beginStr = result.getString(result.getColumnIndex(COL_BEGIN))
                val time = LocalDateTime.parse(beginStr,parseFormatter)
                if ((time.isAfter(beginTime) || time.isEqual(beginTime)) && (time.isBefore(endTime) || time.isEqual(endTime))) {
                    val timeRecording = TimeRecording()
                    timeRecording.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                    timeRecording.begin = result.getString(result.getColumnIndex(COL_BEGIN))
                    timeRecording.end = result.getString(result.getColumnIndex(COL_END))
                    timeRecording.pause = result.getString(result.getColumnIndex(COL_PAUSE))
                    timeRecording.job = result.getString(result.getColumnIndex(COL_JOB))
                    timeRecording.annotation = result.getString(result.getColumnIndex(COL_ANNO))
                    list.add(timeRecording)
                }
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }


    fun updateData(id: String, begin: String, end: String, pause: String, job: String, annotation: String): Int {
        val cv = ContentValues()
        cv.put(COL_BEGIN, begin)
        cv.put(COL_END, end)
        cv.put(COL_PAUSE, pause)
        cv.put(COL_JOB, job)
        cv.put(COL_ANNO, annotation)
        val whereClause = "$COL_ID=?"
        return this.writableDatabase.update(TABLE_NAME, cv, whereClause, arrayOf(id))
    }


    fun deleteData(id: String?) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_ID=?", arrayOf(id.toString()))
        db.close()
    }
}


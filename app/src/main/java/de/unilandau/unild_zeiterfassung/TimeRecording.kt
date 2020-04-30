package de.unilandau.unild_zeiterfassung

class TimeRecording{
    var id : Int = 0
    var date : Int = 0
    var begin : Int = 0
    var end : Int = 0
    var pause : Int = 0

    constructor(date:Int,begin:Int,end:Int,pause:Int){
        this.date = date
        this.begin = begin
        this.end = end
        this.pause = pause
    }
    constructor(){
    }


}
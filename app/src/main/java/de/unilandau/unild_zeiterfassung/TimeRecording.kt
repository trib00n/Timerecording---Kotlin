package de.unilandau.unild_zeiterfassung

class TimeRecording{
    var id : Int = 0
    var begin : String = ""
    var end : String = ""
    var pause : String = ""

    constructor(begin:String,end:String,pause:String){
        this.begin = begin
        this.end = end
        this.pause = pause
    }
    constructor(){
    }


}
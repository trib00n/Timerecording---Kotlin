package de.unilandau.unild_zeiterfassung

class TimeRecording{
    var id : Int = 0
    var begin : String = ""
    var end : String = ""
    var pause : String = ""
    var job : String = ""
    var annotation : String = ""

    constructor(begin:String,end:String,pause:String,job : String,annotation : String){
        this.begin = begin
        this.end = end
        this.pause = pause
        this.job = job
        this.annotation = annotation
    }
    constructor(){
    }


}
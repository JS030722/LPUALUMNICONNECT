package com.example.lpualumniconnect.datamodal

class Event {

    var uid: String?=null
    var photo: String?=null
    var description: String?=null
    var date: String?=null
    var time: String?=null

    constructor(){}

    constructor(uid: String?,photo: String?, description:String?, date: String?, time: String? )
    {
        this.uid = uid
        this.photo=photo
        this.description = description
        this.date = date
        this.time = time
    }
}



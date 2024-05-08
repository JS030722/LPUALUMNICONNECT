package com.example.lpualumniconnect.datamodal

import android.health.connect.datatypes.ExerciseRoute.Location

class MessageUserCover {

    var name: String? = null
    var uid: String? = null
    var bio: String ?=null
    var location: String? = null
    var photo: String? = null

    constructor(){}

    constructor(name:String?,uid: String?, bio: String?, location: String?,photo: String?){
        this.name = name
        this.uid = uid
        this.bio = bio
        this.location = location
        this.photo = photo


    }
}
package com.example.lpualumniconnect.datamodal

class User {

    var name: String? = null
    var email: String? = null
    var uid: String? = null
    var photo:String?=null
    var position: String? = null
    var bio: String? = null
    var location: String? = null
    var description: String? = null
    var skill1: String? = null
    var skill2: String? = null
    var skill3: String? = null
    var school: String? = null
    var batch: String? = null
    var github: String? = null
    var linkedIn: String? = null
    var company: String?=null
    var fcmToken : String?=null




    constructor() {}

    constructor(
        name: String?,
        email: String?,
        uid: String?,
        photo: String?,
        position: String?,
        bio: String?,
        location: String?,
        description: String?,
        skill1: String?,
        skill2: String?,
        skill3: String?,
        school: String?,
        batch: String?,
        github: String?,
        linkedIn: String?,
        company: String?,
        fcmToken: String?
    ) {
        this.name = name
        this.email = email
        this.uid = uid
        this.photo = photo
        this.position = position
        this.bio = bio
        this.location = location
        this.description = description
        this.skill1 = skill1
        this.skill2 = skill2
        this.skill3 = skill3
        this.school = school
        this.batch = batch
        this.github = github
        this.linkedIn = linkedIn
        this.company = company
        this.fcmToken = fcmToken

    }
}

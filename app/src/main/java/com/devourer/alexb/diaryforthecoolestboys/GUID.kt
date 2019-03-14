package com.devourer.alexb.diaryforthecoolestboys

import io.realm.RealmObject

open class GUID() : RealmObject() {

    var guid: String = ""

    constructor(_guid: String) : this(){
        guid = _guid
    }

}
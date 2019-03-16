package com.devourer.alexb.diaryforthecoolestboys

import android.os.Parcelable
import io.realm.RealmObject
import kotlinx.android.parcel.Parcelize

@Parcelize
open class TaskList() : RealmObject(), Parcelable{

    var nameOfTaskList: String = ""
    var taskListId: Long = -1

    constructor(_nameOfTaskList: String, _taskListId: Long) : this(){
        nameOfTaskList = _nameOfTaskList
        taskListId = _taskListId
    }
}
package com.devourer.alexb.diaryforthecoolestboys

import io.realm.RealmObject

open class TaskList() : RealmObject(){

    var nameOfTaskList: String = ""
    var taskListId: Long = -1

    constructor(_nameOfTaskList: String, _taskListId: Long) : this(){
        nameOfTaskList = _nameOfTaskList
        taskListId = _taskListId
    }
}
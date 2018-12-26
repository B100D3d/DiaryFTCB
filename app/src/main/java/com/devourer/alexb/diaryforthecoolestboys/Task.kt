package com.devourer.alexb.diaryforthecoolestboys

import com.google.firebase.Timestamp
import java.util.*

class Task {

    var taskText: String
    var taskDetailsText: String
    var dateOfTasks: Any?
    var notificationDateOfTask: Any?
    var id: String = ""
    var map: Map<String,Any?> = mapOf()

    constructor(_taskText: String,
                _taskDetailsText: String,
                _dateOfTasks: Any?,
                _notificationDateOfTask: Any?,
                _id: String){

        taskText = _taskText
        taskDetailsText = _taskDetailsText
        dateOfTasks = _dateOfTasks
        notificationDateOfTask = _notificationDateOfTask
        id = _id
        map = mapOf(
        "text" to taskText,
        "details_text" to taskDetailsText,
        "date" to dateOfTasks,
        "notification_date" to notificationDateOfTask,
        "key" to false
        )
    }
    constructor(_taskText: String,
                _taskDetailsText: String,
                _dateOfTasks: Any?,
                _notificationDateOfTask: Any?){

        taskText = _taskText
        taskDetailsText = _taskDetailsText
        dateOfTasks = _dateOfTasks
        notificationDateOfTask = _notificationDateOfTask
        map = mapOf(
            "text" to taskText,
            "details_text" to taskDetailsText,
            "date" to dateOfTasks,
            "notification_date" to notificationDateOfTask,
            "key" to false
        )
    }

    constructor(_map: Map<String,Any?>){
        taskText = _map["text"] as String
        taskDetailsText = _map["details_text"] as String
        dateOfTasks = _map["date"]
        notificationDateOfTask = _map["notification_date"]
        map = _map
    }


}
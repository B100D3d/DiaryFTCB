package com.devourer.alexb.diaryforthecoolestboys

import io.realm.RealmObject
import java.util.*


open class Task() : RealmObject() {

    var taskText: String? = ""
    var taskDetailsText: String? = ""
    var listTitle: String? = ""
    var dateOfTasks: Date? = null
    var notificationDateOfTask: Date? = null
    var id: String? = ""

    constructor(_taskText: String?,
                _taskDetailsText: String?,
                _dateOfTasks: Any?,
                _notificationDateOfTask: Any?,
                _id: String?,
                _title: String?) : this() {

        taskText = _taskText
        taskDetailsText = _taskDetailsText
        dateOfTasks = _dateOfTasks as Date
        notificationDateOfTask = if(_notificationDateOfTask!=null) _notificationDateOfTask as Date else null
        id = _id
        listTitle = _title
    }

    constructor(task: Task) : this(){
        taskText = task.taskText
        taskDetailsText = task.taskDetailsText
        dateOfTasks = task.dateOfTasks
        notificationDateOfTask = task.notificationDateOfTask
        id = task.id
        listTitle = task.listTitle
    }

    constructor(completedTask: CompletedTask) : this(){
        taskText = completedTask.completedTaskText
        taskDetailsText = completedTask.completedTaskDetailsText
        dateOfTasks = completedTask.dateOfCompletedTask
        notificationDateOfTask = completedTask.notificationDateOfCompletedTask
        id = completedTask.id
        listTitle = completedTask.listTitle
    }

    constructor(_map: Map<String,Any?>) : this() {
        taskText = _map["text"] as String?
        taskDetailsText = _map["details_text"] as String?
        dateOfTasks = _map["date"] as Date
        notificationDateOfTask = if(_map["notification_date"] != null) _map["notification_date"] as Date else null
    }

    fun map(): Map<String,Any?>{
        return mapOf(
            "text" to taskText,
            "details_text" to taskDetailsText,
            "date" to dateOfTasks,
            "notification_date" to notificationDateOfTask,
            "key" to false
        )
    }

}
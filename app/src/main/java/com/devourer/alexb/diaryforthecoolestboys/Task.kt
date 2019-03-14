package com.devourer.alexb.diaryforthecoolestboys

import com.google.firebase.Timestamp
import io.realm.RealmObject
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class Task() : RealmObject(), Parcelable {

    var taskText: String = ""
    var taskDetailsText: String = ""
    var listTitle: String? = ""
    var dateOfTasks: Date? = null
    var notificationDateOfTask: Date? = null
    var id: String? = ""
    var notificationId: Int? = null

    constructor(_taskText: String,
                _taskDetailsText: String,
                _dateOfTasks: Any?,
                _notificationDateOfTask: Any?,
                _id: String?,
                _title: String?,
                _notificationId: Int? = null) : this() {

        taskText = _taskText
        taskDetailsText = _taskDetailsText
        dateOfTasks = (_dateOfTasks as Timestamp).toDate()
        notificationDateOfTask = if(_notificationDateOfTask!=null) (_notificationDateOfTask as Timestamp).toDate() else null
        id = _id
        listTitle = _title
        notificationId = _notificationId
    }

    constructor(_taskText: String,
                _taskDetailsText: String,
                _dateOfTasks: Date?,
                _notificationDateOfTask: Date?,
                _id: String?,
                _title: String?,
                _notificationId: Int? = null) : this(){
        taskText = _taskText
        taskDetailsText = _taskDetailsText
        dateOfTasks = _dateOfTasks
        notificationDateOfTask = _notificationDateOfTask
        id = _id
        listTitle = _title
        notificationId = _notificationId
    }

    constructor(task: Task) : this(){
        taskText = task.taskText
        taskDetailsText = task.taskDetailsText
        dateOfTasks = task.dateOfTasks
        notificationDateOfTask = task.notificationDateOfTask
        id = task.id
        listTitle = task.listTitle
        notificationId = task.notificationId
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
        taskText = _map["text"] as String
        taskDetailsText = _map["details_text"] as String
        dateOfTasks = _map["date"] as Date
        notificationDateOfTask = if(_map["notification_date"] != null) _map["notification_date"] as Date else null
        notificationId = _map["notification_id"] as Int?
    }

    fun map(): Map<String,Any?>{
        return mapOf(
            "text" to taskText,
            "details_text" to taskDetailsText,
            "date" to dateOfTasks,
            "notification_date" to notificationDateOfTask,
            "key" to false,
            "notification_id" to notificationId
        )
    }

}
package com.devourer.alexb.diaryforthecoolestboys

import com.google.firebase.Timestamp
import io.realm.RealmObject
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class CompletedTask() : RealmObject(), Parcelable {

    var completedTaskText: String = ""
    var completedTaskDetailsText: String = ""
    var listTitle: String? = ""
    var dateOfCompletedTask: Date? = null
    var completionDateOfCompletedTask: Date? = null
    var notificationDateOfCompletedTask: Date? = null
    var id: String? = ""

    constructor(_completedTaskText: String,
                _completedTaskDetailsText: String,
                _dateOfCompletedTask: Any?,
                _completionDateOfCompletedTask: Any?,
                _notificationDateOfCompletedTask: Any?,
                _id: String?,
                _title: String?) : this() {

        completedTaskText = _completedTaskText
        completedTaskDetailsText = _completedTaskDetailsText
        dateOfCompletedTask = (_dateOfCompletedTask as Timestamp).toDate()
        completionDateOfCompletedTask = (_completionDateOfCompletedTask as Timestamp).toDate()
        notificationDateOfCompletedTask = if(_notificationDateOfCompletedTask != null) (_notificationDateOfCompletedTask as Timestamp).toDate() else null
        id = _id
        listTitle = _title
    }

    constructor(_completedTaskText: String,
                _completedTaskDetailsText: String,
                _dateOfCompletedTask: Date?,
                _completionDateOfCompletedTask: Date?,
                _notificationDateOfCompletedTask: Date?,
                _id: String?,
                _title: String?) : this(){
        completedTaskText = _completedTaskText
        completedTaskDetailsText = _completedTaskDetailsText
        dateOfCompletedTask = _dateOfCompletedTask
        completionDateOfCompletedTask = _completionDateOfCompletedTask
        notificationDateOfCompletedTask = _notificationDateOfCompletedTask
        id = _id
        listTitle = _title
    }

    constructor(_map: Map<String,Any?>) : this() {
        completedTaskText = _map["text"] as String
        completedTaskDetailsText = _map["details_text"] as String
        dateOfCompletedTask = _map["date"] as Date
        completionDateOfCompletedTask = _map["completion_date"] as Date
        notificationDateOfCompletedTask = if(_map["notification_date"] != null) _map["notification_date"] as Date else null
    }

    constructor(completedTask: CompletedTask) : this() {
        completedTaskText = completedTask.completedTaskText
        completedTaskDetailsText = completedTask.completedTaskDetailsText
        dateOfCompletedTask = completedTask.dateOfCompletedTask
        completionDateOfCompletedTask = completedTask.completionDateOfCompletedTask
        notificationDateOfCompletedTask = completedTask.notificationDateOfCompletedTask
        id = completedTask.id
        listTitle = completedTask.listTitle
    }

    constructor(task: Task, completionDate: Date) : this(){
        completedTaskText = task.taskText
        completedTaskDetailsText = task.taskDetailsText
        dateOfCompletedTask = task.dateOfTasks
        completionDateOfCompletedTask = completionDate
        notificationDateOfCompletedTask = task.notificationDateOfTask
        id = task.id
        listTitle = task.listTitle
    }

    fun map(): Map<String,Any?>{
        return mapOf(
            "text" to completedTaskText,
            "details_text" to completedTaskDetailsText,
            "date" to dateOfCompletedTask,
            "completion_date" to completionDateOfCompletedTask,
            "notification_date" to notificationDateOfCompletedTask,
            "key" to true
        )
    }

}
package com.devourer.alexb.diaryforthecoolestboys

class CompletedTask {

    var completedTaskText: String
    var completedTaskDetailsText: String
    var dateOfCompletedTask: Any?
    var completionDateOfCompletedTask: Any?
    var notificationDateOfCompletedTask: Any?
    var id: String = ""
    var map: Map<String,Any?> = mapOf()

    constructor(_completedTaskText: String,
                _completedTaskDetailsText: String,
                _dateOfCompletedTask: Any?,
                _completionDateOfCompletedTask: Any?,
                _notificationDateOfCompletedTask: Any?,
                _id: String){

        completedTaskText = _completedTaskText
        completedTaskDetailsText = _completedTaskDetailsText
        dateOfCompletedTask = _dateOfCompletedTask
        completionDateOfCompletedTask = _completionDateOfCompletedTask
        notificationDateOfCompletedTask = _notificationDateOfCompletedTask
        id = _id
        map = mapOf(
            "text" to completedTaskText,
            "details_text" to completedTaskDetailsText,
            "date" to dateOfCompletedTask,
            "completion_date" to completionDateOfCompletedTask,
            "notification_date" to notificationDateOfCompletedTask,
            "key" to true
        )
    }

    constructor(_completedTaskText: String,
                _completedTaskDetailsText: String,
                _dateOfCompletedTask: Any?,
                _completedDateOfCompletedTask: Any?,
                _notificationDateOfCompletedTask: Any?){

        completedTaskText = _completedTaskText
        completedTaskDetailsText = _completedTaskDetailsText
        dateOfCompletedTask = _dateOfCompletedTask
        completionDateOfCompletedTask = _completedDateOfCompletedTask
        notificationDateOfCompletedTask = _notificationDateOfCompletedTask
        map = mapOf(
            "text" to completedTaskText,
            "details_text" to completedTaskDetailsText,
            "date" to dateOfCompletedTask,
            "completion_date" to completionDateOfCompletedTask,
            "notification_date" to notificationDateOfCompletedTask,
            "key" to true
        )
    }

    constructor(_map: Map<String,Any?>){
        completedTaskText = _map["text"] as String
        completedTaskDetailsText = _map["details_text"] as String
        dateOfCompletedTask = _map["date"]
        completionDateOfCompletedTask = _map["completion_date"]
        notificationDateOfCompletedTask = _map["notification_date"]
        map = _map
    }

    constructor(completedTask: CompletedTask){
        completedTaskText = completedTask.completedTaskText
        completedTaskDetailsText = completedTask.completedTaskDetailsText
        dateOfCompletedTask = completedTask.dateOfCompletedTask
        completionDateOfCompletedTask = completedTask.completionDateOfCompletedTask
        notificationDateOfCompletedTask = completedTask.notificationDateOfCompletedTask
        id = completedTask.id
        map = mapOf(
            "text" to completedTaskText,
            "details_text" to completedTaskDetailsText,
            "date" to dateOfCompletedTask,
            "completion_date" to completionDateOfCompletedTask,
            "notification_date" to notificationDateOfCompletedTask,
            "key" to true
        )
    }


}
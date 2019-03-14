package com.devourer.alexb.diaryforthecoolestboys.Adapters

import com.devourer.alexb.diaryforthecoolestboys.CompletedTask
import com.devourer.alexb.diaryforthecoolestboys.Task
import java.util.*


interface AdapterInterface {
    fun taskNotCompleteImageViewOnClick(task: Task, completionDate: Date, position: Int)
    fun taskTextViewOnClick(taskText: String?, taskDetailsText: String?, notificationDate: Date?, position: Int)
}

interface CompletedAdapterInterface{
    fun taskCompletedImageViewOnClick(completedTask: CompletedTask, task: Task, position: Int)
    fun completedTaskTextViewOnClick(taskText: String?, taskDetailsText: String?, notificationDate: Date?, position: Int)
}

interface DeleteCompletedTaskAdapterInterface{
    fun showCompletionBtnWhenCompletedTaskDeleted()
}

interface Snacks{
    fun snack(
    text: String,
    duration: Int,
    backColor: Int
    )

    fun snack(
        text: String,
        duration: Int,
        backColor: Int,
        actionText: String,
        actionColor: Int)

    fun removedSnack(
        text: String,
        duration: Int,
        backColor: Int,
        actionText: String,
        actionColor: Int,
        taskText: String,
        taskDate: Date?,
        completionDate: Date?,
        taskDetailsText: String,
        notificationDate: Date?,
        notificationId: Int?,
        taskId: String?,
        key: Boolean,
        position: Int)

    fun completedSnack(
        text: String,
        duration: Int,
        backColor: Int,
        actionText: String,
        actionColor: Int,
        map: Map<String, Any?>,
        key: Boolean,
        id: String?,
        position: Int)


}
package com.devourer.alexb.diaryforthecoolestboys.Adapters

import com.devourer.alexb.diaryforthecoolestboys.CompletedTask
import com.devourer.alexb.diaryforthecoolestboys.Task


interface AdapterInterface {
    fun taskNotCompleteImageViewOnClick(task: Task, completionDate: Any?, position: Int)
    fun taskTextViewOnClick(taskText: String, taskDetailsText: String, notificationDate: Any?, position: Int)
}

interface CompletedAdapterInterface{
    fun taskCompletedImageViewOnClick(completedTask: CompletedTask, task: Task, position: Int)
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
        taskDate: Any?,
        taskDetailsText: String,
        notificationDate: Any?,
        taskId: String,
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
        id: String,
        position: Int)


}
package com.devourer.alexb.diaryforthecoolestboys.Notification

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.devourer.alexb.diaryforthecoolestboys.Task
import io.realm.Realm
import io.realm.kotlin.where
import java.util.*

class NotificationUtils {

    companion object {
        const val TAG = "Main"
    }

    var realm: Realm = Realm.getDefaultInstance()
    private var mNotificationId: Int = -1
    private val idList = ArrayList<Int?>()
    private var taskText: String = ""
    private var taskDetails: String = ""

    fun setNotification(task: Task, title: String,time: Long, activity: Activity){
        Log.w(TAG, "setNotification | CurrentTime -> ${Calendar.getInstance().timeInMillis} | nTime -> $time")
        if (time > 0){

            mNotificationId = task.notificationId!!
            getTaskInfo(task.id, title)

            val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(activity.applicationContext, NotificationReceiver::class.java)

            alarmIntent.putExtra("timestamp",time)
            alarmIntent.putExtra("text", taskText)
            alarmIntent.putExtra("details", taskDetails)
            alarmIntent.putExtra("nId", mNotificationId)
            alarmIntent.putExtra("id", task.id)
            alarmIntent.putExtra("title", task.listTitle)


            val pendingIntent = PendingIntent.getBroadcast(activity, mNotificationId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }

    }

    fun getNotificationId() : Int{

        var notificationId: Int

        val tasks = realm
            .where<Task>()
            .isNotNull("notificationId")
            .findAll()

        tasks.forEach {
            idList.add(it.notificationId)
        }

        do {
            notificationId = Random().nextInt(50000)
        } while (idList.contains(mNotificationId))

        return notificationId

    }

    private fun getTaskInfo(taskId: String?, title: String){
        val task = realm
            .where<Task>()
            .`in`("listTitle",arrayOf(title))
            .`in`("id", arrayOf(taskId))
            .findFirst()

        Log.w(TAG, "taskText — ${task?.taskText} | taskDetails — ${task?.taskDetailsText}")
        if (task != null){
            taskText = task.taskText
            taskDetails = task.taskDetailsText
        }
    }

}
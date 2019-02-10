package com.devourer.alexb.diaryforthecoolestboys.Notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.devourer.alexb.diaryforthecoolestboys.CompletedTask
import com.devourer.alexb.diaryforthecoolestboys.MyData
import com.devourer.alexb.diaryforthecoolestboys.MyFirebase
import com.devourer.alexb.diaryforthecoolestboys.Task
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where
import java.util.*

class DoneTaskReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "Main"
    }

    lateinit var id: String
    lateinit var title: String
    lateinit var realm: Realm
    lateinit var fire: MyFirebase
    lateinit var data: MyData
    lateinit var date: Date


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.w(TAG, "DoneTaskReceiver | onReceive ")
        Realm.init(context)

        if (intent != null && intent.extras != null){
            Log.w(TAG, "DoneTaskReceiver | onReceive | if")
            id = intent.getStringExtra("id")
            title = intent.getStringExtra("title")
        }

        val mRealmConfiguration = RealmConfiguration.Builder()
            .name("DiaryFTCB.realm")
            .schemaVersion(3)
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(mRealmConfiguration)
        realm = Realm.getDefaultInstance()
        data = MyData()
        data.title = title
        fire = MyFirebase(context!!, data)
        date = Date()

        val task = realm
            .where<Task>()
            .`in`("listTitle", arrayOf(title))
            .`in`("id", arrayOf(id))
            .findFirst()

        cancelNotification(context, task?.notificationId as Int)

        fire.addTaskToCompleted(task.id, date)

        val completedTask = CompletedTask(task, date)
        realm.executeTransaction {
            it.insert(completedTask)
            task.deleteFromRealm()
        }
        realm.close()

        val doneIntent = Intent("notification")
        doneIntent.putExtra("done",true)
        doneIntent.putExtra("id", id)
        LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(doneIntent)
        Log.w(TAG, "DoneTaskReceiver | onReceive | end")

    }

    private fun cancelNotification(context:Context, notificationId: Int){
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notificationId)
        Log.w(TAG, "DoneTaskReceiver | onReceive | cancelNotification")
    }
}
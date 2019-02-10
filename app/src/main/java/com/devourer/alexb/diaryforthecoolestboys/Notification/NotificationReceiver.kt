package com.devourer.alexb.diaryforthecoolestboys.Notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.devourer.alexb.diaryforthecoolestboys.*

class NotificationReceiver : BroadcastReceiver() {

    private lateinit var mNotification: Notification
    private var mNotificationId: Int = -1

    companion object {
        const val CHANNEL_ID = "diaryforthecoolestboys.alexb.devourer.com.CHANNEL_ID"
        const val CHANNEL_NAME = "DiaryFTCB Notification"
        const val TAG = "Main"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.w(TAG, "onReceive")

        notify(context, intent)
    }



    @SuppressLint("NewApi")
    private fun createChannel(context: Context?){
        Log.w(TAG, "createChannel")

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(true)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = ResourcesCompat.getColor(context.resources, R.color.colorAccent,null)
        notificationChannel.description = "DiaryFTCB"
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun notify(context: Context?, intent: Intent?){
        Log.w(TAG, "notify")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel(context)
        }

        var timestamp: Long = 0
        var taskText = ""
        var taskDetailsText = ""
        var id = ""
        var title = ""
        if (intent != null && intent.extras != null){
            timestamp = intent.getLongExtra("timestamp",0)
            taskText = intent.getStringExtra("text")
            taskDetailsText = intent.getStringExtra("details")
            mNotificationId = intent.getIntExtra("nId",-1)
            id = intent.getStringExtra("id")
            title = intent.getStringExtra("title")
        }


        if (timestamp > 0){

            val notifyIntent = Intent(context, SplashActivity::class.java)
            val doneTaskIntent = Intent(context, DoneTaskReceiver::class.java)
            doneTaskIntent.putExtra("id", id)
            doneTaskIntent.putExtra("title", title)


            Log.w(TAG, "text -> $taskText | details -> $taskDetailsText")
            val nTitle = taskText
            val message = taskDetailsText

            notifyIntent.putExtra("title", title)
            notifyIntent.putExtra("id",mNotificationId)
            notifyIntent.putExtra("notification", true)

            notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val donePendingIntent = PendingIntent.getBroadcast(context, 0, doneTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                mNotification = Notification.Builder(context, CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_today_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context?.resources, R.drawable.ic_today_white_24dp))
                    .setAutoCancel(true)
                    .setContentTitle(nTitle)
                    .setColor(ResourcesCompat.getColor(context!!.resources,R.color.colorAccent,null))
                    .addAction(R.drawable.ic_done_blue_20dp,"Done",donePendingIntent)
                    .setStyle(Notification.BigTextStyle()
                        .bigText(message))
                    .setContentText(message)
                    .build()


            } else {
                mNotification = Notification.Builder(context)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_today_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context?.resources, R.drawable.ic_today_white_24dp))
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentTitle(nTitle)
                    .addAction(R.drawable.ic_done_blue_20dp,"Done",donePendingIntent)
                    .setStyle(Notification.BigTextStyle()
                        .bigText(message))
                    .setSound(uri)
                    .setContentText(message)
                    .build()
            }

            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(mNotificationId,mNotification)
            Log.w(TAG, "notificationManager.notify")

        }

    }


}
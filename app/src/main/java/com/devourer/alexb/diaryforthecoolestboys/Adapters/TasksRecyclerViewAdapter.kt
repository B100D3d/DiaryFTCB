package com.devourer.alexb.diaryforthecoolestboys.Adapters

import android.animation.Animator
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.devourer.alexb.diaryforthecoolestboys.*
import com.devourer.alexb.diaryforthecoolestboys.Notification.NotificationReceiver
import com.devourer.alexb.diaryforthecoolestboys.Notification.NotificationUtils
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TasksRecyclerViewAdapter(
    _context: Context,
    _activity: Activity,
    _tasks: ArrayList<Task>,
    _realm: Realm,
    _data: MyData,
    _fire: MyFirebase,
    _snackInterface: Snacks,
    _adapterInterface: AdapterInterface
) : RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder>() {


    companion object {
        private const val TAG: String = "Main"
    }
    private var fire: MyFirebase
    private var data: MyData
    private var realm: Realm
    private var mTasks: ArrayList<Task>
    private var mAdapterInterface: AdapterInterface
    private var mSnackInterface: Snacks
    private var mContext: Context
    private var mActivity: Activity

    init {
        mContext = _context
        mActivity = _activity
        mTasks = _tasks
        realm = _realm
        data = _data
        fire = _fire
        mAdapterInterface = _adapterInterface
        mSnackInterface = _snackInterface
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Log.w(TAG, "onCreateViewHolder start")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_tasklist, parent, false)
        //Log.w(TAG, "onCreateViewHolder end")
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.w(TAG, "onBindViewHolder $position start")
        setTaskText(holder,position)
        setTasksDetailsText(holder,position)
        setNotificationDateChip(holder,position)
        //Log.w(TAG, "onBindViewHolder $position end")
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)


        holder.taskNotCompleteImageView.setOnClickListener {
            imageViewOnClick(holder)
        }

        holder.taskTextLayout.setOnClickListener {
            textLayoutOnClick(holder)
        }

    }

    override fun getItemCount(): Int {
        return mTasks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var taskText: TextView = itemView.findViewById(R.id.taskText)
        var taskDetailsText: TextView = itemView.findViewById(R.id.detailsTaskText)
        var taskNotificationDateText: TextView = itemView.findViewById(R.id.taskNotificationDateText)
        var taskNotCompleteImageView: ImageView = itemView.findViewById(R.id.taskNotCompleteImage)
        var taskNotCompleteImageAnim: LottieAnimationView = itemView.findViewById(R.id.taskNotCompleteImageAnim)
        var taskTextLayout: LinearLayout = itemView.findViewById(R.id.taskTextLayout)

    }

    private fun imageViewOnClick(holder: ViewHolder){
        holder.taskNotCompleteImageView.visibility = View.GONE
        holder.taskNotCompleteImageAnim.visibility = View.VISIBLE
        holder.taskNotCompleteImageAnim.playAnimation()
        holder.taskNotCompleteImageAnim.addAnimatorListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                holder.taskNotCompleteImageAnim.removeAllAnimatorListeners()
                holder.taskNotCompleteImageAnim.clearAnimation()
                val position = holder.adapterPosition
                val task = Task(mTasks[position])
                val completionDate = Date()
                if (task.notificationId != null) {
                    cancelAlarm(task.notificationId as Int)
                    val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(task.notificationId as Int)
                }
                addTaskToCompleted(position, completionDate) // remove task from this list and add to firestore
                mAdapterInterface.taskNotCompleteImageViewOnClick(task, completionDate, position)
                holder.taskNotCompleteImageView.visibility = View.VISIBLE
                holder.taskNotCompleteImageAnim.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        })
    }

    private fun textLayoutOnClick(holder: ViewHolder){
        mAdapterInterface.taskTextViewOnClick(
            mTasks[holder.adapterPosition].taskText,
            mTasks[holder.adapterPosition].taskDetailsText,
            mTasks[holder.adapterPosition].notificationDateOfTask,
            holder.adapterPosition
        )
    }

    fun addTask(
        isAdd: Boolean,
        _taskText: String?,
        _taskDetailsText: String?,
        _dateAndTime: Calendar,
        isChipChecked: Boolean,
        snackInterface: Snacks
    ){
        if (!isAdd){
            if (!_taskText.isNullOrEmpty()) {
                val id = fire.uIdDoc.collection(data.title).document().id
                Log.w(TAG, "id -> $id")
                val taskDetailsText = if (_taskDetailsText.isNullOrBlank()) "" else _taskDetailsText
                val date = Date()
                val notificationDate = if (isChipChecked) _dateAndTime.time else null
                val notificationId = if (notificationDate != null) NotificationUtils().getNotificationId() else null
                val newTask = Task(_taskText, taskDetailsText, date, notificationDate, id, data.title, notificationId)
                Log.w(TAG, "mTasks.size -> ${mTasks.size}")
                if (mTasks.size == 0)
                    mTasks.add(newTask)
                else
                    mTasks.add(0, newTask)
                notifyItemInserted(0)
                realm.executeTransaction {
                    it.insert(Task(newTask))
                }
                if (notificationDate != null){
                    NotificationUtils().setNotification(newTask, data.title, _dateAndTime.timeInMillis, mActivity)
                }
                snackInterface.snack("Task added", Snackbar.LENGTH_SHORT, R.color.colorBackSnackbar)
                fire.addTask(newTask.map(), id)
                Log.w(TAG, "mTasks.size -> ${mTasks.size}")

            }
        }

    }

    fun addTask(task: Any, position: Int){
        mTasks.add(position, task as Task)
        if (task.notificationId != null)
            NotificationUtils().setNotification(task,data.title,task.notificationDateOfTask!!.time, mActivity)
        notifyItemInserted(position)
        realm.executeTransaction {
            it.insert(task)
        }
        fire.addTask(task.map(), task.id)
    }

    fun moveTaskFromCompleted(task: Task, position: Int){
        mTasks.add(position, task)
        notifyItemInserted(position)

    }

    fun moveTaskToCompleted(position: Int){
        mTasks.removeAt(position)
        notifyItemRemoved(position)

    }

    fun deleteTaskAfterNotificationDone(id: String){
        for ((i) in (0 until mTasks.size).withIndex()){
            if (mTasks[i].id == id){
                Log.w(TAG, "deleteTaskAfterNotificationDone | mTasks[$i].id -> ${mTasks[i].id}")
                mTasks.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    fun changeTask(
        taskEditText: String,
        taskDetailsEditText: String,
        chipAddDateText: String,
        _notificationDate: Date?,
        _dateAndTime: Calendar,
        position: Int,
        snackInterface: Snacks
    ){
        val dateFormat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
        val notificationDateText = if (_notificationDate != null) dateFormat.format(_notificationDate.time) else mContext.resources.getText(R.string.add_date)
        val id = mTasks[position].id
        val date = mTasks[position].dateOfTasks
        val notificationId = mTasks[position].notificationId
        val changedNotificationDate =
            if (chipAddDateText == notificationDateText) {
                _notificationDate
            } else if (chipAddDateText != notificationDateText && chipAddDateText != mContext.resources.getText(R.string.add_date)) {
                _dateAndTime.time
            }
            else null
        val changedTask = Task(taskEditText, taskDetailsEditText, date, changedNotificationDate, id, data.title, notificationId)
        if (isAnyChanges(taskEditText, taskDetailsEditText, changedNotificationDate, _notificationDate, position)) {
            if (changedNotificationDate == _dateAndTime.time){
                if (notificationId != null)
                    cancelAlarm(notificationId)
                changedTask.notificationId = NotificationUtils().getNotificationId()
                NotificationUtils().setNotification(changedTask, data.title, _dateAndTime.timeInMillis, mActivity)
            } else if (changedNotificationDate == null && notificationId != null){
                cancelAlarm(notificationId)
                changedTask.notificationId = null
            } else if (changedNotificationDate != null && changedNotificationDate.time > Date().time && notificationId != null){
                cancelAlarm(notificationId)
                NotificationUtils().setNotification(changedTask, data.title, changedNotificationDate.time, mActivity)
            }
            mTasks[position] = changedTask
            notifyItemChanged(position)
            val task = realm
                .where<Task>()
                .`in`("id", arrayOf(id))
                .findFirst()
            realm.executeTransaction {r ->
                task?.deleteFromRealm()
                r.insert(changedTask)
            }
            snackInterface.snack("Task changed", Snackbar.LENGTH_SHORT, R.color.colorBackSnackbar)
            //Log.w(TAG,"ID -> ${mTasks[position].listId}")
            fire.changeTask(mTasks[position].map(), mTasks[position].id)

        }

    }

    private fun addTaskToCompleted(position: Int, _completionDate: Date){
        val task = Task(mTasks[position])
        mTasks.removeAt(position)
        notifyItemRemoved(position)
        val completedTask = realm
            .where<Task>()
            .`in`("id", arrayOf(task.id))
            .findFirst()
        realm.executeTransaction {
            completedTask?.deleteFromRealm()
            it.insert(CompletedTask(task, _completionDate))
        }
        fire.addTaskToCompleted(task.id, _completionDate)

    }

    fun deleteTask(position: Int, snackInterface: Snacks){
        val task = Task(mTasks[position])
        if (task.notificationId != null) {
            cancelAlarm(task.notificationId as Int)
            val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(task.notificationId as Int)
        }
        mTasks.removeAt(position)
        notifyItemRemoved(position)
        val deletedTask = realm
            .where<Task>()
            .`in`("id", arrayOf(task.id))
            .findFirst()
        realm.executeTransaction {
            deletedTask!!.deleteFromRealm()
        }
        snackInterface.removedSnack(
            "Task removed",
            Snackbar.LENGTH_LONG,
            R.color.colorBackSnackbar,
            "UNDO",
            R.color.colorUNDOActionSnackbar,
            task.taskText,
            task.dateOfTasks,
            null,
            task.taskDetailsText,
            task.notificationDateOfTask,
            task.notificationId,
            task.id,
            false,
            position
        )
        fire.deleteTask(task.id)

    }

    private fun isAnyChanges(
        taskText: String,
        taskDetailsText: String,
        changedNotificationDate: Date?,
        _notificationDate: Date?,
        position: Int
    ) : Boolean{
        return (taskText != mTasks[position].taskText || taskDetailsText != mTasks[position].taskDetailsText || changedNotificationDate != _notificationDate) && !taskText.isEmpty()
    }

    private fun cancelAlarm(notificationId: Int){
        val alarmManager = mActivity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(mActivity.applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(mActivity, notificationId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    private fun setTaskText(holder: ViewHolder, position: Int){
        if (mTasks[position].taskText.length > 55){
            var temp = ""
            for (i in 0..54){
                temp += mTasks[position].taskText[i]
            }
            temp += "..."
            holder.taskText.text = temp
        }
        else if (mTasks[position].taskText.contains("\n")){
            var temp = ""
            for (i in 0 until mTasks[position].taskText.length){
                val it: String = mTasks[position].taskText[i].toString()
                if (it == "\n")
                    break
                temp += mTasks[position].taskText[i]
            }
            temp += "..."
            holder.taskText.text = temp
        }
        else
            holder.taskText.text = mTasks[position].taskText
    }

    private fun setTasksDetailsText(holder: ViewHolder, position: Int) {
        holder.taskDetailsText.visibility = View.GONE
        if (!mTasks[position].taskDetailsText.isBlank()) {
            holder.taskDetailsText.visibility = View.VISIBLE
            if (mTasks[position].taskDetailsText.length > 55) {
                var temp = ""
                for (i in 0..54) {
                    temp += mTasks[position].taskDetailsText[i]
                }
                temp += "..."
                holder.taskDetailsText.text = temp
            } else if (mTasks[position].taskDetailsText.contains("\n")) {
                var temp = ""
                for (i in 0 until mTasks[position].taskDetailsText.length) {
                    val it: String = mTasks[position].taskDetailsText[i].toString()
                    if (it == "\n")
                        break
                    temp += mTasks[position].taskDetailsText[i]
                }
                temp += "..."
                holder.taskDetailsText.text = temp
            } else
                holder.taskDetailsText.text = mTasks[position].taskDetailsText
        }
    }

    private fun setNotificationDateChip(holder: ViewHolder, position: Int){
        //Log.w(TAG,"setNotificationDateChip | position -> $position")
        holder.taskNotificationDateText.visibility = View.GONE
        if (mTasks[position].notificationDateOfTask != null){
            holder.taskNotificationDateText.visibility = View.VISIBLE
            val notificationDate = mTasks[position].notificationDateOfTask as Date
            val dateFormat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
            holder.taskNotificationDateText.text = dateFormat.format(notificationDate.time)
        }

    }
}
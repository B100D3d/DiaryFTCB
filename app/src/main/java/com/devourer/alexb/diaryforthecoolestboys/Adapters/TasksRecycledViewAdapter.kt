package com.devourer.alexb.diaryforthecoolestboys.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devourer.alexb.diaryforthecoolestboys.MyFirebase
import com.devourer.alexb.diaryforthecoolestboys.R
import com.devourer.alexb.diaryforthecoolestboys.Task
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TasksRecyclerViewAdapter(
    private val mContext: Context,
    _tasks: ArrayList<Task>,
    _snackInterface: Snacks,
    _adapterInterface: AdapterInterface
) : RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder>() {


    companion object {
        private const val TAG: String = "Main"
    }
    private var fire: MyFirebase = MyFirebase(mContext)
    private var mTasks = ArrayList<Task>()
    private var mAdapterInterface: AdapterInterface = _adapterInterface
    private var mSnackInterface: Snacks = _snackInterface

    init {
        mTasks = _tasks
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_tasklist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.w(TAG, "Tasks onBindViewHolder $position")
        /*val date: Date = mTaskDate[position] as Date
        val sdf = java.text.SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val sDate = sdf.format(date)*/
        //holder.taskText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        setTaskText(holder,position)
        setTasksDetailsText(holder,position)
        setNotificationDateChip(holder,position)



        //Log.w(TAG,"Date -> ${date} ")
        //Log.w(TAG,"sDate -> ${sDate} ")
        //"${Calendar.getInstance().get(Calendar.YEAR)} ${mTaskDate[position].toString().substring(4..19)}"
        //holder.taskDate.text = sDate.toString()

    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)


        holder.taskNotCompleteImageView.setOnClickListener {

            val position = holder.adapterPosition
            val task = mTasks[position]
            val completionDate = Date()
            addTaskToCompleted(position, completionDate, mSnackInterface) // remove task from this list and add to firestore
            mAdapterInterface.taskNotCompleteImageViewOnClick(task, completionDate, position)


        }

        holder.taskTextLayout.setOnClickListener {

            mAdapterInterface.taskTextViewOnClick(
                mTasks[holder.adapterPosition].taskText,
                mTasks[holder.adapterPosition].taskDetailsText,
                mTasks[holder.adapterPosition].notificationDateOfTask,
                holder.adapterPosition
            )

        }
    }

    override fun getItemCount(): Int {
        return mTasks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var taskText: TextView = itemView.findViewById(R.id.taskText)
        var taskDetailsText: TextView = itemView.findViewById(R.id.detailsTaskText)
        var taskNotificationDateText: TextView = itemView.findViewById(R.id.taskNotificationDateText)
        var parentLayout: LinearLayout = itemView.findViewById(R.id.parent_layout)
        //var taskNotCompleteImageLayout: LinearLayout = itemView.findViewById(R.id.taskNotCompleteImageLayout)
        var taskNotCompleteImageView: ImageView = itemView.findViewById(R.id.taskNotCompleteImage)
        var taskTextLayout: LinearLayout = itemView.findViewById(R.id.taskTextLayout)

    }

    fun addTask(isAdd: Boolean, _taskText: String?, _taskDetailsText: String?, _dateAndTime: Calendar, isChipChecked: Boolean, snackInterface: Snacks){
        if (!isAdd){
            if (!_taskText.isNullOrEmpty()) {
                val taskDetailsText = if (_taskDetailsText.isNullOrBlank()) "" else _taskDetailsText
                val date = Date()
                val notificationDate = if (isChipChecked) _dateAndTime.time else null
                val newTask = Task(_taskText, taskDetailsText, date, notificationDate)
                mTasks.add(0, newTask)
                notifyItemInserted(0)
                snackInterface.snack("Task added", Snackbar.LENGTH_SHORT, R.color.colorBackSnackbar)
                fire.addTask(newTask.map, mTasks)
            }
        }

    }

    fun addTask(task: Any, position: Int){
        mTasks.add(position, task as Task)
        notifyItemInserted(position)
        fire.addTask(task.map, task.id)
    }

    fun moveTaskFromCompleted(task: Task, position: Int){
        mTasks.add(position, task)
        notifyItemInserted(position)

    }

    fun moveTaskToCompleted(position: Int){
        mTasks.removeAt(position)
        notifyItemRemoved(position)

    }

    fun changeTask(
        taskEditText: String,
        taskDetailsEditText: String,
        chipAddDateText: String,
        _notificationDate: Any?,
        _dateAndTime: Calendar,
        position: Int,
        snackInterface: Snacks
    ){
        val dateFormat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
        val notificationDate = if (_notificationDate != null) dateFormat.format((_notificationDate as Date).time) else "Add date"
        val id = mTasks[position].id
        val date = mTasks[position].dateOfTasks
        val changedNotificationDate =
            if (chipAddDateText == notificationDate) {
                _notificationDate
            } else if (chipAddDateText != notificationDate && chipAddDateText != "Add date") {
                _dateAndTime.time
            }
            else null
        val changedTask = Task(taskEditText, taskDetailsEditText, date, changedNotificationDate, id)
        if ((taskEditText != mTasks[position].taskText || taskDetailsEditText != mTasks[position].taskDetailsText || changedNotificationDate != _notificationDate) && !taskEditText.isNullOrEmpty()) {
            mTasks[position] = changedTask
            notifyItemChanged(position)
            snackInterface.snack("Task changed", Snackbar.LENGTH_SHORT, R.color.colorBackSnackbar)
            Log.w(TAG,"ID -> ${mTasks[position].id}")
            fire.changeTask(mTasks[position].map, mTasks[position].id)

        }

    }

    private fun addTaskToCompleted(position: Int, _completionDate: Any?, snackInterface: Snacks){
        val task = mTasks[position]
        mTasks.removeAt(position)
        notifyItemRemoved(position)
        fire.addTaskToCompleted(task.id, _completionDate)

    }

    fun deleteTask(position: Int, snackInterface: Snacks){
        val task = mTasks[position]
        mTasks.removeAt(position)
        notifyItemRemoved(position)
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
            task.id,
            false,
            position
        )
        fire.deleteTask(task.id)

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
        if (!mTasks[position].taskDetailsText.isNullOrBlank()) {
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
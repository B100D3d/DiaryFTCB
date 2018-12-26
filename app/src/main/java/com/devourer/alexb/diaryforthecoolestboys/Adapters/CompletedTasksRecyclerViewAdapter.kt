package com.devourer.alexb.diaryforthecoolestboys.Adapters

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.devourer.alexb.diaryforthecoolestboys.*
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CompletedTasksRecyclerViewAdapter(
    private val mContext: Context,
    _completedTasks: ArrayList<CompletedTask>,
    _snackInterface: Snacks,
    _completedAdapterInterface: CompletedAdapterInterface
) : RecyclerView.Adapter<CompletedTasksRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private const val TAG: String = "Main"
    }
    private var mCompletedTasks = ArrayList<CompletedTask>()
    private var fire: MyFirebase = MyFirebase(mContext)
    var mSnackInterface: Snacks
    var mAdapterInterface: CompletedAdapterInterface

    init {
        mCompletedTasks = _completedTasks
        mAdapterInterface = _completedAdapterInterface
        mSnackInterface = _snackInterface
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_completedtasklist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.completedTaskText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        holder.completedTaskDetailsText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

        setTaskText(holder,position)
        setTasksDetailsText(holder,position)
        setNotificationDateChip(holder,position)

    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        //Log.w(TAG,"!!!!!!mTasksDate -> $mTasksDate")
        holder.taskCompletedImageLayout.setOnClickListener {
            val completedTask = mCompletedTasks[holder.adapterPosition]
            val task = Task(
                completedTask.completedTaskText,
                completedTask.completedTaskDetailsText,
                Date(),
                completedTask.notificationDateOfCompletedTask,
                completedTask.id
            )
            addTaskToNotCompleted(task, holder.adapterPosition)
            mAdapterInterface.taskCompletedImageViewOnClick(completedTask, task)
        }



    }

    override fun getItemCount(): Int {
        return mCompletedTasks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var completedTaskText: TextView = itemView.findViewById(R.id.completedTaskText)
        var completedTaskDetailsText: TextView = itemView.findViewById(R.id.completedDetailsTaskText)
        var completedTaskNotificationDateChip: Chip = itemView.findViewById(R.id.completedTaskNotificationDateChip)
        var parentLayout: ConstraintLayout = itemView.findViewById(R.id.parent_layout_)
        var taskCompletedImageLayout: LinearLayout = itemView.findViewById(R.id.taskCompletedImageLayout)
        var taskCompletedImageView: ImageView = itemView.findViewById(R.id.taskCompletedImage)
        var completeTaskTextLayout: LinearLayout = itemView.findViewById(R.id.completeTaskTextLayout)

    }

    fun addTask(task: Task, completionDate: Any?){
        val completedTask = CompletedTask(task.taskText,task.taskDetailsText,task.dateOfTasks, completionDate, task.notificationDateOfTask, task.id)
        mCompletedTasks.add(0, completedTask)
        notifyItemInserted(0)
    }

    fun addTask(completedTask: CompletedTask){
        mCompletedTasks.add(0,completedTask)
        notifyItemInserted(0)
    }

    fun moveTaskToNotCompleted(position: Int){
        mCompletedTasks.removeAt(position)
        notifyItemRemoved(position)

    }

    private fun addTaskToNotCompleted(task: Task, position: Int){
        mCompletedTasks.removeAt(position)
        notifyItemRemoved(position)
        fire.addTaskToNotCompleted(task.map,task.id)

    }

    fun deleteAllCompletedTasks(){
        val completedTasks = ArrayList<CompletedTask>()
        for ((i) in (0 until mCompletedTasks.size).withIndex()){
            completedTasks.add(CompletedTask(mCompletedTasks[i]))
        }
        mCompletedTasks.clear()
        notifyItemRangeRemoved(0,mCompletedTasks.size)
        fire.deleteAllCompletedTasks(completedTasks)
    }

    private fun setTaskText(holder: ViewHolder, position: Int){
        if (mCompletedTasks[position].completedTaskText.length > 55){
            var temp = ""
            for (i in 0..54){
                temp += mCompletedTasks[position].completedTaskText[i]
            }
            temp += "..."
            holder.completedTaskText.text = temp
        }
        else if (mCompletedTasks[position].completedTaskText.contains("\n")){
            var temp = ""
            for (i in 0 until mCompletedTasks[position].completedTaskText.length){
                val it: String = mCompletedTasks[position].completedTaskText[i].toString()
                if (it == "\n")
                    break
                temp += mCompletedTasks[position].completedTaskText[i]
            }
            temp += "..."
            holder.completedTaskText.text = temp
        }
        else
            holder.completedTaskText.text = mCompletedTasks[position].completedTaskText
    }

    private fun setTasksDetailsText(holder: ViewHolder, position: Int) {
        holder.completedTaskDetailsText.visibility = View.GONE
        if (!mCompletedTasks[position].completedTaskDetailsText.isNullOrBlank()) {
            holder.completedTaskDetailsText.visibility = View.VISIBLE
            if (mCompletedTasks[position].completedTaskDetailsText.length > 55) {
                var temp = ""
                for (i in 0..54) {
                    temp += mCompletedTasks[position].completedTaskDetailsText[i]
                }
                temp += "..."
                holder.completedTaskDetailsText.text = temp
            } else if (mCompletedTasks[position].completedTaskDetailsText.contains("\n")) {
                var temp = ""
                for (i in 0 until mCompletedTasks[position].completedTaskDetailsText.length) {
                    val it: String = mCompletedTasks[position].completedTaskDetailsText[i].toString()
                    if (it == "\n")
                        break
                    temp += mCompletedTasks[position].completedTaskDetailsText[i]
                }
                temp += "..."
                holder.completedTaskDetailsText.text = temp
            } else
                holder.completedTaskDetailsText.text = mCompletedTasks[position].completedTaskDetailsText
        }
    }

    private fun setNotificationDateChip(holder: ViewHolder, position: Int) {
        holder.completedTaskNotificationDateChip.visibility = View.GONE
        if (mCompletedTasks[position].notificationDateOfCompletedTask != null) {
            holder.completedTaskNotificationDateChip.visibility = View.VISIBLE
            val notificationDate: Date = mCompletedTasks[position].notificationDateOfCompletedTask as Date
            val dateFromat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
            holder.completedTaskNotificationDateChip.text = dateFromat.format(notificationDate.time)
        }
    }
}
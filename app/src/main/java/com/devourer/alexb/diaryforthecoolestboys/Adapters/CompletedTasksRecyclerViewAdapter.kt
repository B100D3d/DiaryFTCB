package com.devourer.alexb.diaryforthecoolestboys.Adapters

import android.animation.Animator
import android.content.Context
import android.graphics.Paint
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
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CompletedTasksRecyclerViewAdapter(
    mContext: Context,
    _completedTasks: ArrayList<CompletedTask>,
    _realm: Realm,
    _data: MyData,
    _fire: MyFirebase,
    _snackInterface: Snacks,
    _completedAdapterInterface: CompletedAdapterInterface
) : RecyclerView.Adapter<CompletedTasksRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private const val TAG: String = "Main"
    }
    private var isExpanded = false
    private var mCompletedTasks = ArrayList<CompletedTask>()
    private var fire: MyFirebase
    private var data: MyData
    var realm: Realm
    var mSnackInterface: Snacks
    var mAdapterInterface: CompletedAdapterInterface

    init {
        mCompletedTasks = _completedTasks
        mAdapterInterface = _completedAdapterInterface
        mSnackInterface = _snackInterface
        realm = _realm
        data = _data
        fire = _fire
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
        //Log.w(TAG, "CompletedTasks onBindViewHolder $position")

    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.taskCompletedImageAnim.speed = -1.5f

        //Log.w(TAG,"!!!!!!mTasksDate -> $mTasksDate")
        holder.taskCompletedImageView.setOnClickListener {
            holder.taskCompletedImageView.visibility = View.GONE
            holder.taskCompletedImageAnim.visibility = View.VISIBLE
            holder.taskCompletedImageAnim.playAnimation()
            holder.taskCompletedImageAnim.addAnimatorListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    holder.taskCompletedImageAnim.removeAllAnimatorListeners()
                    holder.taskCompletedImageAnim.clearAnimation()
                    val position = holder.adapterPosition
                    val completedTask = CompletedTask(mCompletedTasks[position])
                    val task = Task(completedTask)
                    task.dateOfTasks = Date()
                    addTaskToNotCompleted(task, position)
                    mAdapterInterface.taskCompletedImageViewOnClick(completedTask, task, position)
                    holder.taskCompletedImageView.visibility = View.VISIBLE
                    holder.taskCompletedImageAnim.visibility = View.GONE
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
            })
        }

        holder.completeTaskTextLayout.setOnClickListener {
            mAdapterInterface.completedTaskTextViewOnClick(
                mCompletedTasks[holder.adapterPosition].completedTaskText,
                mCompletedTasks[holder.adapterPosition].completedTaskDetailsText,
                mCompletedTasks[holder.adapterPosition].notificationDateOfCompletedTask,
                holder.adapterPosition
            )
        }



    }

    override fun getItemCount(): Int {
        return mCompletedTasks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var completedTaskText: TextView = itemView.findViewById(R.id.completedTaskText)
        var completedTaskDetailsText: TextView = itemView.findViewById(R.id.completedDetailsTaskText)
        var completedTaskNotificationDateText: TextView = itemView.findViewById(R.id.completedTaskNotificationDateText)
        var taskCompletedImageView: ImageView = itemView.findViewById(R.id.taskCompletedImage)
        var taskCompletedImageAnim: LottieAnimationView = itemView.findViewById(R.id.taskCompletedImageAnim)
        var completeTaskTextLayout: LinearLayout = itemView.findViewById(R.id.completeTaskTextLayout)

    }

    fun onCompletedBtn(){
        isExpanded = !isExpanded
    }

    fun addTask(task: Task, completionDate: Date){
        val completedTask = CompletedTask(task, completionDate)
        mCompletedTasks.add(0, completedTask)
        notifyItemInserted(0)
    }

    fun addTask(completedTask: Any, position: Int){
        mCompletedTasks.add(position, completedTask as CompletedTask)
        notifyItemInserted(position)
        realm.executeTransaction {
            it.insert(completedTask)
        }
        fire.addTask(completedTask.map(),completedTask.id)

    }

    fun moveTaskToNotCompleted(position: Int){
        mCompletedTasks.removeAt(position)
        notifyItemRemoved(position)

    }

    fun addTaskAfterNotificationDone(completedTask: CompletedTask){
        mCompletedTasks.add(0, completedTask)
        notifyItemInserted(0)
    }

    private fun addTaskToNotCompleted(task: Task, position: Int){
        mCompletedTasks.removeAt(position)
        notifyItemRemoved(position)
        val deletedTask = realm
            .where<CompletedTask>()
            .`in`("id", arrayOf(task.id))
            .findFirst()
        realm.executeTransaction {
            deletedTask!!.deleteFromRealm()
            it.insert(task)
        }
        if (mCompletedTasks.isEmpty())
            isExpanded = false
        fire.addTaskToNotCompleted(task.map(),task.id)

    }

    fun deleteCompletedTask(position: Int, showCompletionBtnWhenDeleted: DeleteCompletedTaskAdapterInterface){
        val completedTask = CompletedTask(mCompletedTasks[position])
        mCompletedTasks.removeAt(position)
        notifyItemRemoved(position)
        showCompletionBtnWhenDeleted.showCompletionBtnWhenCompletedTaskDeleted()
        val deletedCompletedTask = realm
            .where<CompletedTask>()
            .`in`("id", arrayOf(completedTask.id))
            .findFirst()
        realm.executeTransaction {
            deletedCompletedTask!!.deleteFromRealm()
        }
        mSnackInterface.removedSnack(
            "Task removed",
            Snackbar.LENGTH_LONG,
            R.color.colorBackSnackbar,
            "UNDO",
            R.color.colorUNDOActionSnackbar,
            completedTask.completedTaskText,
            completedTask.dateOfCompletedTask,
            completedTask.completionDateOfCompletedTask,
            completedTask.completedTaskDetailsText,
            completedTask.notificationDateOfCompletedTask,
            null,
            completedTask.id,
            true,
            position
        )
        fire.deleteTask(completedTask.id)
    }

    fun deleteAllCompletedTasks(){
        val completedTasks = ArrayList<CompletedTask>()
        for ((i) in (0 until mCompletedTasks.size).withIndex()){
            completedTasks.add(CompletedTask(mCompletedTasks[i]))
        }
        mCompletedTasks.clear()
        notifyItemRangeRemoved(0,mCompletedTasks.size)
        val deletedCompletedTasks = realm.
            where<CompletedTask>()
            .`in`("listTitle", arrayOf(data.title))
            .findAll()
        realm.executeTransaction {
            deletedCompletedTasks.deleteAllFromRealm()
        }
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
        if (!mCompletedTasks[position].completedTaskDetailsText.isBlank()) {
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
        holder.completedTaskNotificationDateText.visibility = View.GONE
        if (mCompletedTasks[position].notificationDateOfCompletedTask != null) {
            holder.completedTaskNotificationDateText.visibility = View.VISIBLE
            val notificationDate: Date = mCompletedTasks[position].notificationDateOfCompletedTask as Date
            val dateFromat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
            holder.completedTaskNotificationDateText.text = dateFromat.format(notificationDate.time)
        }
    }
}
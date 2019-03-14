package com.devourer.alexb.diaryforthecoolestboys.Fragments

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.devourer.alexb.diaryforthecoolestboys.*
import com.devourer.alexb.diaryforthecoolestboys.Notification.NotificationReceiver
import com.devourer.alexb.diaryforthecoolestboys.Notification.NotificationUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.change_list_bottomsheet.*
import java.util.*

class ChangeListBottomNavigationDrawerFragment : BottomSheetDialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.change_list_bottomsheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainActivity = activity as MainActivity
        closeKeyboard(mainActivity)

        initMenus(mainActivity)
        val listId: Long = mainActivity.data.listId
        lists_navigation_view.setCheckedItem(listId.toInt())
        lists_navigation_view.setNavigationItemSelectedListener { menuItem ->
            moveTaskToAnotherList(mainActivity, menuItem)
            dismiss()
            true
        }

        disableNavigationViewScrollbars(lists_navigation_view)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val window = dialog.window
        val wlp = window?.attributes
        wlp?.windowAnimations = R.style.DialogAnimation

        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog


            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)

            bottomSheet.setBackgroundColor(ResourcesCompat.getColor(resources, android.R.color.transparent, null))

            bottomSheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }
            })
        }

        return dialog
    }

    private fun moveTaskToAnotherList(mainActivity: MainActivity, menuItem: MenuItem){
        val title = menuItem.title
        val task = mainActivity.realm
            .where<Task>()
            .`in`("listTitle", arrayOf(mainActivity.data.title))
            .`in`("id", arrayOf(mainActivity.mTasks[mainActivity.data.position].id))
            .findFirst()
        mainActivity.mTasks.removeAt(mainActivity.data.position)
        mainActivity.taskListGroupAdapter.notifyItemRemoved(mainActivity.data.position)
        mainActivity.fire.moveTaskToAnotherList(task, mainActivity.data.title, title.toString())
        mainActivity.realm.executeTransaction {
            task?.listTitle = title.toString()
        }
        if (task?.notificationId != null && task.notificationDateOfTask != null && (task.notificationDateOfTask as Date).time > Date().time)
            changeNotification(mainActivity, task)
        mainActivity.updateBarUI(false)
        mainActivity.snacks.snack("Moved to \"$title\"",Snackbar.LENGTH_SHORT,R.color.colorBackSnackbar)
    }

    private fun changeNotification(mainActivity: MainActivity, task: Task){
        val alarmManager = mainActivity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(mainActivity.applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(mainActivity, task.notificationId!!, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.cancel(pendingIntent)
        NotificationUtils().setNotification(task, task.listTitle!!, task.notificationDateOfTask!!.time, mainActivity)
    }

    private fun disableNavigationViewScrollbars(navigationView: NavigationView?) {
        val navigationMenuView = navigationView?.getChildAt(0) as NavigationMenuView
        navigationMenuView.isVerticalScrollBarEnabled = false
    }

    private fun initMenus(mainActivity: MainActivity){
        val menu = lists_navigation_view.menu
        val taskList = mainActivity.mTaskLists
        for ((i) in (0 until taskList.size).withIndex()){
            menu.add(R.id.tasksListGroupMenu,taskList[i].taskListId.toInt(),0,taskList[i].nameOfTaskList).isCheckable = true
        }

    }

    private fun closeKeyboard(mainActivity: MainActivity){
        val imm = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = mainActivity.currentFocus
        if (view == null)
            view = View(mainActivity)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
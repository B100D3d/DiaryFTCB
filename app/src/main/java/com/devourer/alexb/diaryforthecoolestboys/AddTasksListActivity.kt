package com.devourer.alexb.diaryforthecoolestboys

import android.content.Context
import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add_tasks_list.*
import java.util.*
import java.util.concurrent.TimeUnit

class AddTasksListActivity : AppCompatActivity() {

    private lateinit var fire: MyFirebase
    private val handler = Handler()
    private val listsId = ArrayList<Long>()
    lateinit var realm: Realm
    lateinit var data: MyData
    lateinit var snackbar: Snackbar
    private var rand: Long = 0

    companion object {
        private const val TAG = "Main"
        const val INTENT_ADD_LIST_ID = "add_list"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w(TAG, "AddTasksListActivity onCreate")
        setContentView(R.layout.activity_add_tasks_list)
        data = intent.getParcelableExtra(INTENT_ADD_LIST_ID)
        fire = MyFirebase(this,data)
        realm = Realm.getDefaultInstance()
        Log.w(TAG, "AddTasksListActivity Realm.getDefaultInstance()")

        val lists = realm
            .where<TaskList>()
            .findAll()
        Log.w(TAG, "AddTasksListActivity findAll()")
        lists.forEach {
            listsId.add(it.taskListId)
        }
        Log.w(TAG, "AddTasksActivity | listsId -> $listsId")
        do {
            rand = Random().nextInt(5000).toLong()
        } while (listsId.contains(rand))
        Log.w(TAG, "AddTasksActivity | rand -> $rand")

        Thread(Runnable {
            TimeUnit.MILLISECONDS.sleep(300)
            handler.post {
                addTaskListEditText.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(addTaskListEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        }).start()

        addTaskListDoneBtn.setOnClickListener {
            when {
                (addTaskListEditText.text!!.length > 47) -> Thread(Runnable {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    var view = currentFocus
                    if (view == null)
                        view = View(this)
                    handler.post {imm.hideSoftInputFromWindow(view.windowToken, 0)}
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post {snack("Too many characters in the title!", Snackbar.LENGTH_SHORT, R.color.colorBackSnackbar)}
                }).start()
                (addTaskListEditText.text.isNullOrEmpty()) -> hideKeyboard()
                (addTaskListEditText.text!!.contains(".")) -> {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    var view = currentFocus
                    if (view == null)
                        view = View(this)
                    handler.post {imm.hideSoftInputFromWindow(view!!.windowToken, 0)}
                    TimeUnit.MILLISECONDS.sleep(300)
                    snack("Title name cannot consist dots", Snackbar.LENGTH_SHORT, R.color.colorBackSnackbar)
                }
                else -> updateUi()
            }
        }

        addTaskListCancelBtn.setOnClickListener {
            hideKeyboard()
        }

    }

    private fun snack(
        text: String,
        duration: Int,
        backColor: Int
    ){
        val screenSize = Point()
        windowManager.defaultDisplay.getSize(screenSize)
        val marginSide = 0
        val marginBottom: Int = (screenSize.y / 6.4).toInt()
        snackbar = Snackbar.make(
            snackbarLayout,
            text,
            duration)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(ResourcesCompat.getColor(resources,backColor,null))
        val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
        params.setMargins(
            params.leftMargin+marginSide,
            params.topMargin,
            params.rightMargin+marginSide,
            params.bottomMargin+marginBottom
        )
        snackbarView.layoutParams = params
        snackbar.show()
    }

    private fun hideKeyboard(){
        Thread(Runnable {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = currentFocus
            if (view == null)
                view = View(this)
            handler.post {imm.hideSoftInputFromWindow(view.windowToken, 0)}
            TimeUnit.MILLISECONDS.sleep(300)
            handler.post {super.onBackPressed()}
        }).start()
    }

    override fun onBackPressed() {
        //updateUi(false)
        super.onBackPressed()
    }

    private fun updateUi() {
        val intent = Intent(this, MainActivity::class.java)
        val taskListName = addTaskListEditText.text.toString()
        val taskListMap = mapOf(
            "name" to taskListName,
            "id" to rand,
            "date" to Date()
        )
        fire.addTaskList(taskListName,taskListMap)
        val taskList = TaskList(taskListName, rand)
        realm.executeTransaction {
            it.insert(taskList)
        }
        data.listId = rand
        data.title = taskListName
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null)
            view = View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        intent.putExtra(INTENT_ADD_LIST_ID,data)
        startActivity(intent)
        finish()

    }
}

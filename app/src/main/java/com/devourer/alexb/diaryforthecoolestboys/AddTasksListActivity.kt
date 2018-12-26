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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_tasks_list.*
import java.util.*
import java.util.concurrent.TimeUnit

class AddTasksListActivity : AppCompatActivity() {

    private lateinit var fire: MyFirebase
    private val handler = Handler()
    private val listsId = ArrayList<Long>()
    lateinit var snackbar: Snackbar
    private var rand: Long = 0

    companion object {
        private const val TAG = "Main"
        const val INTENT_ID = "Add_list"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tasks_list)
        fire = MyFirebase(this)

        fire.uIdDoc.collection("#$!@#$!@!@#$!@#!3123!@#").get().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w(TAG, "AddTasksActivity | successful get documents for they ids")
                if (it.result!!.documents.isNotEmpty()) {
                    it.result!!.documents.forEach {
                        listsId.add(it["id"] as Long)
                    }
                    Log.w(TAG, "AddTasksActivity | listsId -> $listsId")
                    do {
                        rand = Random().nextInt(5000).toLong()
                    } while (listsId.contains(rand))
                    Log.w(TAG, "AddTasksActivity | rand -> $rand")
                }
            }
        }

        val t = Thread(Runnable {
            TimeUnit.MILLISECONDS.sleep(300)
            handler.post {
                addTaskListEditText.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(addTaskListEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        })
        t.start()

        addTaskListDoneBtn.setOnClickListener {
            if (!addTaskListEditText.text.isNullOrEmpty() && addTaskListEditText.text!!.length > 47){
                Thread(Runnable {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    var view = currentFocus
                    if (view == null)
                        view = View(this)
                    handler.post {imm.hideSoftInputFromWindow(view.windowToken, 0)}
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post {snack("Too many characters in the title!", Snackbar.LENGTH_SHORT, R.color.colorBackSnackbar)}
                }).start()

            }
            else if (addTaskListEditText.text.isNullOrEmpty()) {
                hideKeyboard()
            } else {
                updateUi()
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

    fun hideKeyboard(){
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
        fire.uIdDoc.collection("#$!@#$!@!@#$!@#!3123!@#").document(addTaskListEditText.text.toString()).set(
            mapOf(
                "name" to addTaskListEditText.text.toString(),
                "id" to rand,
                "date" to Timestamp(Date())
            )
        )
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.w(TAG, "AddTasksActivity | successful adding list")
                }
            }
        NavMenuCheckedItem.id = rand
        NavMenuCheckedItem.title = addTaskListEditText.text.toString()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null)
            view = View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        startActivity(intent)
        finish()

    }
}

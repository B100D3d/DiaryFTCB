package com.devourer.alexb.diaryforthecoolestboys

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.devourer.alexb.diaryforthecoolestboys.Fragments.BottomNavigationDrawerFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.devourer.alexb.diaryforthecoolestboys.Adapters.*
import com.devourer.alexb.diaryforthecoolestboys.FingerprintLibrary.FingerprintDialogBuilder
import com.devourer.alexb.diaryforthecoolestboys.Fragments.DatePickerBottomNavigationDrawerFragment
import com.devourer.alexb.diaryforthecoolestboys.Fragments.SettingsBottomNavigationDrawerFragment
import com.github.okdroid.checkablechipview.CheckableChipView
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.devourer.alexb.diaryforthecoolestboys.FingerprintLibrary.AuthenticationCallback
import com.devourer.alexb.diaryforthecoolestboys.Fragments.ChangeListBottomNavigationDrawerFragment
import com.devourer.alexb.diaryforthecoolestboys.Notification.NotificationUtils
import com.google.android.material.animation.AnimationUtils.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import io.realm.kotlin.where
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.animators.*


class MainActivity : AppCompatActivity() {

    private var isBarAnimateEnd = true
    lateinit var realm: Realm
    lateinit var mRealmConfiguration: RealmConfiguration
    lateinit var fire: MyFirebase
    lateinit var data: MyData
    var dateAndTime = Calendar.getInstance()!!
    lateinit var snackbar: Snackbar
    private var isAdd: Boolean = true
    private var isChange: Boolean = false
    private var isCompleted: Boolean = false
    var isDeleteTaskOrList = false // false — task | true — list
    private var isCompletedListGroupExpanded = false
    private val handler = Handler()
    var mFragmentManager: FragmentManager = fragmentManager
    var mTasks = ArrayList<Task>()
    var mCompletedTasks = ArrayList<CompletedTask>()
    var mTaskLists = ArrayList<TaskList>()
    lateinit var taskListGroupAdapter: TasksRecyclerViewAdapter
    lateinit var completedTasksListGroupAdapter: CompletedTasksRecyclerViewAdapter
    companion object {
        private const val TAG: String = "Main"
        const val INTENT_ID = "auth"
        const val INTENT_ADD_LIST_ID = "add_list"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w(TAG,"onCreate")
        setContentView(R.layout.activity_main)
        setSupportActionBar(bar)
        ///////////////////
        setRefreshLayoutColor()
        data = MyData()
        fire = MyFirebase(this@MainActivity, data)
        mRealmConfiguration = RealmConfiguration.Builder()
            .name("DiaryFTCB.realm")
            .schemaVersion(3)
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(mRealmConfiguration)
        realm = Realm.getDefaultInstance()
        getIntentsExtras()
        initRecyclerView()
        initTasksList()
        onNavItemSelected(false)
        ///////////////////////


        fab.setOnClickListener {
            fabOnClick()
        }

        changeTaskListBtn.setOnClickListener {
            changeListOnClick()
        }

        chipAddDate.onCheckedChangeListener = { view: CheckableChipView, isChecked: Boolean ->
            chipDateOnChecked(isChecked)
        }

        taskDetailsEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                editScrollView.scrollBy(0,100)
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editScrollView.scrollBy(0,100)
            }
        })

        chipAddDetails.onCheckedChangeListener = { view: CheckableChipView, isChecked: Boolean ->
            chipDetailsOnChecked(isChecked)
        }

        refreshLayout.setOnRefreshListener {
            onRefresh()
        }

        completedBtn.setOnClickListener { completedBtnOnClick() }

        contentScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            contentOnScroll(scrollY, oldScrollY)
        }

        editScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            editOnScroll(scrollY, oldScrollY)
        }

    }

    private fun getIntentsExtras(){
        if (intent.getBooleanExtra(INTENT_ID,false))
            snacks.snack("Authentication access!",Snackbar.LENGTH_LONG,R.color.colorBackSnackbar,"OK",R.color.colorOkActionSnackbar)
        if (intent.hasExtra(INTENT_ADD_LIST_ID))
            data = intent.getParcelableExtra(INTENT_ADD_LIST_ID)
        if (intent.getBooleanExtra("notification", false)){
            val id = intent.getIntExtra("id", -1)
            val task = realm
                .where<Task>()
                .`in`("notificationId", arrayOf(id))
                .findFirst()
            realm.executeTransaction {
                task?.notificationId = null
            }


        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    fun signOut() {
        fire.mAuth.signOut()
        fire.mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            updateUI(null)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        when (user) {
            null -> {
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun fabOnClick(){
        slideBarUp()
        when {
            !isChange -> {
                Log.w(TAG,"Вутри условия !isChange слушателя ФАБа")
                taskListGroupAdapter.addTask(
                    isAdd,
                    taskEditText.text.toString(),
                    taskDetailsEditText.text.toString(),
                    dateAndTime,
                    chipAddDate.isChecked,
                    snacks
                )
                updateBarUI(isAdd)
            }
            isChange -> {
                Log.w(TAG,"Вутри условия isChange слушателя ФАБа")
                if (!isCompleted) {
                    taskListGroupAdapter.changeTask(
                        taskEditText.text.toString(),
                        taskDetailsEditText.text.toString(),
                        chipAddDate.text.toString(),
                        data.notificationDate,
                        dateAndTime,
                        data.position,
                        snacks
                    )
                }
                isChange = false
                updateBarUI(false)
            }
        }
    }

    private fun changeListOnClick(){
        val changeListBottomNavigationDrawerFragment = ChangeListBottomNavigationDrawerFragment()
        changeListBottomNavigationDrawerFragment.show(supportFragmentManager,changeListBottomNavigationDrawerFragment.tag)
    }

    private fun chipDateOnChecked(isChecked: Boolean){
        when {
            (isChecked && !isAdd) -> {
                Thread(Runnable {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    var view = currentFocus
                    if (view == null)
                        view = View(this)
                    handler.post { imm.hideSoftInputFromWindow(view.windowToken, 0) }
                    TimeUnit.MILLISECONDS.sleep(200)
                    handler.post {
                        val datePickerBottomNavigationDrawerFragment = DatePickerBottomNavigationDrawerFragment()
                        datePickerBottomNavigationDrawerFragment.show(supportFragmentManager,DatePickerBottomNavigationDrawerFragment().tag)
                    }
                }).start()

            }
            (!isChecked && !isAdd) -> chipAddDate.text = resources.getText(R.string.add_date)
        }
    }

    private fun chipDetailsOnChecked(isChecked: Boolean){
        when {
            (isChecked && !isAdd) -> {
                taskDetailsEditTextInputLayout.visibility = View.VISIBLE
                taskDetailsEditText.requestFocus()
                Thread(Runnable {
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(taskDetailsEditText, InputMethodManager.SHOW_IMPLICIT)
                    }
                }).start()
            }
            (!isChecked && !isAdd) -> {
                taskDetailsEditText.setText("")
                taskDetailsEditTextInputLayout.visibility = View.INVISIBLE
                taskEditText.requestFocus()
            }
        }
    }

    private fun onRefresh(){
        onNavItemSelected(true)
        refreshLayout.isRefreshing = false
    }

    private fun contentOnScroll(scrollY: Int, oldScrollY: Int){
        if (isBarAnimateEnd){
            when{
                scrollY > oldScrollY -> slideBarDown()
                scrollY < oldScrollY -> slideBarUp()
            }
        }
    }

    private fun editOnScroll(scrollY: Int, oldScrollY: Int){
        if (isBarAnimateEnd){
            when{
                scrollY > oldScrollY -> slideBarDown()
                scrollY < oldScrollY -> slideBarUp()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.menu -> {
                val settingsBottomNavigationDrawerFragment = SettingsBottomNavigationDrawerFragment()
                settingsBottomNavigationDrawerFragment.show(supportFragmentManager,settingsBottomNavigationDrawerFragment.tag)
            }
            R.id.back -> updateBarUI(false)
            R.id.back_ -> {
                updateBarUI(false)
                isChange = false
            }
            R.id.delete -> {
                isChange = false
                when {
                    Build.VERSION.SDK_INT >= 23 -> {
                        isDeleteTaskOrList = false
                        FingerprintDialogBuilder(this)
                            .setTitle("Delete")
                            .setSubtitle("")
                            .setDescription("Are you want to delete this task?")
                            .setNegativeButton("Cancel")
                            .show(mFragmentManager,callback)
                    }
                    else -> {
                        isChange = false
                        when{
                            !isCompleted -> { taskListGroupAdapter.deleteTask(data.position,snacks) }
                            isCompleted -> { completedTasksListGroupAdapter.deleteCompletedTask(data.position, showCompletionBtnWhenDeleted) }
                        }
                        updateBarUI(false)
                    }
                }
            }
            android.R.id.home -> {
                val bottomNavigationDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavigationDrawerFragment.show(supportFragmentManager,bottomNavigationDrawerFragment.tag)
            }
        }
        return true
    }

    private fun setUiAlpha(b: Boolean){
        when (b){
            true -> {
                headerView.animate().alpha(0f).setDuration(150).setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        headerView.alpha = 0f
                        headerView.visibility = View.GONE
                    }
                })
                headerLineView.animate().alpha(0f).setDuration(150).setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        headerLineView.alpha = 0f
                        headerLineView.visibility = View.GONE
                    }
                })
                refreshLayout.animate().alpha(0f).setDuration(150).setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        refreshLayout.alpha = 0f
                        refreshLayout.visibility = View.GONE
                    }
                })
                Thread(Runnable {
                    TimeUnit.MILLISECONDS.sleep(150)
                    handler.post {
                        editScrollView.visibility = View.VISIBLE
                        editScrollView.animate().alpha(1f).setDuration(150).setListener(object : AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                editScrollView.alpha = 1f
                            }
                        })
                    }
                }).start()
            }
            false -> {
                editScrollView.animate().alpha(0f).setDuration(150).setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        editScrollView.alpha = 0f
                        editScrollView.visibility = View.GONE
                    }
                })
                Thread(Runnable {
                    TimeUnit.MILLISECONDS.sleep(150)
                    handler.post {
                        headerView.visibility = View.VISIBLE
                        headerView.animate().alpha(1f).setDuration(150).setListener(object : AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                headerView.alpha = 1f
                            }
                        })
                        headerLineView.visibility = View.VISIBLE
                        headerLineView.animate().alpha(1f).setDuration(150).setListener(object : AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                headerLineView.alpha = 1f
                            }
                        })
                        refreshLayout.visibility = View.VISIBLE
                        refreshLayout.animate().alpha(1f).setDuration(150).setListener(object : AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                refreshLayout.alpha = 1f
                            }
                        })
                    }
                }).start()
            }
        }
    }


    fun updateBarUI(b:Boolean){
        when (b) {
            true -> {
                progressBarMain.cancelAnimation()
                progressBarMain.visibility = View.GONE
                setUiAlpha(b)
                fab.setImageDrawable(getDrawable(R.drawable.avd_add_to_done))
                val icon = fab.drawable as AnimatedVectorDrawable
                icon.start()
                bar.navigationIcon = null
                bar.replaceMenu(R.menu.empty_menu)
                Thread(Runnable {
                    handler.post {bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END}
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post {
                        taskEditText.requestFocus()
                        bar.replaceMenu(R.menu.add_task_menu)
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(taskEditText, InputMethodManager.SHOW_IMPLICIT)
                    }
                }).start()
                isAdd = false
            }
            false -> {
                if (isCompleted){
                    taskEditText.isEnabled = true
                    taskDetailsEditText.isEnabled = true
                    taskEditText.paintFlags = 1282 // Standard
                    taskDetailsEditText.paintFlags = 1282
                    chipAddDate.isClickable = true
                    chipAddDetails.isClickable = true

                }
                changeTaskListBtn.visibility = View.GONE
                setUiAlpha(b)
                fab.setImageDrawable(getDrawable(R.drawable.avd_done_to_add))
                val icon = fab.drawable as AnimatedVectorDrawable
                icon.start()
                bar.replaceMenu(R.menu.empty_menu)
                Thread(Runnable {
                    handler.post {bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER}
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post {
                        bar.replaceMenu(R.menu.main_menu)
                        bar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
                        taskEditText.setText("")
                        taskDetailsEditText.setText("")
                        taskDetailsEditTextInputLayout.visibility = View.INVISIBLE
                        chipAddDate.isChecked = false
                        chipAddDetails.isChecked = false
                        chipAddDate.text = resources.getText(R.string.add_date)
                    }
                }).start()
                isAdd = true
                isCompleted = false
            }
        }


    }

    private fun updateUIWhenTaskTextIsClicked(taskText: String?, taskDetailsText: String?, _notificationDate: Any?){
        changeTaskListBtn.visibility = View.VISIBLE
        taskEditText.setText(taskText)
        val dateFormat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
        val notificationDate = if (_notificationDate != null) dateFormat.format((_notificationDate as Date).time) else ""
        if (!taskDetailsText.isNullOrBlank()){
            chipAddDetails.isChecked = true
            taskDetailsEditTextInputLayout.visibility = View.VISIBLE
            taskDetailsEditText.setText(taskDetailsText)
            taskEditText.requestFocus()
        }
        if (!notificationDate.isNullOrBlank()){
            chipAddDate.isChecked = true
            chipAddDate.text = notificationDate
        }
        setUiAlpha(true)
        bar.navigationIcon = null
        fab.setImageDrawable(getDrawable(R.drawable.avd_add_to_done))
        val icon = fab.drawable as AnimatedVectorDrawable
        icon.start()
        bar.replaceMenu(R.menu.empty_menu)
        Thread(Runnable {
            handler.post {bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END}
            TimeUnit.MILLISECONDS.sleep(300)
            handler.post { bar.replaceMenu(R.menu.edit_task_menu) }
        }).start()
        isAdd = false
    }

    fun updateUIWhenCompletedTaskTextIsClicked(taskText: String?, taskDetailsText: String?, _notificationDate: Any?){
        Log.w(TAG,"paintFlags -> ${taskEditText.paintFlags}")
        taskEditText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        Log.w(TAG,"paintFlags! -> ${taskEditText.paintFlags}")
        taskEditText.isEnabled = false
        chipAddDetails.isClickable = false
        chipAddDate.isClickable = false
        taskEditText.setText(taskText)
        taskEditText.requestFocus()
        val dateFormat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
        val notificationDate = if (_notificationDate != null) dateFormat.format((_notificationDate as Date).time) else ""
        if (!taskDetailsText.isNullOrBlank()){
            chipAddDetails.isChecked = true
            taskDetailsEditTextInputLayout.visibility = View.VISIBLE
            Log.w(TAG,"paintFlags1 -> ${taskDetailsEditText.paintFlags}")
            taskDetailsEditText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            Log.w(TAG,"paintFlags1! -> ${taskDetailsEditText.paintFlags}")
            taskDetailsEditText.isEnabled = false
            taskDetailsEditText.setText(taskDetailsText)
            taskEditText.requestFocus()
        }
        if (!notificationDate.isNullOrBlank()){
            chipAddDate.isChecked = true
            chipAddDate.text = notificationDate
        }
        setUiAlpha(true)
        bar.navigationIcon = null
        fab.setImageDrawable(getDrawable(R.drawable.avd_add_to_done))
        val icon = fab.drawable as AnimatedVectorDrawable
        icon.start()
        bar.replaceMenu(R.menu.empty_menu)
        Thread(Runnable {
            handler.post {bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END}
            TimeUnit.MILLISECONDS.sleep(300)
            handler.post { bar.replaceMenu(R.menu.edit_task_menu) }
        }).start()
        isAdd = false
    }


    fun onNavItemSelected(isSync: Boolean){
        progressBarMain.visibility = View.VISIBLE
        progressBarMain.playAnimation()
        completedBtnLayout.visibility = View.GONE
        tasksListGroup.visibility = View.GONE
        completedTasksListGroup.visibility = View.GONE
        isCompletedListGroupExpanded = false
        list_name_text.text = data.title
        when (isSync){
            false -> initTasks()
            true -> initAllFirebaseTasks()
        }
    }

    private fun initTasksList(){
        mTaskLists.clear()

        val taskLists = realm
            .where<TaskList>()
            .findAll()
        taskLists.forEach {
            mTaskLists.add(it)
        }
    }

    fun createNewTasksList(){
        val intent = Intent(this,AddTasksListActivity::class.java)
        intent.putExtra(INTENT_ADD_LIST_ID,data)
        startActivity(intent)
    }

    fun deleteTaskList(){
        if (data.title != "My Tasks") {
            val title = data.title
            val taskList = realm
                .where<TaskList>()
                .`in`("nameOfTaskList", arrayOf(title))
                .findFirst()
            val tasks = realm
                .where<Task>()
                .`in`("listTitle", arrayOf(title))
                .findAll()
            val completedTask = realm
                .where<CompletedTask>()
                .`in`("listTitle", arrayOf(title))
                .findAll()

            realm.executeTransaction{
                taskList!!.deleteFromRealm()
                tasks.deleteAllFromRealm()
                completedTask.deleteAllFromRealm()
            }

            fire.deleteTaskList(title)

            data.title = "My Tasks"
            data.listId = R.id.my_tasks_list.toLong()
            snacks.snack("List removed", Snackbar.LENGTH_LONG, R.color.colorBackSnackbar)
            initTasksList()
            onNavItemSelected(false)
        }
        else
            snacks.snack("You can't delete main list, I don't want it! ^—^", Snackbar.LENGTH_LONG, R.color.colorBackSnackbar)
    }

    private fun initTasks(){
        Log.w(TAG, "initTasks")
        val title = data.title
        list_name_text.text = title
        ///////////
        mTasks.clear()
        mCompletedTasks.clear()
        ////////////////////
        Log.w(TAG, "Список очищен")
        if (realm.where<Task>().findAll().isEmpty() && realm.where<CompletedTask>().findAll().isEmpty()){
            onNavItemSelected(true)
        }
        else {
            val sortedTasks = realm
                .where<Task>()
                .sort(Task::dateOfTasks.name, Sort.DESCENDING)
                .`in`("listTitle", arrayOf(title))
                .findAll()
            val sortedCompletedTasks = realm
                .where<CompletedTask>()
                .sort(CompletedTask::completionDateOfCompletedTask.name, Sort.DESCENDING)
                .`in`("listTitle", arrayOf(title))
                .findAll()

            Log.w(
                TAG,
                "sortedTasks.size -> ${sortedTasks.size} | sortedCompletedTasks.size -> ${sortedCompletedTasks.size}"
            )
            sortedTasks.forEach {
                mTasks.add(it)
            }
            sortedCompletedTasks.forEach {
                mCompletedTasks.add(it)
            }
            Log.w(TAG, "mTasks.size -> ${mTasks.size} | mCompletedTasks.size -> ${mCompletedTasks.size}")
            progressBarMain.cancelAnimation()
            progressBarMain.visibility = View.GONE
            completedTasksListGroupAdapter.notifyDataSetChanged()
            Log.w(TAG, "Обновление адаптера completedTaskList при инициализации таскс")
            taskListGroupAdapter.notifyDataSetChanged()
            Log.w(TAG, "Обновление адаптера taskList при инициализации таскс")
            showCompletedBtn()
        }
    }

    private fun initAllFirebaseTasks() {
        Log.w(TAG, "initAllFirebaseTasks")
        val title = data.title
        list_name_text.text = title
        ///////////
        mTasks.clear()
        mCompletedTasks.clear()
        mTaskLists.clear()
        realm.executeTransactionAsync {
            it.deleteAll()
        }
        ////////////////////
        Log.w(TAG, "Список очищен")
        fire.uIdDoc.collection("#$!@#$!@!@#$!@#!3123!@#").orderBy("date").get().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w(TAG, "MainActivity | successful get documents of lists")
                if (it.result!!.documents.isNotEmpty()) {
                    Log.w(TAG, "MainActivity | documents isn't empty")
                    for ((i) in (0 until it.result!!.documents.size).withIndex()) {
                        val doc = it.result!!.documents[i]
                        val taskList = TaskList(doc["name"] as String, doc["id"] as Long)
                        mTaskLists.add(taskList)
                        realm.executeTransaction { r ->
                            r.insert(taskList)
                        }
                    }
                }
            }
            val tasksList = ArrayList<String>()
            mTaskLists.forEach { list ->
                tasksList.add(list.nameOfTaskList)
            }
            tasksList.add("My Tasks")


            for (list in 0 until tasksList.size) {
                fire.uIdDoc.collection(tasksList[list]).orderBy("date", Query.Direction.DESCENDING).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.w(TAG, "it is successful order by date")
                            for ((i) in (0 until it.result!!.size()).withIndex()) {
                                val doc = it.result!!.documents[i]
                                if (doc.get("key") == false) {
                                    //Log.w(TAG,"Внутри условия при key == false")
                                    //Log.w(TAG,"notification_date TYPE -> ${test!!::class.simpleName}")
                                    val task = Task(
                                        doc.get("text") as String,
                                        doc.get("details_text") as String,
                                        doc.get("date"),
                                        doc.get("notification_date"),
                                        doc.id,
                                        tasksList[list]
                                    )
                                    if (tasksList[list] == title)
                                        mTasks.add(task)
                                    realm.executeTransaction { r ->
                                        r.insert(task)
                                    }
                                    //Log.w(TAG,"mTasks[i] -> ${mTasks[i]}")
                                }
                            }

                            fire.uIdDoc.collection(tasksList[list])
                                .orderBy("completion_date", Query.Direction.DESCENDING).get().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Log.w(TAG, "it is successful order by completion_date")
                                    for ((i) in (0 until it.result!!.size()).withIndex()) {
                                        //Log.w(TAG,"initAllFirebaseTasks | Внутри for")
                                        val doc = it.result!!.documents[i]
                                        if (doc.get("key") == true) {
                                            //Log.w(TAG,"Внутри условия при key == true")
                                            val completedTask = CompletedTask(
                                                doc.get("text") as String,
                                                doc.get("details_text") as String,
                                                doc.get("date"),
                                                doc.get("completion_date"),
                                                doc.get("notification_date"),
                                                doc.id,
                                                tasksList[list]
                                            )
                                            if (tasksList[list] == title)
                                                mCompletedTasks.add(completedTask)
                                            realm.executeTransaction { r ->
                                                r.insert(completedTask)
                                            }
                                            //Log.w(TAG,"mCompletedTasks[i] -> ${mCompletedTasks[i]}")
                                        }
                                    }
                                    when (list) {
                                        tasksList.size - 1 -> {
                                            Log.w(
                                                TAG, "realm task -> ${realm.where<Task>().findAll().size} " +
                                                        "| realm completed -> ${realm.where<CompletedTask>().findAll().size}"
                                            )
                                            progressBarMain.cancelAnimation()
                                            progressBarMain.visibility = View.GONE
                                            completedTasksListGroupAdapter.notifyDataSetChanged()
                                            Log.w(TAG, "Обновление адаптера completedTaskList при инициализации таскс")
                                            taskListGroupAdapter.notifyDataSetChanged()
                                            Log.w(TAG, "Обновление адаптера taskList при инициализации таскс")
                                            showCompletedBtn()
                                        }
                                    }


                                } else {
                                    Log.w(TAG, "ERROR initAllFirebaseTasks (by completion_date)  -> ${it.exception}")
                                }

                            }
                        } else {
                            Log.w(TAG, "ERROR initAllFirebaseTasks (by date) -> ${it.exception}")
                            progressBarMain.cancelAnimation()
                            progressBarMain.visibility = View.GONE
                            snacks.snack(
                                "Something went wrong!",
                                Snackbar.LENGTH_LONG,
                                R.color.colorBackSnackbar,
                                "ERROR",
                                R.color.colorErrorActionBar
                            )
                        }
                    }
            }

        }
    }

    private fun initRecyclerView(){
        Log.w(TAG, "initRecyclerView")
        taskListGroupAdapter = TasksRecyclerViewAdapter(
            this,
            this@MainActivity,
            mTasks,
            realm,
            data,
            snacks,
            object : AdapterInterface {
                override fun taskNotCompleteImageViewOnClick(
                    task: Task,
                    completionDate: Any?,
                    position: Int
                ) {
                    completedTasksListGroupAdapter.addTask(task, completionDate) // add task to completedTasksListGroup (0 position)
                    showCompletedBtnWhenTaskMoved()
                    snacks.completedSnack(
                        "Task add to completed",
                        Snackbar.LENGTH_LONG,
                        R.color.colorBackSnackbar,
                        "UNDO",
                        R.color.colorUNDOActionSnackbar,
                        task.map(),
                        false,
                        task.id,
                        task.notificationId,
                        position
                    )

                }

                override fun taskTextViewOnClick(
                    taskText: String?,
                    taskDetailsText: String?,
                    notificationDate: Any?,
                    position: Int
                ) {
                    slideBarUp()
                    updateUIWhenTaskTextIsClicked(taskText, taskDetailsText, notificationDate)
                    data.position = position
                    data.taskText = taskText
                    data.taskDetailsText = taskDetailsText
                    data.notificationDate = notificationDate
                    isChange = true
                }
            }
        )
        tasksListGroup.apply {
            itemAnimator = SlideInDownAnimator(OvershootInterpolator())
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        tasksListGroup.adapter = AlphaInAnimationAdapter(taskListGroupAdapter).apply {
            setDuration(500)
            setFirstOnly(false)
        }
        tasksListGroup.itemAnimator?.apply {
            addDuration = 300
            changeDuration = 300
            removeDuration = 300
            moveDuration = 300
        }
        //////////////////////////////////////////////////////////////////////
        completedTasksListGroupAdapter = CompletedTasksRecyclerViewAdapter(
                    this,
                    mCompletedTasks,
                    realm,
                    data,
                    snacks,
                    object : CompletedAdapterInterface {
                        override fun taskCompletedImageViewOnClick(
                            completedTask: CompletedTask,
                            task: Task,
                            position: Int
                        ) {
                            taskListGroupAdapter.moveTaskFromCompleted(task,0) // add task to TasksListGroup
                            showCompletedBtnWhenTaskMoved()
                            snacks.completedSnack(
                                "Task add to Not Completed",
                                Snackbar.LENGTH_LONG,
                                R.color.colorBackSnackbar,
                                "UNDO",
                                R.color.colorUNDOActionSnackbar,
                                completedTask.map(),
                                true,
                                completedTask.id,
                                null,
                                position
                            )
                        }

                        override fun completedTaskTextViewOnClick(
                            taskText: String?,
                            taskDetailsText: String?,
                            notificationDate: Any?,
                            position: Int
                        ) {
                            slideBarUp()
                            updateUIWhenCompletedTaskTextIsClicked(taskText,taskDetailsText,notificationDate)
                            isChange = true
                            isCompleted = true
                            data.position = position
                        }

                    }
                )
        completedTasksListGroup.apply {
            itemAnimator = LandingAnimator()
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        completedTasksListGroup.adapter = AlphaInAnimationAdapter(completedTasksListGroupAdapter).apply {
            setDuration(500)
            setFirstOnly(false)
        }
        completedTasksListGroup.itemAnimator?.apply {
            addDuration = 300
            changeDuration = 300
            removeDuration = 300
            moveDuration = 300
        }
        Log.w(TAG, "initRecyclerView done")

    }

    override fun onBackPressed() {
        isChange = false
        when (isAdd) {
            false -> updateBarUI(isAdd)
            true -> super.onBackPressed()
        }
        slideBarUp()
    }


    private fun showCompletedBtn(){
        tasksListGroup.visibility = View.VISIBLE
        if(!mCompletedTasks.isEmpty()){
            setBottomMargin(tasksListGroup, 0f)
            underBtnView.visibility = View.VISIBLE
            completedBtnLayout.visibility = View.VISIBLE
            completedBtn.icon = ResourcesCompat.getDrawable(resources, R.drawable.avd_arrow_down_to_up, null)
            completedBtn.text = "Completed (${mCompletedTasks.size})"
            Log.w(TAG,"showCompletedBtn if completed")
        }
        else{
            completedBtnLayout.visibility = View.GONE
            setBottomMargin(tasksListGroup, 90f)
        }
    }

    fun showCompletedBtnWhenTaskMoved(){
        Log.w(TAG, "isExpanded -> $isCompletedListGroupExpanded")
        if (!mCompletedTasks.isEmpty()){
            completedBtnLayout.visibility = View.VISIBLE
            setBottomMargin(tasksListGroup, 0f)
            if (isCompletedListGroupExpanded) {
                completedBtn.icon = ResourcesCompat.getDrawable(resources, R.drawable.avd_arrow_up_to_down, null)
                underBtnView.visibility = View.GONE
            }
            else {
                completedBtn.icon = ResourcesCompat.getDrawable(resources, R.drawable.avd_arrow_down_to_up, null)
                underBtnView.visibility = View.VISIBLE
            }
            completedBtn.text = "Completed (${mCompletedTasks.size})"
        }
        else{
            completedBtnLayout.visibility = View.GONE
            completedTasksListGroup.visibility = View.GONE
            setBottomMargin(tasksListGroup, 90f)
            isCompletedListGroupExpanded = false
        }
    }

    private fun setBottomMargin(view: View, marginInDp: Float){
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            marginInDp,
            resources.displayMetrics
        )
        val params = view.layoutParams as LinearLayout.LayoutParams
        params.bottomMargin = px.toInt()
        view.layoutParams = params
    }

    fun setInitialDateTime() {
        val dateFormat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
        //chipAddDate.text = (DateUtils.formatDateTime(this,dateAndTime.timeInMillis,DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME))
        chipAddDate.text = dateFormat.format(dateAndTime.time)
    }

    private fun completedBtnOnClick(){
        when (isCompletedListGroupExpanded) {
            false -> {
                Log.w(TAG, "false")
                completedTasksListGroup.visibility = View.VISIBLE
                completedBtn.icon = ResourcesCompat.getDrawable(resources, R.drawable.avd_arrow_down_to_up, null)
                underBtnView.visibility = View.GONE
                val icon = completedBtn.icon as AnimatedVectorDrawable
                icon.start()
                Thread(Runnable {
                    completedTasksListGroupAdapter.onCompletedBtn()
                    handler.post {
                        completedTasksListGroup.animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                completedTasksListGroup.clearAnimation()
                            }
                        })
                    }
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post {
                        val anim = ValueAnimator.ofInt(0, 40)
                        anim.addUpdateListener {
                            val v = it.animatedValue as Int
                            contentScrollView.smoothScrollBy(0, v)
                        }
                        anim.duration = 200
                        anim.interpolator = AccelerateDecelerateInterpolator()
                        anim.start()
                    }
                }).start()
            }
            true -> {
                Log.w(TAG, "true")
                completedBtn.icon = ResourcesCompat.getDrawable(resources,R.drawable.avd_arrow_up_to_down,null)
                val icon = completedBtn.icon as AnimatedVectorDrawable
                icon.start()
                Thread(Runnable {
                    handler.post {
                        completedTasksListGroupAdapter.onCompletedBtn()
                        completedTasksListGroup.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                completedTasksListGroup.clearAnimation()
                            }
                        })
                    }
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post {
                        val y = if(completedTasksListGroup.y - 1200 < 0) 0 else (completedTasksListGroup.y - 1200).toInt()
                        val currentScrollY = contentScrollView.scrollY
                        if ((currentScrollY-y)/2 > 0){
                            val anim = ValueAnimator.ofInt(currentScrollY, y)
                            anim.addUpdateListener {
                                val v = it.animatedValue as Int
                                contentScrollView.smoothScrollTo(0, v)
                                if (v == y){
                                    underBtnView.visibility = View.VISIBLE
                                    completedTasksListGroup.visibility = View.GONE
                                }
                            }
                            anim.duration = (currentScrollY-y)/2.toLong()
                            anim.interpolator = AccelerateDecelerateInterpolator()
                            anim.start()
                        }
                        else{
                            underBtnView.visibility = View.VISIBLE
                            completedTasksListGroup.visibility = View.GONE
                            }

                    }
                }).start()

            }
        }
        isCompletedListGroupExpanded = !isCompletedListGroupExpanded
    }

    fun setChipChecked(isChecked: Boolean){
        chipAddDate.isChecked = isChecked
    }

    private fun slideBarUp(){
        isBarAnimateEnd = false
        bar.animate()
            .translationY(0f)
            .setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
            .setDuration(225)
            .setListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    bar.clearAnimation()
                }
            })

        fab.animate()
            .translationY(-100f)
            .setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
            .setDuration(225)
            .setListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    fab.clearAnimation()
                    isBarAnimateEnd = true
                }
            })
    }

    private fun slideBarDown(){
        isBarAnimateEnd = false
        val fabContentRect = Rect()
        fab.getContentRect(fabContentRect)
        val fabShadowPadding: Float = fab.measuredHeight - fabContentRect.height().toFloat()
        bar.animate()
            .translationY(170f)
            .setInterpolator(FAST_OUT_LINEAR_IN_INTERPOLATOR)
            .setDuration(175)
            .setListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    bar.clearAnimation()
                }
            })

        fab.animate()
            .translationY(-fab.paddingBottom + fabShadowPadding)
            .setInterpolator(FAST_OUT_LINEAR_IN_INTERPOLATOR)
            .setDuration(175)
            .setListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    fab.clearAnimation()
                    isBarAnimateEnd = true
                }
            })

    }

    private fun setRefreshLayoutColor(){
        refreshLayout.setColorSchemeResources(R.color.colorAccent)
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorBottomAppBar)
    }

    val snacks = object : Snacks{
        override fun snack(text: String, duration: Int, backColor: Int) {
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

        override fun snack(text: String, duration: Int, backColor: Int, actionText: String, actionColor: Int) {
            val screenSize = Point()
            windowManager.defaultDisplay.getSize(screenSize)
            val marginSide = 0
            val marginBottom: Int = (screenSize.y / 6.4).toInt()
            snackbar = Snackbar.make(
                snackbarLayout,
                text,
                duration)
            snackbar.setAction(actionText) { snackbar.dismiss() }
            snackbar.setActionTextColor(ResourcesCompat.getColor(resources,actionColor,null))
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

        override fun removedSnack(
            text: String,
            duration: Int,
            backColor: Int,
            actionText: String,
            actionColor: Int,
            taskText: String?,
            taskDate: Any?,
            completionDate: Any?,
            taskDetailsText: String?,
            notificationDate: Any?,
            taskId: String?,
            key: Boolean,
            position: Int
        ) {
            val task: Any =
                if(!key)
                    Task(taskText,taskDetailsText,taskDate,notificationDate,taskId,data.title)
                else
                    CompletedTask(taskText,taskDetailsText,taskDate,completionDate,notificationDate,taskId, data.title)
            val screenSize = Point()
            windowManager.defaultDisplay.getSize(screenSize)
            val marginSide = 0
            val marginBottom: Int = (screenSize.y / 6.4).toInt()
            snackbar = Snackbar.make(
                snackbarLayout,
                text,
                duration)
            snackbar.setAction(actionText) {
                when (key){
                    true -> {completedTasksListGroupAdapter.addTask(task,position)}
                    false -> {taskListGroupAdapter.addTask(task, position)}
                }
                Log.w(TAG,"Кнопка UNDO нажата, таска вернулась из Deleted суксесфул")
                snackbar.dismiss()
                Log.w(TAG,"Snackbar dismiss после UNDO")
            }
            snackbar.setActionTextColor(ResourcesCompat.getColor(resources,actionColor,null))
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

        override fun completedSnack(
            text: String,
            duration: Int,
            backColor: Int,
            actionText: String,
            actionColor: Int,
            map: Map<String, Any?>,
            key: Boolean,
            id: String?,
            notificationId: Int?,
            position: Int
        ) {
            val screenSize = Point()
            windowManager.defaultDisplay.getSize(screenSize)
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                95f,
                resources.displayMetrics
            )
            val marginSide = 0
            val marginBottom: Int = px.toInt()
            //Log.w(TAG,"screenSize.x -> ${screenSize.x}, screenSize.y -> ${screenSize.y}")
            snackbar = Snackbar.make(
                snackbarLayout,
                text,
                duration)
            //Log.w(TAG, "completedSnack | map -> $map")
            snackbar.setAction(actionText) {
                when (key) {
                    false -> {
                        completedTasksListGroupAdapter.moveTaskToNotCompleted(0) // delete task from completedTasksListGroup
                        val task = Task(map)
                        task.id = id
                        task.notificationId = notificationId
                        task.listTitle = data.title
                        if (notificationId != null)
                            NotificationUtils().setNotification(task, data.title, task.notificationDateOfTask!!.time, this@MainActivity)
                        val movedTask = realm
                            .where<CompletedTask>()
                            .`in`("id", arrayOf(id))
                            .findFirst()
                        realm.executeTransaction { r ->
                            movedTask!!.deleteFromRealm()
                            r.insert(task)
                        }
                        taskListGroupAdapter.moveTaskFromCompleted(task, position) // add task to TasksListGroup
                        showCompletedBtnWhenTaskMoved()
                        fire.addTaskToNotCompleted(map, id)
                    }
                    true -> {
                        //Log.w(TAG, "completedSnack | key = true | map -> $map")
                        taskListGroupAdapter.moveTaskToCompleted(0) // delete task from TasksListGroup
                        val completedTask = CompletedTask(map)
                        completedTask.id = id
                        completedTask.listTitle = data.title
                        val movedTask = realm
                            .where<Task>()
                            .`in`("id", arrayOf(id))
                            .findFirst()
                        realm.executeTransaction {
                            movedTask!!.deleteFromRealm()
                        }
                        completedTasksListGroupAdapter.addTask(completedTask, position)
                        showCompletedBtnWhenTaskMoved()
                        fire.addTaskToCompleted(id,map["completion_date"])
                    }
                }

                snackbar.dismiss()
            }
            snackbar.setActionTextColor(ResourcesCompat.getColor(resources,actionColor,null))
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

    }

    val callback = object : AuthenticationCallback {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {

        }

        override fun onAuthenticationSucceeded() {
            when(isDeleteTaskOrList){
                true -> deleteTaskList()
                false -> {
                    when{
                        isCompleted -> { completedTasksListGroupAdapter.deleteCompletedTask(data.position, showCompletionBtnWhenDeleted) }
                        !isCompleted -> { taskListGroupAdapter.deleteTask(data.position,snacks) }
                    }
                    updateBarUI(false)
                }
            }
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {

        }

        override fun onAuthenticationFailed() {

        }

        override fun fingerprintAuthenticationNotSupported() {
            when(isDeleteTaskOrList) {
                true -> deleteTaskList()
                false -> {
                    when{
                        isCompleted -> { completedTasksListGroupAdapter.deleteCompletedTask(data.position, showCompletionBtnWhenDeleted) }
                        !isCompleted -> { taskListGroupAdapter.deleteTask(data.position,snacks) }
                    }
                    updateBarUI(false)
                }
            }

        }

        override fun hasNoFingerprintEnrolled() {
            when(isDeleteTaskOrList) {
                true -> deleteTaskList()
                false -> {
                    when{
                        isCompleted -> { completedTasksListGroupAdapter.deleteCompletedTask(data.position, showCompletionBtnWhenDeleted) }
                        !isCompleted -> { taskListGroupAdapter.deleteTask(data.position,snacks) }
                    }
                    updateBarUI(false)
                }
            }
        }

        override fun authenticationCanceledByUser() {

        }

    }

    val deleteTasksCallback = object : AuthenticationCallback {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {

        }

        override fun onAuthenticationSucceeded() {
            completedTasksListGroupAdapter.deleteAllCompletedTasks()
            showCompletedBtnWhenTaskMoved()
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {

        }

        override fun onAuthenticationFailed() {

        }

        override fun fingerprintAuthenticationNotSupported() {
            completedTasksListGroupAdapter.deleteAllCompletedTasks()
            showCompletedBtnWhenTaskMoved()

        }

        override fun hasNoFingerprintEnrolled() {
            completedTasksListGroupAdapter.deleteAllCompletedTasks()
            showCompletedBtnWhenTaskMoved()
        }

        override fun authenticationCanceledByUser() {

        }

    }

    val showCompletionBtnWhenDeleted = object : DeleteCompletedTaskAdapterInterface{

        override fun showCompletionBtnWhenCompletedTaskDeleted() {
            showCompletedBtnWhenTaskMoved()
        }
    }

}
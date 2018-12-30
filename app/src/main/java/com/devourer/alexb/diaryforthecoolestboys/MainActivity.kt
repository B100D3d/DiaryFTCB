package com.devourer.alexb.diaryforthecoolestboys

import android.app.FragmentManager
import android.content.Context
import android.content.Intent
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
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.devourer.alexb.diaryforthecoolestboys.Fragments.BottomNavigationDrawerFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.devourer.alexb.diaryforthecoolestboys.FingerprintLibrary.AuthenticationCallback
import com.google.android.material.animation.AnimationUtils.*
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


class MainActivity : AppCompatActivity() {



    private val mAuth = FirebaseAuth.getInstance()
    private val myDB = FirebaseFirestore.getInstance()
    lateinit var fire: MyFirebase
    var dateAndTime = Calendar.getInstance()!!
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var snackbar: Snackbar
    private var isAdd: Boolean = true
    private var isChange: Boolean = false
    var isDeleteTaskOrList = false // false — task | true — list
    private var isCompletedListGroupExpanded = false
    private val handler = Handler()
    var mFragmentManager: FragmentManager = fragmentManager
    private val uId = mAuth.uid!!
    private val user = mAuth.currentUser!!
    val name = user.displayName!!
    private var mTasks = ArrayList<Task>()
    private var mCompletedTasks = ArrayList<CompletedTask>()
    var mTaskLists = ArrayList<TaskList>()
    private lateinit var taskListGroupAdapter: TasksRecyclerViewAdapter
    lateinit var completedTasksListGroupAdapter: CompletedTasksRecyclerViewAdapter
    //Database links
    private val users = myDB.collection("users")
    private val uIdDoc = users.document(uId)
    //
    companion object {
        private const val TAG: String = "Main"
        const val INTENT_ID = "auth"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w(TAG,"onCreate")
        setContentView(R.layout.activity_main)
        setSupportActionBar(bar)
        fire = MyFirebase(this@MainActivity)
        initTasksList()
        onNavItemSelected(NavMenuCheckedItem.title)

        if (intent.getBooleanExtra(INTENT_ID,false))
            snacks.snack("Authentication access!",Snackbar.LENGTH_LONG,R.color.colorBackSnackbar,"OK",R.color.colorOkActionSnackbar)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        fab.setOnClickListener {
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
                    taskListGroupAdapter.changeTask(
                        taskEditText.text.toString(),
                        taskDetailsEditText.text.toString(),
                        chipAddDate.text.toString(),
                        NavMenuCheckedItem.notificationDate,
                        dateAndTime,
                        NavMenuCheckedItem.position,
                        snacks
                    )
                    isChange = false
                    updateBarUI(false)
                }
            }
        }

        chipAddDate.onCheckedChangeListener = { view: CheckableChipView, isChecked: Boolean ->
            when {
                (isChecked && !isAdd) -> {
                    taskEditText.requestFocus()
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

        taskDetailsEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                editScrollView.fullScroll(View.FOCUS_DOWN)
                taskDetailsEditText.requestFocus()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editScrollView.fullScroll(View.FOCUS_DOWN)
                taskDetailsEditText.requestFocus()
            }

        })


        chipAddDetails.onCheckedChangeListener = { view: CheckableChipView, isChecked: Boolean ->
            when {
                (isChecked && !isAdd) -> {
                    taskDetailsEditTextInputLayout.visibility = View.VISIBLE
                    taskDetailsEditText.requestFocus()
                    Thread(Runnable {
                        TimeUnit.MILLISECONDS.sleep(300)
                        handler.post {  val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

        completedBtn.setOnClickListener { onCompletedBtn() }

        contentScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            when {
                scrollY > oldScrollY -> slideBarDown()
                scrollY < oldScrollY -> slideBarUp()
            }
        }

        editScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            when {
                scrollY > oldScrollY -> slideBarDown()
                scrollY < oldScrollY -> slideBarUp()
            }
        }

    }

    override fun onStart() {
        super.onStart()
    }

    fun signOut() {
        // Firebase sign out
        mAuth.signOut()

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
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
                        taskListGroupAdapter.deleteTask(NavMenuCheckedItem.position,snacks)
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


    private fun updateBarUI(b:Boolean){
        when (b) {
            true -> {
                progressBarMain.visibility = View.GONE
                headerView.visibility = View.INVISIBLE
                headerLineView.visibility = View.INVISIBLE
                contentScrollView.visibility = View.INVISIBLE
                fab.setImageDrawable(getDrawable(R.drawable.avd_add_to_done))
                val icon = fab.drawable
                if(icon is AnimatedVectorDrawable)
                    icon.start()
                taskEditTextLayout.visibility = View.VISIBLE
                bar.navigationIcon = null
                bar.replaceMenu(R.menu.empty_menu)
                Thread(Runnable {
                    handler.post {bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END}
                    //handler.post { fab.setImageDrawable(getDrawable(R.drawable.ic_done_white_24dp)) }
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post {taskEditText.requestFocus()}
                    handler.post { bar.replaceMenu(R.menu.add_task_menu) }
                    handler.post {  val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(taskEditText, InputMethodManager.SHOW_IMPLICIT) }
                }).start()
                isAdd = false
            }
            false -> {
                taskEditText.setText("")
                taskDetailsEditText.setText("")
                chipAddDate.isChecked = false
                chipAddDetails.isChecked = false
                chipAddDate.text = resources.getText(R.string.add_date)
                headerView.visibility = View.VISIBLE
                headerLineView.visibility = View.VISIBLE
                contentScrollView.visibility = View.VISIBLE
                taskDetailsEditTextInputLayout.visibility = View.INVISIBLE
                taskEditTextLayout.visibility = View.INVISIBLE
                fab.setImageDrawable(getDrawable(R.drawable.avd_done_to_add))
                val icon = fab.drawable
                if(icon is AnimatedVectorDrawable)
                    icon.start()
                bar.replaceMenu(R.menu.empty_menu)
                Thread(Runnable {
                    handler.post {bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER}
                    //handler.post { fab.setImageDrawable(getDrawable(R.drawable.ic_add_white_24dp)) }
                    TimeUnit.MILLISECONDS.sleep(300)
                    handler.post { bar.replaceMenu(R.menu.main_menu) }
                    handler.post { bar.setNavigationIcon(R.drawable.ic_menu_white_24dp) }
                }).start()
                isAdd = true
            }
        }


    }

    private fun updateUIWhenTaskTextIsClicked(taskText: String, taskDetailsText: String?, _notificationDate: Any?){
        taskEditText.setText(taskText)
        val dateFormat = SimpleDateFormat("yyyy MMMM dd, h:mm a")
        val notificationDate = if (_notificationDate != null) dateFormat.format((_notificationDate as Date).time) else ""
        if (!taskDetailsText.isNullOrBlank()){
            chipAddDetails.isChecked = true
            taskEditText.requestFocus()
            taskDetailsEditTextInputLayout.visibility = View.VISIBLE
            taskDetailsEditText.setText(taskDetailsText)
        }
        if (!notificationDate.isNullOrBlank()){
            chipAddDate.isChecked = true
            chipAddDate.text = notificationDate
        }
        headerView.visibility = View.INVISIBLE
        headerLineView.visibility = View.INVISIBLE
        contentScrollView.visibility = View.INVISIBLE
        taskEditTextLayout.visibility = View.VISIBLE
        bar.navigationIcon = null
        fab.setImageDrawable(getDrawable(R.drawable.avd_add_to_done))
        val icon = fab.drawable
        if(icon is AnimatedVectorDrawable)
            icon.start()
        bar.replaceMenu(R.menu.empty_menu)
        Thread(Runnable {
            handler.post {bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END}
            TimeUnit.MILLISECONDS.sleep(300)
            handler.post { bar.replaceMenu(R.menu.edit_task_menu) }
        }).start()
        isAdd = false
    }


    fun onNavItemSelected(title: String){
        progressBarMain.visibility = View.VISIBLE
        completedBtnLayout.visibility = View.GONE
        val params = completedTasksListGroup.layoutParams
        params.height = 0
        setBottomMargin(tasksListGroup,90f)
        initRecyclerView()
        list_name_text.text = title
        initTasks(title)
    }

    private fun initTasksList(){
        mTaskLists.clear()
        uIdDoc.collection("#$!@#$!@!@#$!@#!3123!@#").orderBy("date").get().addOnCompleteListener {
            if (it.isSuccessful){
                Log.w(TAG,"MainActivity | successful get documents of lists")
                if (it.result!!.documents.isNotEmpty()) {
                    Log.w(TAG,"MainActivity | documents isn't empty")
                    for ((i) in (0 until it.result!!.documents.size).withIndex()){
                        val doc = it.result!!.documents[i]
                        mTaskLists.add(TaskList(doc["name"] as String, doc["id"] as Long))
                    }
                }
            }
        }
    }

    fun createNewTasksList(){
        val intent = Intent(this,AddTasksListActivity::class.java)
        startActivity(intent)
    }

    fun deleteTaskList(){
        if (NavMenuCheckedItem.title != "My Tasks") {
            val title = NavMenuCheckedItem.title
            uIdDoc.collection("#$!@#$!@!@#$!@#!3123!@#").document(title).delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        NavMenuCheckedItem.title = "My Tasks"
                        NavMenuCheckedItem.id = R.id.my_tasks_list.toLong()
                        snacks.snack("List removed", Snackbar.LENGTH_LONG, R.color.colorBackSnackbar)
                        initRecyclerView()
                        completedBtnLayout.visibility = View.GONE
                        progressBarMain.visibility = View.VISIBLE
                        initTasksList()
                        initTasks(NavMenuCheckedItem.title)

                    }
                }
            uIdDoc.collection(title).get().addOnCompleteListener {
                if(it.isSuccessful){
                    it.result!!.documents.forEach {
                        it.reference.delete()
                    }
                }
            }
        }
        else
            snacks.snack("You can't delete main list, I don't want it!", Snackbar.LENGTH_LONG, R.color.colorBackSnackbar)
    }

    private fun initTasks(title: String){
        list_name_text.text = NavMenuCheckedItem.title
        ///////////
        mTasks.clear()
        mCompletedTasks.clear()
        ////////////////////
        Log.w(TAG, "Список очищен")

        uIdDoc.collection(title).orderBy("date",Query.Direction.DESCENDING).get().addOnCompleteListener {
            if(it.isSuccessful){
                Log.w(TAG, "it is successful order by date")
                for ((i) in (0 until it.result!!.size()).withIndex()){
                    val doc = it.result!!.documents[i]
                    if (doc.get("key") == false){
                        //Log.w(TAG,"Внутри условия при key == false")
                        //Log.w(TAG,"notification_date TYPE -> ${test!!::class.simpleName}")
                        mTasks.add(Task(
                            doc.get("text") as String,
                            doc.get("details_text") as String,
                            doc.get("date"),
                            doc.get("notification_date"),
                            doc.id
                        ))
                        //Log.w(TAG,"mTasks[i] -> ${mTasks[i]}")
                    }
                }

                uIdDoc.collection(title).orderBy("completion_date",Query.Direction.DESCENDING).get().addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.w(TAG, "it is successful order by completion_date")
                        for ((i) in (0 until it.result!!.size()).withIndex()){
                            //Log.w(TAG,"initTasks | Внутри for")
                            val doc = it.result!!.documents[i]
                            if (doc.get("key") == true){
                                //Log.w(TAG,"Внутри условия при key == true")
                                mCompletedTasks.add(CompletedTask(
                                    doc.get("text") as String,
                                    doc.get("details_text") as String,
                                    doc.get("date"),
                                    doc.get("completion_date"),
                                    doc.get("notification_date"),
                                    doc.id
                                ))
                                //Log.w(TAG,"mCompletedTasks[i] -> ${mCompletedTasks[i]}")
                            }
                        }

                        progressBarMain.visibility = View.INVISIBLE
                        showCompletedBtn()
                        completedTasksListGroupAdapter.notifyDataSetChanged()
                        Log.w(TAG,"Обновление адаптера completedTaskList при инициализации таскс")
                        taskListGroupAdapter.notifyDataSetChanged()
                        Log.w(TAG,"Обновление адаптера taskList при инициализации таскс")
                    }
                    else{
                        Log.w(TAG, "ERROR initTasks (by completion_date)  -> ${it.exception}")
                    }

                }
            }
            else{
                Log.w(TAG, "ERROR initTasks (by date) -> ${it.exception}")
                progressBarMain.visibility = View.INVISIBLE
                snacks.snack("Something went wrong!",Snackbar.LENGTH_LONG,R.color.colorBackSnackbar,"ERROR",R.color.colorErrorActionBar)
            }


        }

    }

    private fun initRecyclerView(){
        taskListGroupAdapter = TasksRecyclerViewAdapter(
            this,
            mTasks,
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
                        task.map,
                        false,
                        task.id,
                        position
                    )

                }

                override fun taskTextViewOnClick(
                    taskText: String,
                    taskDetailsText: String,
                    notificationDate: Any?,
                    position: Int
                ) {
                    slideBarUp()
                    updateUIWhenTaskTextIsClicked(taskText, taskDetailsText, notificationDate)
                    NavMenuCheckedItem.position = position
                    NavMenuCheckedItem.taskText = taskText
                    NavMenuCheckedItem.taskDetailsText = taskDetailsText
                    NavMenuCheckedItem.notificationDate = notificationDate
                    isChange = true
                }
            }
        )
        tasksListGroup.apply {
            itemAnimator = SlideInDownAnimator()
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        tasksListGroup.adapter = AlphaInAnimationAdapter(SlideInBottomAnimationAdapter(taskListGroupAdapter)).apply {
            setDuration(500)
            setFirstOnly(false)
        }
        tasksListGroup.itemAnimator?.apply {
            addDuration = 300
            changeDuration = 300
            removeDuration = 300
            moveDuration = 300
        }
        completedTasksListGroupAdapter =
                CompletedTasksRecyclerViewAdapter(
                    this,
                    mCompletedTasks,
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
                                completedTask.map,
                                true,
                                completedTask.id,
                                position
                            )
                        }

                    }
                )
        completedTasksListGroup.apply {
            itemAnimator = SlideInUpAnimator()
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        completedTasksListGroup.adapter = AlphaInAnimationAdapter(SlideInBottomAnimationAdapter(completedTasksListGroupAdapter)).apply {
            setDuration(500)
            setFirstOnly(false)
        }
        completedTasksListGroup.itemAnimator?.apply {
            addDuration = 300
            changeDuration = 300
            removeDuration = 300
            moveDuration = 300
        }

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
        if(!mCompletedTasks.isEmpty()){
            completedBtnLayout.visibility = View.VISIBLE
            underBtnView.visibility = View.VISIBLE
            completedBtn.icon = ResourcesCompat.getDrawable(resources, R.drawable.avd_arrow_down_to_up, null)
            completedBtn.text = "Completed (${mCompletedTasks.size})"
            setBottomMargin(tasksListGroup,0f)
            setBottomMargin(completedTasksListGroup,90f)
            Log.w(TAG,"showCompletedBtn if completed")
        }
        else{
            completedBtnLayout.visibility = View.GONE
            val params = completedTasksListGroup.layoutParams
            params.height = 0
            setBottomMargin(tasksListGroup,90f)
            setBottomMargin(completedTasksListGroup, 0f)

        }
    }

    fun showCompletedBtnWhenTaskMoved(){
        if (!mCompletedTasks.isEmpty()){
            completedBtnLayout.visibility = View.VISIBLE
            if (isCompletedListGroupExpanded)
                underBtnView.visibility = View.GONE
            else
                underBtnView.visibility = View.VISIBLE
            completedBtn.text = "Completed (${mCompletedTasks.size})"
            setBottomMargin(tasksListGroup,0f)
            setBottomMargin(completedTasksListGroup,90f)
        }
        else{
            completedBtnLayout.visibility = View.GONE
            //completedTasksListGroup.visibility = View.GONE
            val params = completedTasksListGroup.layoutParams
            params.height = 0
            setBottomMargin(tasksListGroup,90f)
            setBottomMargin(completedTasksListGroup, 0f)
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

    private fun onCompletedBtn(){
        val params = completedTasksListGroup.layoutParams
        when (params.height) {
            0 -> {
                completedBtn.icon = ResourcesCompat.getDrawable(resources,R.drawable.avd_arrow_down_to_up,null)
                underBtnView.visibility = View.GONE
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                underBtnView.visibility = View.VISIBLE
                params.height = 0
                completedBtn.icon = ResourcesCompat.getDrawable(resources,R.drawable.avd_arrow_up_to_down,null)
            }
        }

        val icon = completedBtn.icon as AnimatedVectorDrawable
        icon.start()
        completedTasksListGroupAdapter.onCompletedBtn()
        isCompletedListGroupExpanded = !isCompletedListGroupExpanded


    }

    fun setChipChecked(isChecked: Boolean){
        chipAddDate.isChecked = isChecked
    }

    private fun slideBarUp(){
        bar.clearAnimation()
        fab.clearAnimation()
        bar.animate()
            .translationY(0f)
            .setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
            .duration = 225

        fab.animate()
            .translationY(-100f)
            .setInterpolator(LINEAR_OUT_SLOW_IN_INTERPOLATOR)
            .duration = 225
    }

    private fun slideBarDown(){
        bar.clearAnimation()
        fab.clearAnimation()
        val fabContentRect = Rect()
        fab.getContentRect(fabContentRect)
        val fabShadowPadding: Float = fab.measuredHeight - fabContentRect.height().toFloat()
        bar.animate()
            .translationY(170f)
            .setInterpolator(FAST_OUT_LINEAR_IN_INTERPOLATOR)
            .duration = 175

        fab.animate()
            .translationY(-fab.paddingBottom + fabShadowPadding)
            .setInterpolator(FAST_OUT_LINEAR_IN_INTERPOLATOR)
            .duration = 175

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
            taskText: String,
            taskDate: Any?,
            taskDetailsText: String,
            notificationDate: Any?,
            taskId: String,
            key: Boolean,
            position: Int
        ) {
            val task = Task(taskText,taskDetailsText,taskDate,notificationDate,taskId)
            val screenSize = Point()
            windowManager.defaultDisplay.getSize(screenSize)
            val marginSide = 0
            val marginBottom: Int = (screenSize.y / 6.4).toInt()
            snackbar = Snackbar.make(
                snackbarLayout,
                text,
                duration)
            snackbar.setAction(actionText) {
                taskListGroupAdapter.addTask(task, position)
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
            id: String,
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
                        taskListGroupAdapter.moveTaskFromCompleted(task, position) // add task to TasksListGroup
                        showCompletedBtnWhenTaskMoved()
                        fire.addTaskToNotCompleted(map, id)
                    }
                    true -> {
                        //Log.w(TAG, "completedSnack | key = true | map -> $map")
                        taskListGroupAdapter.moveTaskToCompleted(0)
                        val completedTask = CompletedTask(map)
                        completedTask.id = id
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
                    taskListGroupAdapter.deleteTask(NavMenuCheckedItem.position,snacks)
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
                    taskListGroupAdapter.deleteTask(NavMenuCheckedItem.position,snacks)
                    updateBarUI(false)
                }
            }

        }

        override fun hasNoFingerprintEnrolled() {
            when(isDeleteTaskOrList) {
                true -> deleteTaskList()
                false -> {
                    taskListGroupAdapter.deleteTask(NavMenuCheckedItem.position,snacks)
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

}
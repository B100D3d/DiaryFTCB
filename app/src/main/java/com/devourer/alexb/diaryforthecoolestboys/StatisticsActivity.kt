package com.devourer.alexb.diaryforthecoolestboys

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.devourer.alexb.diaryforthecoolestboys.Adapters.StatisticsRecyclerViewAdapter
import io.realm.Realm
import io.realm.kotlin.where
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import kotlinx.android.synthetic.main.activity_statistics.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var statisticsGroupAdapter: StatisticsRecyclerViewAdapter
    private lateinit var realm: Realm
    private val mTasks = ArrayList<Map<String, Float>>()
    private val mCompletedTasks = ArrayList<Map<String, Float>>()
    private val mTaskLists = ArrayList<TaskList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        setupSharedPreferences()
        statisticsBackBtn.setOnClickListener {
            super.onBackPressed()
        }
        realm = Realm.getDefaultInstance()
        getLists()
        getTasks()
        initRecyclerView()

    }

    private fun initRecyclerView(){
        statisticsGroupAdapter = StatisticsRecyclerViewAdapter(
            this,
            mTasks,
            mCompletedTasks,
            mTaskLists
        )
        statisticsGroup.apply {
            itemAnimator = SlideInDownAnimator(OvershootInterpolator())
            layoutManager = GridLayoutManager(this@StatisticsActivity, 2)
        }
        statisticsGroup.adapter = AlphaInAnimationAdapter(statisticsGroupAdapter).apply {
            setDuration(500)
            setFirstOnly(false)
        }
        statisticsGroup.itemAnimator?.apply {
            addDuration = 300
            changeDuration = 300
            removeDuration = 300
            moveDuration = 300
        }

    }

    private fun getLists(){
        realm
            .where<TaskList>()
            .findAll()
            .forEach { mTaskLists.add(it) }
        mTaskLists.add(0, TaskList("My Tasks", intent.getIntExtra("lists", -1).toLong()))
    }

    private fun getTasks(){
        mTaskLists.forEach {
            val sortedTasks = realm
                .where<Task>()
                .`in`("listTitle", arrayOf(it.nameOfTaskList))
                .findAll()
            val sortedCompletedTasks = realm
                .where<CompletedTask>()
                .`in`("listTitle", arrayOf(it.nameOfTaskList))
                .findAll()
            mTasks.add(mapOf(
                "tasks_count" to sortedTasks.size.toFloat()
            ))
            mCompletedTasks.add(mapOf(
                "completed_tasks_count" to sortedCompletedTasks.size.toFloat()
            ))
        }
    }


    private fun setupSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!sharedPreferences.getBoolean("Show flakes", false))
            flakesView.visibility = View.GONE
        else flakesView.setImage(sharedPreferences)
    }
}

package com.devourer.alexb.diaryforthecoolestboys.Adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.devourer.alexb.diaryforthecoolestboys.R
import com.devourer.alexb.diaryforthecoolestboys.TaskList
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.ColorTemplate

class StatisticsRecyclerViewAdapter(
    _context: Context,
    _tasks: ArrayList<Map<String, Float>>,
    _completedTasks: ArrayList<Map<String, Float>>,
    _taskLists: ArrayList<TaskList>
) : RecyclerView.Adapter<StatisticsRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private const val TAG: String = "Main"
    }

    private val context: Context
    private val tasks: ArrayList<Map<String, Float>>
    private val completedTasks: ArrayList<Map<String, Float>>
    private val taskLists: ArrayList<TaskList>

    init {
        context = _context
        tasks = _tasks
        completedTasks = _completedTasks
        taskLists = _taskLists
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_statistics, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatisticsRecyclerViewAdapter.ViewHolder, position: Int) {

        holder.mChart.description.isEnabled = false
        holder.mChart.setUsePercentValues(false)
        holder.mChart.centerText = taskLists[position].nameOfTaskList
        if (taskLists[position].nameOfTaskList.length >= 20)
            holder.mChart.setCenterTextSize(8f)
        else
            holder.mChart.setCenterTextSize(15f)
        holder.mChart.setCenterTextColor(Color.WHITE)
        holder.mChart.setCenterTextTypeface(ResourcesCompat.getFont(context, R.font.google_sans_regular))
        holder.mChart.holeRadius = 50f
        holder.mChart.isDrawHoleEnabled = true
        holder.mChart.setHoleColor(ResourcesCompat.getColor(context.resources, R.color.colorMain, null))
        holder.mChart.setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.colorMain, null))
        holder.mChart.setTransparentCircleColor(ResourcesCompat.getColor(context.resources, R.color.colorMain, null))
        holder.mChart.setTransparentCircleAlpha(60)
        holder.mChart.setDrawCenterText(true)
        setData(holder, position)


    }

    override fun getItemCount(): Int {
        return taskLists.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var mChart: PieChart = itemView.findViewById(R.id.chart)
    }

    private fun getHighlightEntry(taskEntry: PieEntry, cTaskEntry: PieEntry) : PieEntry{
        return if (taskEntry.value > cTaskEntry.value) cTaskEntry
        else taskEntry
    }

    private fun setData(holder: StatisticsRecyclerViewAdapter.ViewHolder, position: Int) {
        val taskCount = tasks[position].getValue("tasks_count")
        val cTaskCount = completedTasks[position].getValue("completed_tasks_count")
        val entries = ArrayList<PieEntry>()
        if (!(taskCount == 0f && cTaskCount == 0f)) {
            val cTaskEntry = PieEntry(cTaskCount)
            val taskEntry = PieEntry(taskCount)
            if (taskCount > cTaskCount){
                entries.add(cTaskEntry)
                entries.add(taskEntry)
            } else {
                entries.add(taskEntry)
                entries.add(cTaskEntry)
            }



            val dataSet = PieDataSet(entries, "")
            dataSet.valueFormatter = DefaultValueFormatter(0)
            dataSet.sliceSpace = 5f
            dataSet.selectionShift = 3f
            dataSet.setAutomaticallyDisableSliceSpacing(true)
            val colors = ArrayList<Int>()
            if (taskCount > cTaskCount) {
                colors.add(Color.parseColor("#7c4dff"))
                colors.add(Color.parseColor("#FFC41520"))
            } else {
                colors.add(Color.parseColor("#FFC41520"))
                colors.add(Color.parseColor("#7c4dff"))
            }

            dataSet.colors = colors

            val data = PieData(dataSet)
            if (taskCount == 0f && cTaskCount > 0) {
                data.setValueTextSize(0f)
                holder.mChart.description.isEnabled = true
                val desc = Description()
                desc.text = "All Done!"
                desc.typeface = ResourcesCompat.getFont(context, R.font.google_sans_regular)
                desc.textColor = Color.WHITE
                desc.textSize = 14f
                desc.yOffset = -15f
                desc.xOffset = 10f
                holder.mChart.description = desc
            } else if (taskCount == 0f || cTaskCount == 0f)
                data.setValueTextSize(0f)
            else {
                data.setValueTextSize(14f)
            }
            data.setValueTextColor(Color.WHITE)
            holder.mChart.data = data
            holder.mChart.invalidate()

            val highlightedEntry = getHighlightEntry(taskEntry, cTaskEntry)
            val highlight = Highlight(0f, highlightedEntry.y, 0)
            holder.mChart.highlightValue(highlight)
        }
        else {
            entries.add(PieEntry(1f))
            val dataSet = PieDataSet(entries, "")
            val colors = ArrayList<Int>()
            colors.add(ColorTemplate.MATERIAL_COLORS[3])
            dataSet.colors = colors
            val data = PieData(dataSet)
            data.setValueTextSize(0f)
            holder.mChart.description.isEnabled = true
            val desc = Description()
            desc.text = "Empty"
            desc.typeface = ResourcesCompat.getFont(context, R.font.google_sans_regular)
            desc.textColor = Color.WHITE
            desc.textSize = 14f
            desc.yOffset = -10f
            desc.xOffset = 13f
            holder.mChart.description = desc
            holder.mChart.data = data
            holder.mChart.invalidate()
        }
    }

}
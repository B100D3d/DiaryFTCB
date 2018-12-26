package com.devourer.alexb.diaryforthecoolestboys.Fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.devourer.alexb.diaryforthecoolestboys.FingerprintLibrary.FingerprintDialogBuilder
import com.devourer.alexb.diaryforthecoolestboys.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.devourer.alexb.diaryforthecoolestboys.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.settings_bottomsheet.*

class SettingsBottomNavigationDrawerFragment: BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "Main"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_bottomsheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainActivity = activity as MainActivity

        settings_navigation_view_layout.clipToOutline = true

        settings_navigation_view.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.delete_list -> {
                    mainActivity.isDeleteTaskOrList = true
                    FingerprintDialogBuilder(mainActivity)
                        .setTitle("Delete")
                        .setSubtitle("")
                        .setDescription("Are you want to delete this list?")
                        .setNegativeButton("Cancel")
                        .show(mainActivity.mFragmentManager,mainActivity.callback)
                }
                R.id.delete_completed_tasks -> {
                    FingerprintDialogBuilder(mainActivity)
                        .setTitle("Delete")
                        .setSubtitle("")
                        .setDescription("Are you want to delete all completed tasks?")
                        .setNegativeButton("Cancel")
                        .show(mainActivity.mFragmentManager,mainActivity.deleteTasksCallback)

                }
            }
            dismiss()
            true
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val window = dialog.window
        val wlp = window.attributes
        wlp.windowAnimations = R.style.DialogAnimation

        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog


            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)

            bottomSheet.setBackgroundColor(ResourcesCompat.getColor(resources, android.R.color.transparent, null))

            bottomSheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) { BottomSheetBehavior.STATE_HIDDEN-> dismiss() }
                }
            })
        }

        return dialog
    }
}
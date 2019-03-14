package com.devourer.alexb.diaryforthecoolestboys.Fragments

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.devourer.alexb.diaryforthecoolestboys.*
import com.devourer.alexb.diaryforthecoolestboys.FingerprintLibrary.FingerprintDialogBuilder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.settings_bottomsheet.*

class SettingsBottomNavigationDrawerFragment: BottomSheetDialogFragment() {

    lateinit var data: MyData

    companion object {
        private const val TAG = "Main"
        private const val CLICK_BTN = 13
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_bottomsheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainActivity = activity as MainActivity
        val fire = mainActivity.fire
        data = mainActivity.data

        settings_navigation_view_layout.clipToOutline = true

        if (fire.uId == "Cxje6ZRvCaeunxDHJwRjMWMudFe2" || fire.uId == "YdruDXWPkwYWc3fcrLsDy01WgEF2") addClickBtn()

        settings_navigation_view.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.settings -> { startSettingsActivity(mainActivity) }
                R.id.about -> {

                }
                R.id.delete_list -> { deleteList(mainActivity) }
                R.id.delete_completed_tasks -> { deleteAllCompletedTasks(mainActivity) }
                CLICK_BTN -> { ourThing(data, fire, mainActivity) }
            }
            true
        }
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (data.clickNum != 0){
            data.clickNum = 1
        }
    }

    private fun startSettingsActivity(mainActivity: MainActivity){
        val intent = Intent(mainActivity, SettingsActivity::class.java)
        startActivity(intent)
        dismiss()
    }

    private fun addClickBtn(){
        val menu = settings_navigation_view.menu
        menu.add(R.id.about_group, CLICK_BTN, 0, "Click")
    }

    private fun deleteList(mainActivity: MainActivity){
        mainActivity.isDeleteTaskOrList = true
        FingerprintDialogBuilder(mainActivity)
            .setTitle("Delete")
            .setSubtitle("")
            .setDescription("Are you want to delete this list?")
            .setNegativeButton("Cancel")
            .show(mainActivity.mFragmentManager,mainActivity.authCallback)
        dismiss()
    }

    private fun deleteAllCompletedTasks(mainActivity: MainActivity){
        FingerprintDialogBuilder(mainActivity)
            .setTitle("Delete")
            .setSubtitle("")
            .setDescription("Are you want to delete all completed tasks?")
            .setNegativeButton("Cancel")
            .show(mainActivity.mFragmentManager,mainActivity.deleteTasksCallback)
        dismiss()
    }

    private fun ourThing(data: MyData, fire: MyFirebase, mainActivity: MainActivity){
        Log.w(TAG, "clickNum -> ${data.clickNum}")
        when (data.clickNum){
            0 -> {
                fire.changeUIdDoc()
                data.clickNum = 1
                mainActivity.onNavItemSelected(true)
                dismiss()
            }
            in 1..9 -> {
                data.clickNum++
            }
            10 -> {
                if (fire.uId == "Cxje6ZRvCaeunxDHJwRjMWMudFe2")
                    fire.changeUIdDoc("YdruDXWPkwYWc3fcrLsDy01WgEF2")
                else if (fire.uId == "YdruDXWPkwYWc3fcrLsDy01WgEF2")
                    fire.changeUIdDoc("Cxje6ZRvCaeunxDHJwRjMWMudFe2")
                data.clickNum = 0
                mainActivity.onNavItemSelected(true)
                dismiss()
            }
        }
    }
}
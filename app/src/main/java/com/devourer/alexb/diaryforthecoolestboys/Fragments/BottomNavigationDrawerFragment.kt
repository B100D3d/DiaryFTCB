package com.devourer.alexb.diaryforthecoolestboys.Fragments


import android.app.Dialog
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.devourer.alexb.diaryforthecoolestboys.MainActivity
import com.devourer.alexb.diaryforthecoolestboys.MyFirebase
import com.devourer.alexb.diaryforthecoolestboys.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottomsheet.*
import com.devourer.alexb.diaryforthecoolestboys.NavMenuCheckedItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import kotlin.collections.ArrayList


class BottomNavigationDrawerFragment: BottomSheetDialogFragment() {

    lateinit var fire: MyFirebase

    companion object {
        private const val TAG = "Main"
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottomsheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //initViews()
        val mainActivity = activity as MainActivity
        fire = MyFirebase(mainActivity)
        initMenus(mainActivity)
        if (mainActivity.mTaskLists.size > 4){
            val param = navigation_view.layoutParams
            val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                280f,
                resources.displayMetrics
            )
            param.height = px.toInt()
        }
        //navigation_view_layout.clipToOutline = true
        //navigation_view.clipToOutline = true
        setProfileInfo(fire.name, fire.email, fire.photoUrl)
        val id: Long = NavMenuCheckedItem.id
        navigation_view.setCheckedItem(id.toInt())

        navigation_view.setNavigationItemSelectedListener { menuItem ->
            //navigation_view.setCheckedItem(menuItem.itemId)
            //Log.w(TAG,"menuItem.itemId -> ${menuItem.itemId} ")
            /*if(menuItem.itemId!=R.id.create_new_list && menuItem.itemId!=R.id.show_statistick) {

            }
            when(menuItem.itemId){
                R.id.create_new_list -> { mainActivity.createNewTasksList() }
                R.id.show_statistick -> { }
            }*/
            NavMenuCheckedItem.id = menuItem.itemId.toLong()
            NavMenuCheckedItem.title = menuItem.title as String
            mainActivity.onNavItemSelected(false)
            dismiss()
            true
        }

        logoutBtn.setOnClickListener { mainActivity.signOut() }
        createNewListBtn.setOnClickListener {
            dismiss()
            mainActivity.createNewTasksList() }



        disableNavigationViewScrollbars(navigation_view)
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
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            // Наконец-то, еб@нные прозрачные скругленные углы у диалога
            bottomSheet.setBackgroundColor(ResourcesCompat.getColor(resources,android.R.color.transparent,null))

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

    private fun disableNavigationViewScrollbars(navigationView: NavigationView?) {
        val navigationMenuView = navigationView?.getChildAt(0) as NavigationMenuView
        navigationMenuView.isVerticalScrollBarEnabled = false
    }

    private fun setProfileInfo(name: String, email: String, photoUrl: Uri){
        nameTextView.text = name
        emailTextView.text = email
        Glide.with(this).load(photoUrl).apply(RequestOptions.circleCropTransform()).into(avatar)

    }

    private fun initMenus(mainActivity: MainActivity){
        val menu = navigation_view.menu
        val taskList = mainActivity.mTaskLists
        for ((i) in (0 until taskList.size).withIndex()){
            menu.add(R.id.tasksListGroupMenu,taskList[i].taskListId.toInt(),0,taskList[i].nameOfTaskList).isCheckable = true
        }

    }
}
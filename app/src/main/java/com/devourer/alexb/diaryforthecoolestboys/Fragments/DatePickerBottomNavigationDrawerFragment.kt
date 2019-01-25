package com.devourer.alexb.diaryforthecoolestboys.Fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.devourer.alexb.diaryforthecoolestboys.MainActivity
import com.devourer.alexb.diaryforthecoolestboys.R
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.date_bottomsheet.*
import java.util.*


class DatePickerBottomNavigationDrawerFragment : BottomSheetDialogFragment(){

    var date: Date = Date()
    private var isClicked = false

    companion object {
        private const val TAG = "Main"
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.date_bottomsheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainActivity = activity as MainActivity
        val picker: SingleDateAndTimePicker = datePicker
        picker.setStepMinutes(1)
        val okBtn = OKDateBtn

        okBtn.setOnClickListener {
            date = picker.date
            mainActivity.dateAndTime.time = date
            mainActivity.setInitialDateTime()
            isClicked = true
            dismiss()
        }

        CancelDateBtn.setOnClickListener {
            dismiss()
        }


    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!isClicked){
            val mainActivity = activity as MainActivity
            mainActivity.setChipChecked(false)
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
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

            // Наконец-то, еб@нные прозрачные скругленные углы у диалога
            bottomSheet!!.setBackgroundColor(ResourcesCompat.getColor(resources,android.R.color.transparent,null))

            bottomSheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN){
                        dismiss()
                    }
                }
            })
        }

        return dialog
    }
}
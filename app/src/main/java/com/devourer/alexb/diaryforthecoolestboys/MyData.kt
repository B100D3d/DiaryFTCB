package com.devourer.alexb.diaryforthecoolestboys

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class MyData(var id: Long = R.id.my_tasks_list.toLong(),
                  var title: String = "My Tasks",
                  var position: Int = -1,
                  var taskText: String? = "",
                  var taskDetailsText: String? = "",
                  var notificationDate: @RawValue Any? = null,
                  var clickNum: Int = 1
) : Parcelable
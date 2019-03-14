package com.devourer.alexb.diaryforthecoolestboys

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class MyData(var listId: Long = R.id.my_tasks_list.toLong(),
                  var title: String = "My Tasks",
                  var position: Int = -1,
                  var taskText: String? = "",
                  var taskDetailsText: String? = "",
                  var notificationDate: Date? = null,
                  var clickNum: Int = 1
) : Parcelable
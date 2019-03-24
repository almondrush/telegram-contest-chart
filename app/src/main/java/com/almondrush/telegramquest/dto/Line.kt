package com.almondrush.telegramquest.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Line(val color: Int, val name: String, val data: List<PointL>): Parcelable
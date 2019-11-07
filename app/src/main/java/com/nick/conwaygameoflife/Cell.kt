package com.nick.conwaygameoflife

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Cell (
    var isLiving: Boolean = false,
    var age: Int = 0
) : Parcelable
package com.mountaintechnology.quarz.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SMS(
    val phone: String,
    val text: String
) : Parcelable
package com.mountaintechnology.quarz.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BarcodeContact(
    val name: String,
    val phone: String,
    val address: String,
    val email: String,
    val url: String,
    val org: String,
    val title: String
) : Parcelable
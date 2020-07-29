package com.example.qr.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BarcodeContact(
    val name: String,
    val phone: String
) : Parcelable
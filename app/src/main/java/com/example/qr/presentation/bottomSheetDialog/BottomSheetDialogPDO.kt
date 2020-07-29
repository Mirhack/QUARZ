package com.example.qr.presentation.bottomSheetDialog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//Parcelable Data Object
@Parcelize
data class BottomSheetDialogPDO(
    val barcodeText: String,
    val barcodeType: Int,
    val wifiSsid: String,
    val wifiPassword: String,
    val url: String
) : Parcelable



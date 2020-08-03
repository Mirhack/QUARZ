package com.mountaintechnology.quarz.presentation.bottomSheetDialog

import android.os.Parcelable
import com.mountaintechnology.quarz.model.BarcodeContact
import kotlinx.android.parcel.Parcelize

//Parcelable Data Object
@Parcelize
data class BottomSheetDialogPDO(
    val barcodeText: String,
    val barcodeType: Int,
    val wifiSsid: String,
    val wifiPassword: String,
    val url: String,
    val phone: String,
    val barcodeContact: BarcodeContact
) : Parcelable


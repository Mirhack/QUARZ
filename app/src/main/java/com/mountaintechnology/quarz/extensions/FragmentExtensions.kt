package com.mountaintechnology.quarz.extensions

import android.os.Bundle
import android.os.Parcelable

const val EXTRA_DATA = "EXTRA_DATA"



fun <T : Parcelable> Bundle?.loadDTO(): T? {
    return this?.getParcelable(EXTRA_DATA)
}

fun <T : Parcelable> Bundle.saveDTO(data: T?): Bundle {
    this.putParcelable(EXTRA_DATA, data)
    return this
}
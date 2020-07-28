package com.example.qr.extensions

import android.view.View
import java.util.*

fun View.toVisibleOrGone(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun Collection<View>.toVisibleOrGoneAll(isVisible: Boolean) {
    this.forEach { it.visibility = if (isVisible) View.VISIBLE else View.GONE }
}
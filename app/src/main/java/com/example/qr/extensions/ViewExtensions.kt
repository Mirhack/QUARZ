package com.example.qr.extensions

import android.view.View
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import com.example.qr.utils.Event

fun View.toVisibleOrGone(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun Collection<View>.toVisibleOrGoneAll(isVisible: Boolean) {
    this.forEach { it.visibility = if (isVisible) View.VISIBLE else View.GONE }
}

fun <T> Button.clickToEvent(emitter: MutableLiveData<Event<T>>, event: T) {
    this.setOnClickListener {
        emitter.sendEvent(event)
    }
}
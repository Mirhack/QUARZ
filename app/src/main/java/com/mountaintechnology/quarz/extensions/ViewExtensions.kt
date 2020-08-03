package com.mountaintechnology.quarz.extensions

import android.view.View
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mountaintechnology.quarz.utils.Event

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

fun <T> FloatingActionButton.clickToEvent(emitter: MutableLiveData<Event<T>>, event: T) {
    this.setOnClickListener {
        emitter.sendEvent(event)
    }
}
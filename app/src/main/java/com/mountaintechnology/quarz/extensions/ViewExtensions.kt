package com.mountaintechnology.quarz.extensions

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.mountaintechnology.quarz.presentation.cameraActivity.OrientationListener
import com.mountaintechnology.quarz.utils.Event

fun View.toVisibleOrGone(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun Collection<View>.toVisibleOrGoneAll(isVisible: Boolean) {
    this.forEach { it.visibility = if (isVisible) View.VISIBLE else View.GONE }
}

fun <T> View.clickToEvent(emitter: MutableLiveData<Event<T>>, event: T) {
    this.setOnClickListener {
        emitter.sendEvent(event)
    }
}

fun View.rotate(rotation: Int) {
    when (rotation) {
        OrientationListener.ROTATION_0 -> animate().rotation(0F).setDuration(500)
            .start();
        OrientationListener.ROTATION_90 -> animate().rotation(90F).setDuration(500)
            .start();
        OrientationListener.ROTATION_180 -> animate().rotation(180F).setDuration(500)
            .start();
        OrientationListener.ROTATION_270 -> animate().rotation(-90F).setDuration(500)
            .start();
    }
}
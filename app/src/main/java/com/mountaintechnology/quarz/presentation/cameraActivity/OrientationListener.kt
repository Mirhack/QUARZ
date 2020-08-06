package com.mountaintechnology.quarz.presentation.cameraActivity

import android.content.Context
import android.view.OrientationEventListener
import androidx.lifecycle.MutableLiveData
import com.mountaintechnology.quarz.extensions.sendEvent
import com.mountaintechnology.quarz.utils.Event


class OrientationListener(
    context: Context,
    private val eventEmitter: MutableLiveData<Event<CameraActivityViewEvent>>
) :
    OrientationEventListener(context) {

    private var previousRotation = 0
    private var rotation = 0
    override fun onOrientationChanged(orientation: Int) {
        when {
            (orientation < 35 || orientation > 325) && rotation != ROTATION_0 -> {
                rotation = ROTATION_0
            }
            (orientation in 146..214 && rotation != ROTATION_180) -> {
                rotation = ROTATION_180
            }
            (orientation in 56..124 && rotation != ROTATION_270) -> {
                rotation = ROTATION_270
            }
            (orientation in 236..304 && rotation != ROTATION_90) -> {
                rotation = ROTATION_90
            }
        }

        if (previousRotation != rotation) {
            previousRotation = rotation
            eventEmitter.sendEvent(CameraActivityViewEvent.ScreenRotated(rotation))
        }
    }

    companion object {
        const val ROTATION_0 = 1 // PORTRAIT
        const val ROTATION_90 = 2 //LANDSCAPE
        const val ROTATION_180 = 3 // REVERSE PORTRAIT
        const val ROTATION_270 = 4 // REVERSE LANDSCAPE
    }
}

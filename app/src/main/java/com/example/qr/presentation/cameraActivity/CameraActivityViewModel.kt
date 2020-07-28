package com.example.qr.presentation.cameraActivity

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.qr.extensions.send
import com.example.qr.extensions.sendEvent
import com.example.qr.presentation.cameraActivity.CameraActivityViewEffect.*
import com.example.qr.presentation.cameraActivity.CameraActivityViewEvent.Init
import com.example.qr.presentation.cameraActivity.CameraActivityViewEvent.RequestPermissionsResult
import com.example.qr.utils.Event
import com.google.mlkit.vision.barcode.Barcode


class CameraActivityViewModel(application: Application) : AndroidViewModel(application) {
    val viewState: LiveData<CameraActivityViewState> get() = _viewState
    val viewEffect: LiveData<Event<CameraActivityViewEffect>> get() = _viewEffect

    private val _viewState = MutableLiveData<CameraActivityViewState>()
    private val _viewEffect = MutableLiveData<Event<CameraActivityViewEffect>>()

    fun processEvent(event: CameraActivityViewEvent?) {
        when (event) {
            is Init -> init()
            is RequestPermissionsResult -> permissionsRequested(event.requestCode)
            is CameraActivityViewEvent.BarcodeScanned -> barcodeScanned(event.barcode)
            is CameraActivityViewEvent.BottomSheetDialogDismissed -> scanNextCode()
        }
    }


    private fun init() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            _viewEffect.sendEvent(StartCamera)
        } else {
            _viewEffect.sendEvent(RequestPermissions)
        }
        _viewState.send(CameraActivityViewState())
    }

    private fun scanNextCode() {
        _viewState.send(viewState.value!!.copy(isPredefinedValue = true, scanNextCode = true))
    }

    private fun permissionsRequested(requestCode: Int) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                _viewEffect.sendEvent(StartCamera)
            } else {
                _viewEffect.sendEvent(ShowToast("Permissions not granted by the user."))
                _viewEffect.sendEvent(Finish)
            }
        }
    }

    private fun barcodeScanned(barcode: Barcode) {
        _viewState.send(_viewState.value!!.copy(scanNextCode = false))
        _viewEffect.sendEvent(ShowBottomSheetDialog(barcode))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            getApplication(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val TAG = "CameraXBasic"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

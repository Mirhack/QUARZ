package com.mountaintechnology.quarz.presentation.cameraActivity

import com.google.mlkit.vision.barcode.Barcode

data class CameraActivityViewState(
    val isPredefinedValue:Boolean = false,
    val scanNextCode: Boolean = true,
    val isTorchEnabled:Boolean = false,
    val rotation: Int = 1
)

sealed class CameraActivityViewEvent {
    object Init : CameraActivityViewEvent()
    object BottomSheetDialogDismissed: CameraActivityViewEvent()
    object FlashToggle: CameraActivityViewEvent()

    data class RequestPermissionsResult(val requestCode: Int) : CameraActivityViewEvent()
    data class BarcodeScanned(val barcode: Barcode) : CameraActivityViewEvent()
    data class ScreenRotated(val rotation: Int) : CameraActivityViewEvent()
}

sealed class CameraActivityViewEffect {
    object StartCamera : CameraActivityViewEffect()
    object RequestPermissions : CameraActivityViewEffect()
    object Finish : CameraActivityViewEffect()
    data class ShowToast(val text: String) : CameraActivityViewEffect()
    data class ShowBottomSheetDialog(val barcode: Barcode) : CameraActivityViewEffect()

}
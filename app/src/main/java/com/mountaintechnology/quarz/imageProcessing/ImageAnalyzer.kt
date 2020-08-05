package com.mountaintechnology.quarz.imageProcessing

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import com.mountaintechnology.quarz.di.scannerModule
import com.mountaintechnology.quarz.extensions.sendEvent
import com.mountaintechnology.quarz.imageProcessing.graphics.RectangleGraphic
import com.mountaintechnology.quarz.presentation.cameraActivity.CameraActivityViewEvent
import com.mountaintechnology.quarz.presentation.cameraActivity.CameraActivityViewEvent.BarcodeScanned
import com.mountaintechnology.quarz.presentation.cameraActivity.CameraActivityViewModel
import com.mountaintechnology.quarz.presentation.cameraActivity.CameraActivityViewState
import com.mountaintechnology.quarz.utils.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.inject

class ImageAnalyzer(
    private val coroutineScope: CoroutineScope,
    private val graphicOverlay: GraphicOverlay,
    private val eventEmitter: MutableLiveData<Event<CameraActivityViewEvent>>,
    private val viewState: LiveData<CameraActivityViewState>
) : ImageAnalysis.Analyzer, KoinComponent {

    private val scanner: BarcodeScanner by inject()

    private var needUpdateGraphicOverlayImageSourceInfo = true
    private val lensFacing = CameraSelector.LENS_FACING_BACK

    init {
        loadKoinModules(listOf(scannerModule))
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (viewState.value?.scanNextCode == true) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                if (needUpdateGraphicOverlayImageSourceInfo)
                    updateGraphicOverlayImageSourceInfo(imageProxy)

                // Pass image to an ML Kit Vision API
                val barCodesTask = scanner.process(image)
                barCodesTask
                    .addOnSuccessListener { barCodes ->
                        // Task completed successfully
                        if (barCodes.isNotEmpty()) {
                            barCodes[0].let {
                                it.boundingBox?.let { boundingBox ->
                                    graphicOverlay.replace(
                                        RectangleGraphic(
                                            graphicOverlay,
                                            boundingBox
                                        )
                                    )
                                } ?: graphicOverlay.clear()

                                if (!it.displayValue.isNullOrEmpty())
                                    eventEmitter.sendEvent(BarcodeScanned(it))

                                coroutineScope.launch(IO) {
                                    delay(500)
                                    graphicOverlay.clear()
                                }
                            }
                        } else
                            graphicOverlay.clear()

                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        // Task failed with an exception
                        Log.d(CameraActivityViewModel.TAG, it.message ?: "")
                        imageProxy.close()
                    }
            }
        } else imageProxy.close()

    }

    private fun updateGraphicOverlayImageSourceInfo(imageProxy: ImageProxy) {
        val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        if (rotationDegrees == 0 || rotationDegrees == 180) {
            graphicOverlay.setImageSourceInfo(
                imageProxy.width, imageProxy.height, isImageFlipped
            )
        } else {
            graphicOverlay.setImageSourceInfo(
                imageProxy.height, imageProxy.width, isImageFlipped
            )
        }
        needUpdateGraphicOverlayImageSourceInfo = false
    }
}
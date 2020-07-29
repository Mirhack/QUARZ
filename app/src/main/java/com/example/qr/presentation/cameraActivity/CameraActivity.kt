package com.example.qr.presentation.cameraActivity

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.qr.R
import com.example.qr.di.cameraModule
import com.example.qr.di.imageAnalysingModule
import com.example.qr.extensions.savePDO
import com.example.qr.extensions.sendEvent
import com.example.qr.imageProcessing.ImageAnalyzer
import com.example.qr.presentation.bottomSheetDialog.BottomSheetDialog
import com.example.qr.presentation.bottomSheetDialog.BottomSheetDialogPDO
import com.example.qr.presentation.cameraActivity.CameraActivityViewEvent.*
import com.example.qr.presentation.cameraActivity.CameraActivityViewModel.Companion.REQUEST_CODE_PERMISSIONS
import com.example.qr.presentation.cameraActivity.CameraActivityViewModel.Companion.REQUIRED_PERMISSIONS
import com.example.qr.utils.Event
import com.google.mlkit.vision.barcode.Barcode
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import java.util.concurrent.ExecutorService
import kotlin.coroutines.CoroutineContext


class CameraActivity : AppCompatActivity(), CoroutineScope {
    private val viewEvent: LiveData<Event<CameraActivityViewEvent>> get() = _viewEvent
    private val _viewEvent = MutableLiveData<Event<CameraActivityViewEvent>>()

    private var camera: Camera? = null
    private var toast: Toast? = null

    private var imageAnalyzer: ImageAnalyzer? = null
    private val viewModel: CameraActivityViewModel by viewModels()
    private val imageAnalysis: ImageAnalysis by inject()
    private val cameraExecutor: ExecutorService by inject()
    private val cameraSelector: CameraSelector by inject()
    private val preview: Preview by inject()

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        loadKoinModules(listOf(imageAnalysingModule, cameraModule))

        _viewEvent.sendEvent(Init)
    }

    override fun onResume() {
        super.onResume()
        viewEvent.observe(this, Observer { viewModel.processEvent(it.getContentIfNotHandled()) })
        viewModel.viewEffect.observe(this, Observer { onViewEffect(it.getContentIfNotHandled()) })
        viewModel.viewState.observe(this, Observer { onViewStateChanged(it) })
    }

    private fun onViewEffect(event: CameraActivityViewEffect?) {
        when (event) {
            is CameraActivityViewEffect.RequestPermissions -> {
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }
            is CameraActivityViewEffect.StartCamera -> startCamera()
            is CameraActivityViewEffect.ShowToast -> showToast(event.text)
            is CameraActivityViewEffect.ShowBottomSheetDialog -> showBottomSheetDialog(
                getBarcodeBundle(event.barcode)
            )
            is CameraActivityViewEffect.Finish -> finish()
        }
    }

    private fun getBarcodeBundle(barcode: Barcode): Bundle {
        return Bundle().savePDO(
            BottomSheetDialogPDO(
                barcodeText = barcode.displayValue ?: "",
                barcodeType = barcode.valueType,
                wifiSsid = barcode.wifi?.ssid ?: "",
                wifiPassword = barcode.wifi?.password ?: "",
                url = barcode.url?.url ?: ""
            )
        )
    }

    private fun onViewStateChanged(viewState: CameraActivityViewState) {
        imageAnalyzer?.enabled = viewState.scanNextCode
    }

    private fun showBottomSheetDialog(bundle: Bundle) {
        val dialog = BottomSheetDialog()
        dialog.arguments = bundle

        dialog.onDismissListener = {
            _viewEvent.sendEvent(BottomSheetDialogDismissed)
        }
        dialog.show(supportFragmentManager, "dialog")
    }

    private fun showToast(text: String) {
        val displayedText = toast?.let {
            ((it.view as LinearLayout).getChildAt(0) as TextView).text.toString()
        } ?: ""

        if (displayedText != text) {
            toast?.cancel()
            toast = Toast.makeText(this, text, Toast.LENGTH_SHORT).apply { show() }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        _viewEvent.sendEvent(RequestPermissionsResult(requestCode))
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            imageAnalyzer = ImageAnalyzer(this, graphic_overlay, _viewEvent)

            imageAnalysis.setAnalyzer(cameraExecutor, imageAnalyzer!!)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
                preview.setSurfaceProvider(viewFinder.createSurfaceProvider())
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()

    }

    companion object {
        private const val TAG = "CameraXBasic"
    }
}


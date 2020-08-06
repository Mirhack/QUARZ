package com.mountaintechnology.quarz.presentation.cameraActivity

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
import com.google.mlkit.vision.barcode.Barcode
import com.mountaintechnology.quarz.R
import com.mountaintechnology.quarz.di.cameraModule
import com.mountaintechnology.quarz.di.imageAnalysingModule
import com.mountaintechnology.quarz.extensions.clickToEvent
import com.mountaintechnology.quarz.extensions.rotate
import com.mountaintechnology.quarz.extensions.saveDTO
import com.mountaintechnology.quarz.extensions.sendEvent
import com.mountaintechnology.quarz.imageProcessing.ImageAnalyzer
import com.mountaintechnology.quarz.model.BarcodeContact
import com.mountaintechnology.quarz.model.SMS
import com.mountaintechnology.quarz.presentation.bottomSheetDialog.BottomSheetDialog
import com.mountaintechnology.quarz.presentation.bottomSheetDialog.BottomSheetDialogDTO
import com.mountaintechnology.quarz.presentation.cameraActivity.CameraActivityViewEffect.*
import com.mountaintechnology.quarz.presentation.cameraActivity.CameraActivityViewEvent.*
import com.mountaintechnology.quarz.presentation.cameraActivity.CameraActivityViewModel.Companion.REQUEST_CODE_PERMISSIONS
import com.mountaintechnology.quarz.presentation.cameraActivity.CameraActivityViewModel.Companion.REQUIRED_PERMISSIONS
import com.mountaintechnology.quarz.utils.Event
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import java.util.concurrent.ExecutorService
import kotlin.coroutines.CoroutineContext


class CameraActivity : AppCompatActivity(), CoroutineScope,
    BottomSheetDialog.DialogDismissListener {
    private val viewEvent: LiveData<Event<CameraActivityViewEvent>> get() = _viewEvent
    private val _viewEvent = MutableLiveData<Event<CameraActivityViewEvent>>()

    private var camera: Camera? = null
    private var toast: Toast? = null
    private lateinit var orientationListener: OrientationListener
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

        orientationListener = OrientationListener(this, _viewEvent)

        camera_iv_flash.clickToEvent(_viewEvent, FlashToggle)
    }

    override fun onResume() {
        super.onResume()
        viewEvent.observe(this, Observer { viewModel.processEvent(it.getContentIfNotHandled()) })
        viewModel.viewEffect.observe(this, Observer { onViewEffect(it.getContentIfNotHandled()) })
        viewModel.viewState.observe(this, Observer { onViewStateChanged(it) })
    }

    override fun onStart() {
        super.onStart()
        orientationListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationListener.disable()
    }

    private fun onViewEffect(event: CameraActivityViewEffect?) {
        when (event) {
            is RequestPermissions -> {
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            }
            is StartCamera -> startCamera()
            is ShowToast -> showToast(event.text)
            is ShowBottomSheetDialog -> showBottomSheetDialog(getBarcodeBundle(event.barcode))
            is Finish -> finish()
        }
    }

    private fun getBarcodeBundle(barcode: Barcode): Bundle {
        return Bundle().saveDTO(
            barcode.run {
                BottomSheetDialogDTO(
                    barcodeText = displayValue ?: "",
                    barcodeType = valueType,
                    wifiSsid = wifi?.ssid ?: "",
                    wifiPassword = wifi?.password ?: "",
                    url = url?.url ?: "",
                    phone = phone?.number ?: "",
                    barcodeContact = BarcodeContact(
                        contactInfo?.name?.formattedName ?: "",
                        contactInfo?.phones?.getOrNull(0)?.number ?: "",
                        contactInfo?.addresses?.getOrNull(0)?.addressLines?.joinToString() ?: "",
                        contactInfo?.emails?.getOrNull(0)?.address ?: "",
                        contactInfo?.urls?.getOrNull(0) ?: "",
                        contactInfo?.organization ?: "",
                        contactInfo?.title ?: ""
                    ),
                    sms = SMS(sms?.phoneNumber ?: "", sms?.message ?: "")
                )
            }
        )
    }

    private fun onViewStateChanged(viewState: CameraActivityViewState) {
        camera?.cameraControl?.enableTorch(viewState.isTorchEnabled)
        if (viewState.isTorchEnabled)
            camera_iv_flash.setImageResource(R.drawable.ic_flash_off_24)
        else
            camera_iv_flash.setImageResource(R.drawable.ic_flash_on_24)

        camera_iv_flash.rotate(viewState.rotation)
    }

    private fun showBottomSheetDialog(bundle: Bundle) {
        BottomSheetDialog(this).apply {
            arguments = bundle
            show(supportFragmentManager, "dialog")
        }
    }


    /**
     * Реализует быструю смену тостов
     */
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
            imageAnalyzer = ImageAnalyzer(this, graphic_overlay, _viewEvent, viewModel.viewState)

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


    override fun onDialogDismiss() {
        _viewEvent.sendEvent(BottomSheetDialogDismissed)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    companion object {
        private const val TAG = "CameraXBasic"

    }
}


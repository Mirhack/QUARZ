package com.example.qr.presentation.bottomSheetDialog

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.qr.extensions.send
import com.example.qr.extensions.sendEvent
import com.example.qr.presentation.bottomSheetDialog.BottomSheetDialogViewEvent.ConnectClick
import com.example.qr.presentation.bottomSheetDialog.BottomSheetDialogViewEvent.Init
import com.example.qr.utils.Event
import com.example.qr.wifiConnection.WifiConnectionManager
import com.google.mlkit.vision.barcode.Barcode
import org.koin.core.KoinComponent
import org.koin.core.inject


class BottomSheetDialogViewModel(application: Application) : AndroidViewModel(application),
    KoinComponent {
    val viewState: LiveData<BottomSheetDialogViewState> get() = _viewState
    val viewEffect: LiveData<Event<BottomSheetDialogViewEffect>> get() = _viewEffect

    private val _viewState = MutableLiveData<BottomSheetDialogViewState>()
    private val _viewEffect = MutableLiveData<Event<BottomSheetDialogViewEffect>>()

    private val context: Context by inject()

    fun processEvent(event: BottomSheetDialogViewEvent?) {
        when (event) {
            is Init -> _viewState.send(
                BottomSheetDialogViewState(
                    isPredefinedValue = true,
                    barcodeText = event.pdo.barcodeText,
                    wifiSsid = event.pdo.wifiSsid,
                    wifiPassword = event.pdo.wifiPassword,
                    barcodeType = event.pdo.barcodeType,
                    barcodeTypeName = when (event.pdo.barcodeType) {
                        Barcode.TYPE_WIFI -> "WIFI"
                        else -> "Text"
                    },
                    androidVersionLessQ = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                )
            )
            is ConnectClick -> {
                if (viewState.value!!.androidVersionLessQ) {
                    //Connect to WiFi Network
                    val wifiConnectionManager = WifiConnectionManager()
                    wifiConnectionManager.connect(
                        viewState.value?.wifiSsid ?: "",
                        viewState.value?.wifiPassword ?: ""
                    )
                } else {
                    //Copy password
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("copiedText", viewState.value?.wifiPassword)
                    clipboard.setPrimaryClip(clip)
                    _viewEffect.sendEvent(BottomSheetDialogViewEffect.ShowToast("Copied to clipboard"))
                }
            }
        }
    }

}

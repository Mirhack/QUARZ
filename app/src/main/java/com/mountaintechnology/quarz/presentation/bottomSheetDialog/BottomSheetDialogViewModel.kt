package com.mountaintechnology.quarz.presentation.bottomSheetDialog

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.barcode.Barcode.*
import com.mountaintechnology.quarz.R
import com.mountaintechnology.quarz.extensions.send
import com.mountaintechnology.quarz.extensions.sendEvent
import com.mountaintechnology.quarz.presentation.bottomSheetDialog.BottomSheetDialogViewEvent.*
import com.mountaintechnology.quarz.utils.Event
import com.mountaintechnology.quarz.wifiConnection.WifiConnectionManager
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
                event.pdo.run {
                    BottomSheetDialogViewState(
                        isPredefinedValue = true,
                        barcodeText = barcodeText,
                        wifiSsid = wifiSsid,
                        wifiPassword = wifiPassword,
                        barcodeType = barcodeType,
                        androidVersionLessQ = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q,
                        url = url,
                        phone = phone,
                        barcodeContact = barcodeContact
                    )
                }
            )
            is ConnectWiFi -> {
                if (viewState.value!!.androidVersionLessQ) {
                    //Connect to WiFi Network
                    val wifiConnectionManager = WifiConnectionManager()
                    wifiConnectionManager.connect(
                        viewState.value?.wifiSsid ?: "",
                        viewState.value?.wifiPassword ?: ""
                    )
                }
            }
            is CopyToClipboard -> {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = when (viewState.value?.barcodeType) {
                    TYPE_WIFI -> viewState.value?.wifiPassword ?: ""
                    TYPE_TEXT, TYPE_PRODUCT, TYPE_ISBN -> viewState.value?.barcodeText ?: ""
                    TYPE_URL -> viewState.value?.url ?: ""
                    TYPE_PHONE -> viewState.value?.phone ?: ""
                    else -> ""
                }
                val clip = ClipData.newPlainText("copiedText", text)
                clipboard.setPrimaryClip(clip)
                _viewEffect.sendEvent(BottomSheetDialogViewEffect.ShowToast(context.getString(R.string.message_copied)))
            }
            is OpenInBrowser -> {
                _viewEffect.sendEvent(
                    BottomSheetDialogViewEffect.OpenInBrowser(
                        viewState.value?.url ?: ""
                    )
                )
            }
            is AddPhoneToContacts -> {
                _viewEffect.sendEvent(
                    BottomSheetDialogViewEffect.AddPhoneContact(viewState.value?.phone ?: "")
                )
            }
            is Dial -> {
                _viewEffect.sendEvent(
                    BottomSheetDialogViewEffect.Dial(viewState.value?.phone ?: "")
                )
            }
            is AddContact -> {
                viewState.value?.barcodeContact?.let {
                    _viewEffect.sendEvent(
                        BottomSheetDialogViewEffect.AddContact(it)
                    )
                }
            }
        }
    }

}

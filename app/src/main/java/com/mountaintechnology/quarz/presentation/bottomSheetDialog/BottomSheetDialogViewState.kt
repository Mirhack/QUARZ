package com.mountaintechnology.quarz.presentation.bottomSheetDialog

import com.mountaintechnology.quarz.model.BarcodeContact
import com.mountaintechnology.quarz.model.SMS


data class BottomSheetDialogViewState(
    val isPredefinedValue: Boolean = false,
    val barcodeText: String,
    val barcodeType: Int,
    val wifiSsid: String,
    val wifiPassword: String,
    val url: String,
    val phone: String,
    val barcodeContact: BarcodeContact,
    val sms: SMS,
    val androidVersionLessQ: Boolean
)

sealed class BottomSheetDialogViewEvent {
    data class Init(val dto: BottomSheetDialogDTO) : BottomSheetDialogViewEvent()
    object ConnectWiFi : BottomSheetDialogViewEvent()
    object CopyToClipboard : BottomSheetDialogViewEvent()
    object OpenInBrowser : BottomSheetDialogViewEvent()
    object AddPhoneToContacts : BottomSheetDialogViewEvent()
    object AddContact : BottomSheetDialogViewEvent()
    object SendSMS : BottomSheetDialogViewEvent()
    object Dial : BottomSheetDialogViewEvent()
}

sealed class BottomSheetDialogViewEffect {
    data class ShowToast(val text: String) : BottomSheetDialogViewEffect()
    data class OpenInBrowser(val url: String) : BottomSheetDialogViewEffect()
    data class AddPhoneContact(val phone: String) : BottomSheetDialogViewEffect()
    data class AddContact(val barcodeContact: BarcodeContact) : BottomSheetDialogViewEffect()
    data class SendSMS(val sms: SMS) : BottomSheetDialogViewEffect()
    data class Dial(val phone: String) : BottomSheetDialogViewEffect()
}

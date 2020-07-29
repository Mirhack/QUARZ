package com.example.qr.presentation.bottomSheetDialog

import com.example.qr.model.BarcodeContact


data class BottomSheetDialogViewState(
    val isPredefinedValue: Boolean = false,
    val barcodeText: String,
    val barcodeTypeName: String,
    val barcodeType: Int,
    val wifiSsid: String,
    val wifiPassword: String,
    val url: String,
    val phone: String,
    val barcodeContact: BarcodeContact,
    val androidVersionLessQ: Boolean
)

sealed class BottomSheetDialogViewEvent {
    data class Init(val pdo: BottomSheetDialogPDO) : BottomSheetDialogViewEvent()
    object ConnectWiFi : BottomSheetDialogViewEvent()
    object CopyToClipboard : BottomSheetDialogViewEvent()
    object OpenInBrowser : BottomSheetDialogViewEvent()
    object AddPhoneToContacts : BottomSheetDialogViewEvent()
    object AddContact : BottomSheetDialogViewEvent()
    object Dial : BottomSheetDialogViewEvent()
}

sealed class BottomSheetDialogViewEffect {
    data class ShowToast(val text: String) : BottomSheetDialogViewEffect()
    data class OpenInBrowser(val url: String) : BottomSheetDialogViewEffect()
    data class AddPhoneContact(val phone: String) : BottomSheetDialogViewEffect()
    data class AddContact(val barcodeContact: BarcodeContact) : BottomSheetDialogViewEffect()
    data class Dial(val phone: String) : BottomSheetDialogViewEffect()
}

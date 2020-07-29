package com.example.qr.presentation.bottomSheetDialog


data class BottomSheetDialogViewState(
    val isPredefinedValue: Boolean = false,
    val barcodeText: String,
    val barcodeTypeName: String,
    val barcodeType: Int,
    val wifiSsid: String,
    val wifiPassword: String,
    val url: String,
    val androidVersionLessQ: Boolean
)

sealed class BottomSheetDialogViewEvent {
    data class Init(val pdo: BottomSheetDialogPDO) : BottomSheetDialogViewEvent()
    object ConnectWiFi : BottomSheetDialogViewEvent()
    object CopyToClipboard : BottomSheetDialogViewEvent()
    object OpenInBrowser : BottomSheetDialogViewEvent()
}

sealed class BottomSheetDialogViewEffect {
    data class ShowToast(val text: String) : BottomSheetDialogViewEffect()
    data class OpenInBrowser(val url: String) : BottomSheetDialogViewEffect()
}

package com.example.qr.presentation.bottomSheetDialog

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.qr.R
import com.example.qr.extensions.clickToEvent
import com.example.qr.extensions.loadPDO
import com.example.qr.extensions.sendEvent
import com.example.qr.extensions.toVisibleOrGone
import com.example.qr.presentation.bottomSheetDialog.BottomSheetDialogViewEvent.*
import com.example.qr.utils.Event
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.vision.barcode.Barcode
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.type_text.*
import kotlinx.android.synthetic.main.type_url.*
import kotlinx.android.synthetic.main.type_wifi.*

class BottomSheetDialog : BottomSheetDialogFragment() {
    private val viewEvent: LiveData<Event<BottomSheetDialogViewEvent>> get() = _viewEvent
    private val _viewEvent = MutableLiveData<Event<BottomSheetDialogViewEvent>>()

    private val viewModel: BottomSheetDialogViewModel by viewModels()

    var onDismissListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.loadPDO<BottomSheetDialogPDO>()?.let {
            prepareTypeLayout(it.barcodeType)
            _viewEvent.sendEvent(Init(it))
        }
    }

    private fun prepareTypeLayout(barcodeType: Int) {
        when (barcodeType) {
            Barcode.TYPE_WIFI -> {
                bottom_sheet_fl_type_container.apply {
                    layoutResource = R.layout.type_wifi
                    inflate()
                }
                wifi_btn_connect.clickToEvent(_viewEvent, ConnectWiFi)
                wifi_btn_copy_password.clickToEvent(_viewEvent, CopyToClipboard)
            }
            Barcode.TYPE_TEXT -> {
                bottom_sheet_fl_type_container.apply {
                    layoutResource = R.layout.type_text
                    inflate()
                }
                text_btn_copy.clickToEvent(_viewEvent, CopyToClipboard)
            }
            Barcode.TYPE_URL -> {
                bottom_sheet_fl_type_container.apply {
                    layoutResource = R.layout.type_url
                    inflate()
                }
                url_btn_copy.clickToEvent(_viewEvent, CopyToClipboard)
                url_btn_open_link.clickToEvent(_viewEvent, OpenInBrowser)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    override fun onResume() {
        super.onResume()
        viewEvent.observe(this, Observer { viewModel.processEvent(it.getContentIfNotHandled()) })
        viewModel.viewEffect.observe(this, Observer { onViewEffect(it.getContentIfNotHandled()) })
        viewModel.viewState.observe(this, Observer { onViewStateChanged(it) })
    }

    private fun onViewEffect(viewEffect: BottomSheetDialogViewEffect?) {
        when (viewEffect) {
            is BottomSheetDialogViewEffect.ShowToast -> {
                Toast.makeText(context, viewEffect.text, Toast.LENGTH_LONG).show()
            }
            is BottomSheetDialogViewEffect.OpenInBrowser -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(viewEffect.url)
                }
                startActivity(intent)
            }
        }
    }

    private fun onViewStateChanged(viewState: BottomSheetDialogViewState) {
        if (viewState.isPredefinedValue) {
            bottom_sheet_barcode_type.text = viewState.barcodeTypeName

            when (viewState.barcodeType) {
                Barcode.TYPE_WIFI -> {
                    wifi_ssid.text = viewState.wifiSsid
                    wifi_password.text = viewState.wifiPassword
                    wifi_btn_connect.toVisibleOrGone(viewState.androidVersionLessQ)
                }
                Barcode.TYPE_TEXT -> {
                    text_text.text = viewState.barcodeText
                }
                Barcode.TYPE_URL -> {
                    url_address.text = viewState.url
                }
            }
        }
    }
}



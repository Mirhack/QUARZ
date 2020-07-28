package com.example.qr.presentation.bottomSheetDialog

import android.content.DialogInterface
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
import com.example.qr.extensions.loadPDO
import com.example.qr.extensions.sendEvent
import com.example.qr.extensions.toVisibleOrGone
import com.example.qr.extensions.toVisibleOrGoneAll
import com.example.qr.presentation.bottomSheetDialog.BottomSheetDialogViewEvent.ConnectClick
import com.example.qr.presentation.bottomSheetDialog.BottomSheetDialogViewEvent.Init
import com.example.qr.utils.Event
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.vision.barcode.Barcode
import kotlinx.android.synthetic.main.bottom_sheet.*

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
        val view = inflater.inflate(R.layout.bottom_sheet, container, false)
        return view
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottom_sheet_btn_copy_connect.setOnClickListener { _viewEvent.sendEvent(ConnectClick) }

        arguments?.loadPDO<BottomSheetDialogPDO>()?.let {
            _viewEvent.sendEvent(Init(it))
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
        }
    }

    private fun onViewStateChanged(viewState: BottomSheetDialogViewState) {
        if (viewState.isPredefinedValue) {
            listOf(
                bottom_sheet_wifi_ssid,
                bottom_sheet_wifi_ssid_title,
                bottom_sheet_wifi_password,
                bottom_sheet_wifi_password_title,
                bottom_sheet_btn_copy_connect
            ).toVisibleOrGoneAll(viewState.barcodeType == Barcode.TYPE_WIFI)
            bottom_sheet_barcode_text.toVisibleOrGone(viewState.barcodeType != Barcode.TYPE_WIFI)

            bottom_sheet_barcode_text.text = viewState.barcodeText
            bottom_sheet_barcode_type.text = viewState.barcodeTypeName
            bottom_sheet_wifi_ssid.text = viewState.wifiSsid
            bottom_sheet_wifi_password.text = viewState.wifiPassword

            bottom_sheet_btn_copy_connect.text =
                if (viewState.androidVersionLessQ) "Connect" else "Copy to clipboard"
        }
    }
}



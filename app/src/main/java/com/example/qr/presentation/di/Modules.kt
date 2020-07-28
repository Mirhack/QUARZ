package com.example.qr.presentation.di

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.Executors

val contextModule = module {
    single { androidContext() }
}

val scannerModule = module {
    factory (named("scannerOptions")) {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    }

    factory{ BarcodeScanning.getClient(get(named("scannerOptions"))) }

}

val imageAnalysingModule = module {
    factory {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
}

val cameraModule = module {
    factory { Executors.newSingleThreadExecutor() }

    factory {
        CameraSelector.Builder()
                //select Back camera
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }

    factory { Preview.Builder().build() }
}
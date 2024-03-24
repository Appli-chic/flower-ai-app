package com.applichic.flowerdetector

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadImages()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, readImagePermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(readImagePermission)
        } else {
            viewModel.loadImages()
        }

        setContent {
            MaterialTheme {
                MainScreen(viewModel)
            }
        }
    }
}

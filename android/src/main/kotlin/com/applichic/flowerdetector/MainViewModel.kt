package com.applichic.flowerdetector

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadImages() {
        val context = getApplication<Application>().applicationContext

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(images = emptyList())
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            cursor?.use {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                    val bitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeStream(context.contentResolver.openInputStream(contentUri))
                    }
                    _uiState.value = _uiState.value.copy(images = _uiState.value.images + bitmap)
                }
            }
        }
    }
}

data class UiState(
    val images: List<Bitmap> = emptyList(),
)

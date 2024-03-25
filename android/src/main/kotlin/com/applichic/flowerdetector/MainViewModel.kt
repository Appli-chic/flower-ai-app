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

    private val modelManager = ModelManager(application)

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
                withContext(Dispatchers.IO) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val contentUri =
                            Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())

                        val imagePath = "content://media${contentUri.path}"
                        if (imagePath != null) {
                            _uiState.value = _uiState.value.copy(
                                images = _uiState.value.images + imagePath
                            )
                        }
                    }
                }
            }
        }
    }

    fun onRoseFilterChanged() {
        val context = getApplication<Application>().applicationContext
        _uiState.value = _uiState.value.copy(isRoseFilterSelected = !_uiState.value.isRoseFilterSelected)

        if (_uiState.value.isRoseFilterSelected) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    for (imageUrl in _uiState.value.images) {
                        val imageUri = Uri.parse(imageUrl)
                        val bitmap =
                            BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
                        val fixedSizeBitmap = Bitmap.createScaledBitmap(bitmap, 192, 192, true)

                        val score = modelManager.predict(fixedSizeBitmap)

                        if (score == 2) {
                            _uiState.value =
                                _uiState.value.copy(selectedImages = _uiState.value.selectedImages + imageUrl)
                        }
                    }
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(selectedImages = emptyList())
        }
    }
}

data class UiState(
    val images: List<String> = emptyList(),
    val selectedImages: List<String> = emptyList(),
    val isRoseFilterSelected: Boolean = false,
)

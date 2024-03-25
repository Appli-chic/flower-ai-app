package com.applichic.flowerdetector

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ModelManager(context: Context) {

    private val module = LiteModuleLoader.load(assetFilePath(context, "model.ptl"))

    fun predict(image: Bitmap): Int {
        val input = TensorImageUtils.bitmapToFloat32Tensor(
            image,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB,
        )

        val outputTensor = module.forward(IValue.from(input)).toTensor()
        val scores = outputTensor.dataAsFloatArray
        return scores.indices.maxBy { scores[it] }
    }

    private fun assetFilePath(context: Context, assetName: String): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        try {
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
                return file.absolutePath
            }
        } catch (e: IOException) {
            Log.e("ModelManager", "Error process asset $assetName to file path")
        }
        return null
    }
}

package com.example.agriscan.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteHelper(private val context: Context) {

    fun loadModel(): Interpreter {
        val fileDescriptor = context.assets.openFd("agriscan_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        val modelBuffer: MappedByteBuffer =
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        return Interpreter(modelBuffer)
    }
}
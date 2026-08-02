package com.symtotrack.ml

import android.content.Context
import android.util.Log

class ModelInterpreter(context: Context) {

    // TFLite interpreter kept as Any? to avoid hard crash if TFLite not available
    private var interpreter: Any? = null

    init {
        try {
            val assetFiles = context.assets.list("") ?: emptyArray()
            if (assetFiles.contains("model.tflite")) {
                val tfClass = Class.forName("org.tensorflow.lite.Interpreter")
                val modelBuffer = loadModelFile(context, "model.tflite")
                interpreter = tfClass.getConstructor(java.nio.MappedByteBuffer::class.java)
                    .newInstance(modelBuffer)
                Log.d("ModelInterpreter", "TFLite model loaded successfully.")
            } else {
                Log.w("ModelInterpreter", "model.tflite not found in assets. Offline ML disabled.")
            }
        } catch (e: Exception) {
            Log.e("ModelInterpreter", "Failed to load TFLite model: ${e.message}")
        }
    }

    private fun loadModelFile(context: Context, modelName: String): java.nio.MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = java.io.FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun predict(input: FloatArray): FloatArray {
        if (interpreter == null) return FloatArray(1) { 0f }
        val output = FloatArray(1)
        try {
            val runMethod = interpreter!!.javaClass.getMethod("run", Any::class.java, Any::class.java)
            runMethod.invoke(interpreter, input, output)
        } catch (e: Exception) {
            Log.e("ModelInterpreter", "Prediction failed: ${e.message}")
        }
        return output
    }
}

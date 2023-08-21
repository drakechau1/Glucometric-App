package com.example.glucometric1

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import java.nio.FloatBuffer
import java.util.Arrays

class Model : AppCompatActivity() {
    var ValueGlucoseOutput:Int = 80
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model)
        // Parse input from inputEditText
        val intent = intent
        val dataDevice = intent.getFloatArrayExtra("data")

        if (dataDevice != null) {
            Log.d("GetValue", "Start runProcessing")
            ValueGlucoseOutput = runProcessing(dataDevice)
        }
    }
    fun runProcessing(inputs: FloatArray): Int{
        if (inputs != null) {
            Log.d("FloatArray", Arrays.toString(inputs))
            val ortEnvironment = OrtEnvironment.getEnvironment()
            val ortSession = createORTSession(ortEnvironment)
            val output = runPrediction(inputs, ortSession, ortEnvironment)
            val textview = findViewById<TextView>(R.id.textViewdata)
            Log.d("SUCCESS", output.toString())
            val around:Float = String.format("%.1f", output).toFloat()
            ValueGlucoseOutput = around.toInt()
            Log.d("ValueGlucoseOutput", "ValueGlucoseOutput: $ValueGlucoseOutput")
            Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                override fun run() {
                    GetValue(ValueGlucoseOutput)
                }
            },1000)
        } else {
            Log.d("FAIL", "Error")
        }
        return ValueGlucoseOutput
    }
    fun GetValue(value: Int){
        val resultIntent = Intent(this, MeasurementActivity::class.java)
        resultIntent.putExtra("resultData", value)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    // Create an OrtSession with the given OrtEnvironment
    private fun createORTSession(ortEnvironment: OrtEnvironment): OrtSession {
        val modelBytes = resources.openRawResource(R.raw.sklearn_model).readBytes()
        return ortEnvironment.createSession(modelBytes)
    }


    // Make predictions with given inputs
    private fun runPrediction(
        input: FloatArray,
        ortSession: OrtSession,
        ortEnvironment: OrtEnvironment
    ): Float {
        // Get the name of the input node
        val inputName = ortSession.inputNames?.iterator()?.next()
        // Make a FloatBuffer of the inputs
        val floatBufferInputs = FloatBuffer.wrap(input)
        // Create input tensor with floatBufferInputs of shape ( 1 , 1 )
        val inputTensor =
            OnnxTensor.createTensor(ortEnvironment, floatBufferInputs, longArrayOf(1, 18))
        // Run the model
        val results = ortSession.run(mapOf(inputName to inputTensor))
        // Fetch and return the results
        val output = results[0].value as Array<FloatArray>
        return output[0][0]
    }
}
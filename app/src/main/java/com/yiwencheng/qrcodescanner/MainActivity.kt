package com.yiwencheng.qrcodescanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.yiwencheng.qrcodescanner.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    lateinit var detector:BarcodeDetector
    lateinit var binding:ActivityMainBinding
    lateinit var cameraSource:CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        configCameraSource()

    }

    private fun init(){
        val processor = object : Detector.Processor<Barcode>{
            override fun release() {

            }

            override fun receiveDetections(detection: Detector.Detections<Barcode>?) {
                if(detection !=null && detection.detectedItems.isNotEmpty()){
                    val barcode = detection.detectedItems
                    if(barcode.size() ?:0>0){
                        binding.textScanResult.text = barcode.valueAt(0).displayValue
                        Log.d("abc",barcode.valueAt(0).displayValue)
                        Toast.makeText(baseContext,barcode.valueAt(0).displayValue?:" ",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        detector = BarcodeDetector.Builder(baseContext).setBarcodeFormats(Barcode.QR_CODE).build()
        detector.setProcessor(processor)
    }

    private fun configCameraSource(){
        val surfaceCallBack = object : SurfaceHolder.Callback{
            @RequiresApi(Build.VERSION_CODES.M)
            override fun surfaceCreated(holder: SurfaceHolder) {
                if(ContextCompat.checkSelfPermission(baseContext!!,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    cameraSource.start(holder)
                }else{
                    requestPermissions(arrayOf(Manifest.permission.CAMERA),100)
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            //    Toast.makeText(baseContext, "Surface changed", Toast.LENGTH_SHORT).show()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }

        }

        cameraSource = CameraSource.Builder(baseContext,detector)
            .setRequestedFps(25f)
            .setAutoFocusEnabled(true)
            .build()
        binding.cameraSurfaceView.holder.addCallback(surfaceCallBack)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                cameraSource.start(binding.cameraSurfaceView.holder)
            }else{
                Toast.makeText(baseContext,"Please give permission to continue",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        cameraSource.stop()
        cameraSource.release()
    }
}
package com.gliu.wifiscan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gliu.wifiscan.databinding.ActivityMainBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.Text
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var textView: TextView
    private lateinit var surfaceView: SurfaceView

    private lateinit var cameraSource: CameraSource
    private lateinit var textRecognizer: TextRecognizer

    private lateinit var stringResult: String

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        ActivityCompat.requestPermissions(this, Array(1){Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED)
    }

    protected override fun onDestroy() {
        super.onDestroy()
    }

    private fun textRecognizer() {
        context = this
        textRecognizer = TextRecognizer.Builder(applicationContext).build()
        cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setRequestedPreviewSize(412, 653)
            .build()
        surfaceView = findViewById(R.id.surfaceView)

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(context as MainActivity, Array(1){Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED)
                    }
                    cameraSource.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        textRecognizer.setProcessor(object: Detector.Processor<TextBlock> {
            override fun release() {
            }

            override fun receiveDetections(p0: Detector.Detections<TextBlock>) {
                var sparseArray: SparseArray<TextBlock> = p0.detectedItems
                var stringBuilder: StringBuilder = StringBuilder()

                for (i in 0..sparseArray.size()) {
                    var textBlock: TextBlock = sparseArray[i]
                    if (textBlock != null) {
                        stringBuilder.append(textBlock.value + " ")
                    }
                }

                var fullString: String = stringBuilder.toString()
                var handler: Handler = Handler(Looper.getMainLooper())
                handler.post(object: Runnable {
                    override fun run() {
                        stringResult = fullString
                        resultObtained()
                    }
                })
            }
        })
    }

    private fun resultObtained() {
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        textView.text = stringResult

    }

    fun buttonStart(view: View) {
        setContentView(R.layout.surface_view_camera)
        textRecognizer()
    }

}

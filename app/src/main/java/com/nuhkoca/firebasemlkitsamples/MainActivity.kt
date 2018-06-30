package com.nuhkoca.firebasemlkitsamples

import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import com.bumptech.glide.Glide
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.nuhkoca.firebasemlkitsamples.databinding.ActivityMainBinding
import org.jetbrains.anko.alert

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.imageUrlField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Glide.with(applicationContext).load(binding.imageUrlField.text.toString())
                        .into(binding.imageHolder)
                true
            }
            false
        }
    }

    fun recognizeText(v: View) {
        val textImage = FirebaseVisionImage.fromBitmap(
                (binding.imageHolder.drawable as BitmapDrawable).bitmap
        )

        val detector = FirebaseVision.getInstance().visionTextDetector

        detector.detectInImage(textImage)
                .addOnCompleteListener {
                    var detectedText = ""
                    it.result.blocks.forEach {
                        detectedText += it.text + "\n"
                    }

                    runOnUiThread {
                        alert(detectedText, "Text").show().setCanceledOnTouchOutside(false)
                    }
                }

        detector.close()
    }

    fun detectFaces(v: View) {
        val detector = FirebaseVision.getInstance().visionFaceDetector

        detector.detectInImage(FirebaseVisionImage.fromBitmap(
                (binding.imageHolder.drawable as BitmapDrawable).bitmap
        )).addOnCompleteListener {
            val markedBitmap =
                    (binding.imageHolder.drawable as BitmapDrawable)
                            .bitmap
                            .copy(Bitmap.Config.ARGB_8888, true)

            val canvas = Canvas(markedBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.parseColor("#99003399")

            it.result.forEach {
                canvas.drawRect(it.boundingBox, paint)
            }

            runOnUiThread {
                binding.imageHolder.setImageBitmap(markedBitmap)
            }
        }
    }

    fun generateLabels(v: View) {
        val detector =
                FirebaseVision.getInstance().visionLabelDetector

        detector.detectInImage(FirebaseVisionImage.fromBitmap(
                (binding.imageHolder.drawable as BitmapDrawable).bitmap
        )).addOnCompleteListener {
            var output = ""
            it.result.forEach {
                if (it.confidence > 0.7)
                    output += it.label + "\n"
            }
            runOnUiThread {
                alert(output, "Labels").show().setCanceledOnTouchOutside(false)
            }
        }
    }
}
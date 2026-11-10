package com.akula.winkpro

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import java.io.OutputStream

class MainActivity : Activity() {
    private val purple = Color.rgb(164, 93, 255)
    private val background = Color.rgb(13, 10, 22)
    private val panel = Color.rgb(29, 22, 43)
    private var selectedBitmap: Bitmap? = null
    private var photoView: ImageView? = null
    private var brightness = 0
    private var videoView: VideoView? = null

    companion object {
        private const val PICK_PHOTO = 101
        private const val PICK_VIDEO = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun baseLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(18))
            setBackgroundColor(background)
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
    }

    private fun showHome() {
        videoView = null
        val root = baseLayout()
        val brand = label("AKULA", 14, purple, true)
        root.addView(brand)
        root.addView(label("WINK PRO", 30, Color.WHITE, true))
        root.addView(label("Create something worth remembering.", 14, 0xFFB9B0C8.toInt(), false))
        addGap(root, 26)
        root.addView(sectionTitle("YOUR CREATIVE STUDIO"))
        addGap(root, 10)
        root.addView(actionCard("✦  Photo editor", "Import a photo • adjust brightness • save a copy") {
            pick(PICK_PHOTO, "image/*")
        })
        addGap(root, 12)
        root.addView(actionCard("▶  Video studio", "Choose a clip and preview it") {
            pick(PICK_VIDEO, "video/*")
        })
        addGap(root, 22)
        root.addView(sectionTitle("TOOLS"))
        addGap(root, 8)
        root.addView(label("Photo enhancement  •  Brightness control  •  Gallery export", 13, 0xFFD0C8DC.toInt(), false))
        addGap(root, 18)
        val note = TextView(this).apply {
            text = "MVP BUILD  •  MORE TOOLS COMING"
            textSize = 11f
            setTextColor(0xFF8E829F.toInt())
            letterSpacing = .08f
        }
        root.addView(note)
        root.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))
        root.addView(label("Independent project • Not affiliated with Wink", 11, 0xFF81788F.toInt(), false))
        setContentView(root)
    }

    private fun showPhotoEditor(bitmap: Bitmap) {
        selectedBitmap = bitmap
        brightness = 0
        val root = baseLayout()
        root.addView(label("PHOTO STUDIO", 12, purple, true))
        root.addView(label("Make it yours", 26, Color.WHITE, true))
        addGap(root, 14)
        photoView = ImageView(this).apply {
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            adjustViewBounds = true
        }
        root.addView(photoView, LinearLayout.LayoutParams(-1, 0, 1f))
        addGap(root, 14)
        root.addView(label("BRIGHTNESS", 12, 0xFFD8CDEB.toInt(), true))
        val seek = SeekBar(this).apply {
            max = 200
            progress = 100
            progressTintList = android.content.res.ColorStateList.valueOf(purple)
            thumbTintList = android.content.res.ColorStateList.valueOf(purple)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, value: Int, fromUser: Boolean) {
                    brightness = value - 100
                    updatePhotoFilter()
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        root.addView(seek)
        addGap(root, 8)
        val buttons = horizontal()
        buttons.addView(button("BACK") { showHome() }, weighted())
        buttons.addView(button("SAVE COPY") { saveEditedPhoto() }, weighted())
        root.addView(buttons)
        setContentView(root)
    }

    private fun updatePhotoFilter() {
        val b = selectedBitmap ?: return
        val scale = 1f
        val offset = brightness * 1.25f
        val matrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, offset,
            0f, scale, 0f, 0f, offset,
            0f, 0f, scale, 0f, offset,
            0f, 0f, 0f, 1f, 0f
        ))
        photoView?.setImageBitmap(b)
        photoView?.colorFilter = ColorMatrixColorFilter(matrix)
    }

    private fun saveEditedPhoto() {
        val original = selectedBitmap ?: return
        try {
            val output = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(output)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = photoView?.colorFilter
            }
            canvas.drawBitmap(original, 0f, 0f, paint)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "akula_edit_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AKULA-WINK-PRO")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Could not create gallery item")
            val stream: OutputStream = contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("Could not open output stream")
            stream.use { output.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            Toast.makeText(this, "Edited photo saved to Pictures/AKULA-WINK-PRO", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showVideo(uri: Uri) {
        val root = baseLayout()
        root.addView(label("VIDEO STUDIO", 12, purple, true))
        root.addView(label("Your clip", 26, Color.WHITE, true))
        addGap(root, 14)
        val player = VideoView(this)
        videoView = player
        root.addView(player, LinearLayout.LayoutParams(-1, 0, 1f))
        player.setVideoURI(uri)
        player.setOnPreparedListener { it.isLooping = false; player.start() }
        addGap(root, 12)
        root.addView(label("Preview only in this MVP. Video enhancement and export are not implemented yet.", 13, 0xFFD0C8DC.toInt(), false))
        addGap(root, 12)
        root.addView(button("BACK TO HOME") { player.stopPlayback(); showHome() })
        setContentView(root)
    }

    private fun pick(request: Int, mime: String) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = mime }
        startActivityForResult(Intent.createChooser(intent, "Choose media"), request)
    }

    @Deprecated("Legacy activity result used for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            when (requestCode) {
                PICK_PHOTO -> {
                    val stream = contentResolver.openInputStream(uri)
                    val bitmap = stream?.use { BitmapFactory.decodeStream(it) }
                    if (bitmap != null) showPhotoEditor(bitmap)
                    else Toast.makeText(this, "Couldn't read that photo", Toast.LENGTH_SHORT).show()
                }
                PICK_VIDEO -> showVideo(uri)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't open media: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun actionCard(title: String, subtitle: String, action: () -> Unit): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(panel)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        box.addView(label(title, 19, Color.WHITE, true))
        addGap(box, 5)
        box.addView(label(subtitle, 12, 0xFFB9B0C8.toInt(), false))
        return box
    }

    private fun button(text: String, action: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(purple)
            isAllCaps = false
            setOnClickListener { action() }
        }

    private fun label(text: String, size: Int, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun sectionTitle(text: String) = label(text, 11, purple, true)

    private fun horizontal() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }

    private fun weighted() = LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginEnd = dp(6) }

    private fun addGap(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}

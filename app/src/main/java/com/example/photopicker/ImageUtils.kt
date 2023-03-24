package com.example.photopicker

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import timber.log.Timber
import java.io.File

class ImageUtils {
    companion object {
        fun addImageFromDeviceOrCamera(
            activity: Activity,
            takePictureWithCamera: () -> Unit,
            selectImageFromGallery: () -> Unit
        ) {
            val itemTakePictureWithCamera: String = activity.getString(R.string.camera)
            val itemSelectFromPhotos = "Device"
            val itemCancel = activity.getString(R.string.cancel)
            val choices = arrayOf(itemTakePictureWithCamera, itemSelectFromPhotos, itemCancel)

            val builder = AlertDialog.Builder(activity)
            builder.setTitle(activity.getString(R.string.click_to_add_image))
            builder.setItems(choices) { _, which ->
                when (choices[which]) {
                    itemTakePictureWithCamera -> {
                        takePictureWithCamera()
                    }
                    itemSelectFromPhotos -> {
                        selectImageFromGallery()
                    }
                }
            }
            builder.create()
            builder.show()
        }

        /**
         * URI for photo from Camera
         */
        fun getTmpFileUri(applicationContext: Context, cacheDir: File): Uri {
            val tmpFile = File.createTempFile("tmp_image_file_", ".png", cacheDir).apply {
                val fileCreated = createNewFile()
                deleteOnExit()
                Timber.d("tmpFile created = $fileCreated")
            }
            Timber.d("created File ${tmpFile.absolutePath}")
            val uri = FileProvider.getUriForFile(
                applicationContext,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                tmpFile
            )
            Timber.d("Created uri ${uri}")
            return uri
        }

        fun displayCircleImageUri(context: Context, imageUri: String?, imageView: ImageView) {
            imageUri?.let { uri ->
                Glide.with(context)
                    .load(uri)
                    .circleCrop()
                    .into(imageView)
            }
        }
    }
}
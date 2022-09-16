package com.example.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.util.Log
import kotlin.math.roundToInt

class PictureUtils {

    companion object {
        fun getScaledBitmap(path: String, activity: Activity): Bitmap {
            val size = Point()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                size.x = activity.windowManager.currentWindowMetrics.bounds.width()
                size.y = activity.windowManager.currentWindowMetrics.bounds.height()
            }else{
                activity.windowManager.defaultDisplay.getSize(size)
            }
            return getScaledBitmap(path, size.x, size.y)
        }

        fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
            // Read in the dimensions of the image on disk
            var options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            val srcWidth = options.outWidth.toFloat()
            val srcHeight = options.outHeight.toFloat()
            // Figure out how much to scale down by
            var inSampleSize = 1
            if (srcHeight > destHeight || srcWidth > destWidth) {
                val heightScale = srcHeight / destHeight
                val widthScale = srcWidth / destWidth
                val sampleScale = if (heightScale > widthScale) {
                    heightScale
                } else {
                    widthScale
                }
                inSampleSize = sampleScale.roundToInt()
            }
            options = BitmapFactory.Options()
            options.inSampleSize = inSampleSize
            // Read in and create final bitmap
            return BitmapFactory.decodeFile(path, options)
        }
    }
}
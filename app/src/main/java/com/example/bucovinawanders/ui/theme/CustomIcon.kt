package com.example.bucovinawanders.ui.theme

import android.content.*
import android.graphics.*
import android.graphics.drawable.*
import androidx.core.content.*
import androidx.core.graphics.*
import com.google.android.gms.maps.model.*

import com.example.bucovinawanders.*

fun getCustomIcon(context: Context, tip: String, isDarkTheme: Boolean, scale: Float = 1f): BitmapDescriptor {
    val resId = when (tip) {
        "Fortress" -> R.drawable.ic_fortress
        "Church" -> R.drawable.ic_church
        "Museum" -> R.drawable.ic_museum
        "Park" -> R.drawable.ic_park
        "Monastery" -> R.drawable.ic_monastery
        else -> R.drawable.icon_compass
    }

    val vectorDrawable = ContextCompat.getDrawable(context, resId) as VectorDrawable

    val tintColor = if (isDarkTheme) Color.WHITE else Color.BLACK
    vectorDrawable.setTint(tintColor)

    val width = (vectorDrawable.intrinsicWidth * scale).toInt()
    val height = (vectorDrawable.intrinsicHeight * scale).toInt()

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
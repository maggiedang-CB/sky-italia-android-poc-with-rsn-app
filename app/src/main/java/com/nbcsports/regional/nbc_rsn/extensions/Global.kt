package com.nbcsports.regional.nbc_rsn.extensions

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import androidx.annotation.ColorInt
import android.util.Log

import org.joda.time.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by justin on 2018-03-23.
 */
// Simplified logging messages
@SuppressLint("LogNotTimber")
fun Any.e(any: Any? = "no message provided") {
    Log.e(this.javaClass.simpleName + "`~", any.toString())
}

@SuppressLint("LogNotTimber")
fun Any.d(any: Any? = "no message provided") {
    Log.d(this.javaClass.simpleName + "`~", any.toString())
}

inline fun <reified T1 : Enum<T1>, T2 : Number> fromNumber(value: T2) : T1? = enumValues<T1>().firstOrNull { it.ordinal == value }

inline fun <reified T1 : Enum<T1>> fromInt(value: Int) : T1? = fromNumber<T1, Int>(value)

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

// Sets colors for drawables including shapes
fun Drawable.overrideColor(@ColorInt colorInt: Int) {
    when (this) {
        is GradientDrawable -> setColor(colorInt)
        is ShapeDrawable -> paint.color = colorInt
        is ColorDrawable -> color = colorInt
    }
}

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun DateTime.isWithinLastXDays(days: Int): Boolean {
    val now = DateTime()
    val last2weeks = Interval(now.minusDays(days), now)
    return last2weeks.contains(this)
}

fun String.asFormattedCalendarDate(dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"): Calendar? {
    return try {
        val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.time = formatter.parse(this)
        calendar
    } catch (e: Exception) {
        Timber.e("getDateAsCalendar() - date formatting error, %s", e.toString())
        null
    }
}
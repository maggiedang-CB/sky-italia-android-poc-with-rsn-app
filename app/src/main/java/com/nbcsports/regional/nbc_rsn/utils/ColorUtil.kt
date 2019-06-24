package com.nbcsports.regional.nbc_rsn.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.DrawableRes
import com.nbcsports.regional.nbc_rsn.R
import com.nbcsports.regional.nbc_rsn.RsnApplication

object ColorUtil {

    fun makeGradientDrawable(color: String): GradientDrawable {

        //create a new gradient color
        val colorDigits = color.substring(color.indexOf('#') + 1)

        val color1 = Color.parseColor("#ff$colorDigits") //("#008000");
        val color2 = Color.parseColor("#00$colorDigits")  //("#ADFF2F");
        val colors = intArrayOf(color1, color2)

        val gd = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, // BOTTOM_TOP means angle="90"
                colors)
        gd.cornerRadius = 0f

        return gd
    }

    fun setLayerDrawableColor(@DrawableRes resourceId: Int, color: Int): Drawable {

        val drawable = RsnApplication.getInstance().resources.getDrawable(resourceId)
        if (drawable is LayerDrawable) {
            //Drawable coloredDrawable = layerDrawable.getDrawable(0);
            val coloredDrawable = drawable.findDrawableByLayerId(R.id.coloredDrawable)

            if (coloredDrawable is GradientDrawable) {
                coloredDrawable.setColor(color)
            } else {
                //coloredDrawable.setColorFilter(color, PorterDuff.Mode.SRC);//works ok , PorterDuff.Mode.MULTIPLY);// does not work
                coloredDrawable.setTint(color) //works ok
            }
        }
        return drawable
    }
}
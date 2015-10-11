package ds.vkplus.ui

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import de.keyboardsurfer.android.widget.crouton.Crouton
import de.keyboardsurfer.android.widget.crouton.Style
import ds.vkplus.App
import ds.vkplus.R

object CroutonStyles {
	val INFO = Style.Builder().setBackgroundColorValue(App.instance.resources.getColor(R.color.crouton_blue)).build()
	val ERROR = Style.Builder().setBackgroundColorValue(App.instance.resources.getColor(R.color.crouton_red)).build()
}


fun Activity.crouton(text: String, style: Style = CroutonStyles.INFO) = Crouton.makeText(this, text, style/*, this.findViewById(R.id.content) as ViewGroup*/).show()


/*
object Colors {
	val info = App.instance.resources.getColor(R.color.crouton_blue)
	val error = App.instance.resources.getColor(R.color.crouton_blue)
}

fun Activity.snack(text: String, backColor: Int = this.resources.getColor(R.color.crouton_blue), textColor: Int = Color.WHITE) {
	val snack = Snackbar.make(this.findViewById(R.id.content), text, Snackbar.LENGTH_LONG)
	val view = snack.view as Snackbar.SnackbarLayout
	view.setBackgroundColor(backColor)
	(view.findViewById(android.support.design.R.id.snackbar_text) as TextView).setTextColor(textColor)
	val params = view.layoutParams as FrameLayout.LayoutParams
	params.gravity = Gravity.TOP
	view.layoutParams = params
	snack.show()
}*/




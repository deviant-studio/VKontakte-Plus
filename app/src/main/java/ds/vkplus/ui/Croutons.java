package ds.vkplus.ui;

import android.app.Activity;
import android.widget.Toast;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;
import de.keyboardsurfer.android.widget.crouton.Style;
import ds.vkplus.App;
import ds.vkplus.R;
import ds.vkplus.eventbus.EventBus;
import ds.vkplus.eventbus.events.CroutonEvent;
import ds.vkplus.utils.Utils;

public class Croutons {

	public static final int STYLE_ERROR = 0;
	public static final int STYLE_SUCCESS = 1;
	public static final int STYLE_INFO = 3;
	public static final int STYLE_WARN = 4;

	public static final int DURATION_SHORT = 1000;
	public static final int DURATION_LONG = 2000;

	private static final int DEFAULT_DURATION = DURATION_LONG;


	public static class Builder {

		String message;
		int style;
		int duration = DEFAULT_DURATION;
		LifecycleCallback callback;
		Activity a;


		private Builder() {
		}


		private Builder(final Activity a) {
			this.a = a;
		}


		public Builder duration(int d) {
			duration = d;
			return this;
		}


		public Builder style(int s) {
			style = s;
			return this;
		}


		public Builder message(int id) {
			message = App.instance().getString(id);
			return this;
		}


		public Builder message(String m) {
			message = m;
			return this;
		}

		public Builder callback(LifecycleCallback cb){
			callback=cb;
			return this;
		}


		public void show(Activity a) {
			//Crouton.makeText(a, message, getStyle());

			int appearance = R.style.crouton_appearance;
			Style croutonStyle = new Style.Builder().setBackgroundColor(getColor(style)).setTextAppearance(appearance).build();
			final Crouton crouton;
			final Configuration conf = new Configuration.Builder().setDuration(duration).build();
			crouton = Crouton.makeText(a, getMessage(), croutonStyle).setConfiguration(conf);
			if (callback != null)
				crouton.setLifecycleCallback(callback);
			crouton.show();
		}


		public void broadcast() {
			if (Utils.isAppForeground()) {
				EventBus.post(new CroutonEvent(getMessage(), style, duration));
			} else
				Toast.makeText(App.instance(), getMessage(), 0).show();
		}


		private String getMessage() {
			if (message == null)
				message = "[no message]";

			return message;
		}


		private static int getColor(int style) {
			switch (style) {
				case STYLE_ERROR:
					return android.R.color.holo_red_light;
				case STYLE_SUCCESS:
					return android.R.color.holo_green_light;
				case STYLE_INFO:
					return android.R.color.holo_blue_light;
				case STYLE_WARN:
					return android.R.color.holo_orange_light;
				default:
					return R.color.gray1;
			}
		}
	}


	public static Builder prepare() {
		return new Builder();
	}


}

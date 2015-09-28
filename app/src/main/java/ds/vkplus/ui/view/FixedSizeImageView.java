package ds.vkplus.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import ds.vkplus.R;
import ds.vkplus.utils.Utils;

public class FixedSizeImageView extends ImageView {

	public int displayH;
	public int displayW;
	public Drawable placeholder;
	private Drawable playIcon;
	private boolean isVideo;


	public FixedSizeImageView(final Context context) {
		super(context);
		//this.phAlpha = 0;
		//this.animated = false;
		placeholder = new ColorDrawable(context.getResources().getColor(R.color.gray2));
		playIcon = context.getResources().getDrawable(R.drawable.video_play);
	}


	public void toggleVideoIcon(boolean enable) {
		isVideo = enable;
		invalidate();
	}


	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		if (isVideo) {
			int w2 = displayW / 2;
			int h2 = displayH / 2;
			int size = Math.min(displayW / 4, displayH / 4);
			size = Math.min(Utils.INSTANCE$.dp(getContext(), 48), size);
			playIcon.setBounds(w2 - size / 2, h2 - size / 2, w2 + size / 2, h2 + size / 2);
			playIcon.draw(canvas);
		}
	}


	public void onMeasure(final int n, final int n2) {
		this.setMeasuredDimension(this.displayW, this.displayH);
	}


}
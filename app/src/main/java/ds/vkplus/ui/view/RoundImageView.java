package ds.vkplus.ui.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundImageView extends ImageView {

	private Paint maskPaint;
	private Path roundedPath;


	public RoundImageView(final Context context) {
		this(context, null, 0);
	}


	public RoundImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public RoundImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);


		maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

		setScaleType(ScaleType.CENTER_CROP);    // always do a nice square pic
		setLayerType(LAYER_TYPE_SOFTWARE, null);
	}


	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawPath(roundedPath, maskPaint);
		//canvas.drawRoundRect();
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w != oldw || h != oldh) {
			roundedPath = new Path();
			roundedPath.addOval(new RectF(0, 0, w, h), Path.Direction.CW);
			roundedPath.setFillType(Path.FillType.INVERSE_WINDING);
		}
	}
}

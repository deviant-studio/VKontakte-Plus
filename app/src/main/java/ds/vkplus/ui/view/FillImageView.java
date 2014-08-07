package ds.vkplus.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FillImageView extends ImageView {

	public FillImageView(Context context) {
		super(context);
	}


	public FillImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public FillImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height;
		if (getDrawable() != null) {
			height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
		} else
			height = 0;
		
		setMeasuredDimension(width, height);
	}
}

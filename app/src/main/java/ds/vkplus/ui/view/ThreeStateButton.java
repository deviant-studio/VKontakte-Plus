/**
 * 
 */
package ds.vkplus.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import ds.vkplus.R;

public class ThreeStateButton extends ImageView {

	public static final int STATE_CHECKED = 0;
	public static final int STATE_HALF = 1;
	public static final int STATE_UNCHECKED = 2;

	private int state;

	private OnClickListener onStateChangedListener = null;


	public ThreeStateButton(Context context) {
		super(context);
		initConfig();
	}


	public ThreeStateButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initConfig();
	}


	public ThreeStateButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initConfig();
	}


	private void nextState() {
		// state++;
		state = state == STATE_CHECKED ? STATE_UNCHECKED : STATE_CHECKED;
		// forces to redraw the view
		redraw();
		if (onStateChangedListener != null)
			onStateChangedListener.onClick(this);
	}


	private void initConfig() {
		// initialize variables
		state = ThreeStateButton.STATE_HALF;

		// listeners
		setOnClickListener(v -> nextState());

	}


	private void redraw() {
		switch (state) {
		case STATE_HALF:
			setBackgroundResource(R.drawable.threestate_half);
			break;
		case STATE_CHECKED:
			setBackgroundResource(R.drawable.threestate_checked);
			break;
		case STATE_UNCHECKED:
			setBackgroundResource(R.drawable.threestate_unchecked);

			break;
		}
	}


	public void setOnStateChangedListener(OnClickListener listener) {
		onStateChangedListener = listener;
	}


	public int getState() {
		return state;
	}


	public void setState(int state) {
		this.state = state;
		// forces to redraw the view
		redraw();
	}

}
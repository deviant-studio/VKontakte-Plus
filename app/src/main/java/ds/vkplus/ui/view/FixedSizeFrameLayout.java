/*
 * Decompiled with CFR 0_85.
 * 
 * Could not load the following classes:
 *  android.content.Context
 *  android.util.AttributeSet
 *  android.widget.FrameLayout
 */
package ds.vkplus.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FixedSizeFrameLayout
		extends FrameLayout {

	private int h;
	private int w;


	public FixedSizeFrameLayout(Context context) {
		super(context);
	}


	public FixedSizeFrameLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}


	public FixedSizeFrameLayout(Context context, AttributeSet attributeSet, int n) {
		super(context, attributeSet, n);
	}


	public void onMeasure(int w, int h) {
		super.onMeasure(MeasureSpec.EXACTLY | this.w, MeasureSpec.EXACTLY | this.h);
	}


	public void setSize(int n, int n2) {
		this.w = n;
		this.h = n2;
		this.requestLayout();
	}
}


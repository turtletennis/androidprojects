package com.example.stevestestapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Paint;
import android.graphics.BlurMaskFilter.Blur;
import android.util.AttributeSet;
import android.view.View;

public class PieChart extends View {

	private boolean mShowText;
	private int mTextPos;
	private Paint mTextPaint;
	private int mTextColor;
	private float mTextHeight;
	
	private Paint mPiePaint;
	private Paint mShadowPaint;
	
	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, 
				R.styleable.PieChart,
				0,0);
		try {
		       mShowText = a.getBoolean(R.styleable.PieChart_showText, false);
		       mTextPos = a.getInteger(R.styleable.PieChart_labelPosition, 0);
		   } finally {
		       a.recycle();
		   }
	}
	
	private void init(){
		mTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setColor(mTextColor);
		if (mTextHeight == 0) {
	       mTextHeight = mTextPaint.getTextSize();
	   } else {
	       mTextPaint.setTextSize(mTextHeight);
	   }
		
		mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPiePaint.setStyle(Paint.Style.FILL);
		mPiePaint.setTextSize(mTextHeight);
		
		mShadowPaint = new Paint(0);
		mShadowPaint.setColor(0xff101010);
		mShadowPaint.setMaskFilter(new BlurMaskFilter(8,BlurMaskFilter.Blur.NORMAL));
	}
	
	public boolean isShowText(){
		return mShowText;
	
	}
	
	public void setShowText(boolean showText){
		mShowText=showText;
		invalidate();
		requestLayout();
	}

}

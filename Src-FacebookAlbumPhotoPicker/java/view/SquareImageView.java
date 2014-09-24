package com.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


public class SquareImageView extends ImageView
{
	public SquareImageView(Context context)
	{
		super(context);
	}


	public SquareImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public SquareImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = 0;

		try
		{
			height = width;
		}
		catch (ArithmeticException e)
		{

		}

		setMeasuredDimension(width, height);
	}
}

package com.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;


public class LiveWebView extends WebView
{

	public LiveWebView(Context context)
	{
		super(context);
	}


	public LiveWebView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public LiveWebView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@Override
	public boolean onCheckIsTextEditor()
	{
		return true;
	}
}
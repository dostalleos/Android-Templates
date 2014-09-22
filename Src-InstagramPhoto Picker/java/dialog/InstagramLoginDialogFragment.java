package com.example.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.ExampleConfig;
import com.example.R;
import com.example.view.LiveWebView;


public class InstagramLoginDialogFragment extends DialogFragment
{
	private static final String ARGUMENT_URL = "url";

	private String mUrl;
	private View mRootView;
	private InstagramLoginDialogListener mListener;


	public interface InstagramLoginDialogListener
	{
		public void onInstagramLoginDialogLoginFinished(String token);
	}


	public static InstagramLoginDialogFragment newInstance(String url)
	{
		InstagramLoginDialogFragment fragment = new InstagramLoginDialogFragment();

		// arguments
		Bundle arguments = new Bundle();
		arguments.putString(ARGUMENT_URL, url);
		fragment.setArguments(arguments);

		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setCancelable(true);
		setRetainInstance(true);

		// handle fragment arguments
		Bundle arguments = getArguments();
		if(arguments != null)
		{
			handleArguments(arguments);
		}

		// set callback listener
		try
		{
			mListener = (InstagramLoginDialogListener) getTargetFragment();
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(getTargetFragment().toString() + " must implement " + InstagramLoginDialogListener.class.getName());
		}
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// cancelable on touch outside
		if(getDialog()!=null) getDialog().setCanceledOnTouchOutside(true);

		// restore saved state
		handleSavedInstanceState();
	}


	@Override
	public void onDestroyView()
	{
		// http://code.google.com/p/android/issues/detail?id=17423
		if(getDialog() != null && getRetainInstance()) getDialog().setDismissMessage(null);
		super.onDestroyView();
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// save current instance state
		super.onSaveInstanceState(outState);
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), getTheme(true));
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		mRootView = inflater.inflate(R.layout.dialog_instagram_login, null);
		final LiveWebView webView = ((LiveWebView) mRootView.findViewById(R.id.dialog_instagram_login_webview));
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setWebViewClient(new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if(url.startsWith(ExampleConfig.INSTAGRAM_REDIRECT_URL))
				{
					if(url.contains("error"))
					{
						mListener.onInstagramLoginDialogLoginFinished("error");
					}
					else
					{
						String parts[] = url.split("=");
						mListener.onInstagramLoginDialogLoginFinished(parts[1]);

					}
					dismiss();
					return true;
				}
				return false;
			}
		});
		webView.requestFocus(View.FOCUS_DOWN);
		webView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_UP:

						v.requestFocus();

						break;
				}
				return false;
			}
		});
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(mUrl);

		builder.setView(mRootView);
		// create dialog from builder
		final AlertDialog dialog = builder.create();
		return dialog;
	}


	private int getTheme(boolean light)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			return light ? android.R.style.Theme_DeviceDefault_Light_Dialog : android.R.style.Theme_DeviceDefault_Dialog;
		}
		else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			return light ? android.R.style.Theme_Holo_Light_Dialog : android.R.style.Theme_Holo_Dialog;
		}
		else
		{
			return android.R.style.Theme_Dialog;
		}
	}


	private void handleArguments(Bundle arguments)
	{
		if(arguments.containsKey(ARGUMENT_URL))
		{
			mUrl = (String) arguments.get(ARGUMENT_URL);
		}
	}


	private void handleSavedInstanceState()
	{

	}
}
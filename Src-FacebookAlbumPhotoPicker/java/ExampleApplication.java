package com.example;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;


public class ExampleApplication extends Application
{
	private static ExampleApplication mInstance;


	public ExampleApplication()
	{
		mInstance = this;
	}


	@Override
	public void onCreate()
	{
		super.onCreate();

		// force AsyncTask to be initialized in the main thread due to the bug:
		// http://stackoverflow.com/questions/4280330/onpostexecute-not-being-called-in-asynctask-handler-runtime-exception
		try
		{
			Class.forName("android.os.AsyncTask");
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		// init image caching
		File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());
		cacheDir.mkdirs(); // needs android.permission.WRITE_EXTERNAL_STORAGE

		try
		{
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
					.threadPoolSize(3)
					.threadPriority(Thread.NORM_PRIORITY - 2)
					.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
					.diskCache(new LruDiscCache(cacheDir, new HashCodeFileNameGenerator(), 32 * 1024 * 1024))
					.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
					.build();

			ImageLoader.getInstance().init(config);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	public static Context getContext()
	{
		return mInstance;
	}
}

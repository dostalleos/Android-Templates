package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.example.R;
import com.example.entity.FBPhotoEntity;
import com.example.listener.AnimateImageLoadingListener;
import com.example.listener.OnGridItemClickListener;

import java.util.ArrayList;


public class PhotoPickerAdapter extends BaseAdapter
{
	private Context mContext;
	private ArrayList<FBPhotoEntity> mItemList;
	private int mSelectedPosition = -1;
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private DisplayImageOptions mDisplayImageOptions;
	private ImageLoadingListener mImageLoadingListener;
	private OnGridItemClickListener mListener;


	public PhotoPickerAdapter(Context context, ArrayList<FBPhotoEntity> items, OnGridItemClickListener listener)
	{
		mContext = context;
		mItemList = items;
		mListener = listener;

		// image caching options
		mDisplayImageOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(android.R.color.transparent)
				.showImageForEmptyUri(R.drawable.placeholder_no_photo_gallery_square)
				.showImageOnFail(R.drawable.placeholder_no_photo_gallery_square)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.displayer(new SimpleBitmapDisplayer())
				.build();
		mImageLoadingListener = new AnimateImageLoadingListener();
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// inflate view
		View view = convertView;
		if(view == null)
		{
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.fragment_photo_picker_item, parent, false);

			// view holder
			ViewHolder holder = new ViewHolder();
			holder.itemLinearLayout = (LinearLayout) view.findViewById(R.id.fragment_photo_picker_grid_item);
			holder.item1LinearLayout = (LinearLayout) view.findViewById(R.id.fragment_photo_picker_grid_item_1);
			holder.photo1ImageView = (ImageView) view.findViewById(R.id.fragment_photo_picker_grid_item_photo_1);
			holder.item2LinearLayout = (LinearLayout) view.findViewById(R.id.fragment_photo_picker_grid_item_2);
			holder.photo2ImageView = (ImageView) view.findViewById(R.id.fragment_photo_picker_grid_item_photo_2);
			holder.item3LinearLayout = (LinearLayout) view.findViewById(R.id.fragment_photo_picker_grid_item_3);
			holder.photo3ImageView = (ImageView) view.findViewById(R.id.fragment_photo_picker_grid_item_photo_3);
			view.setTag(holder);
		}

		// entity
		FBPhotoEntity item1 = (FBPhotoEntity)getItem(position * 3);
		FBPhotoEntity item2 = (FBPhotoEntity)getItem(position * 3 + 1);
		FBPhotoEntity item3 = (FBPhotoEntity)getItem(position * 3 + 2);

		// view holder
		ViewHolder holder = (ViewHolder) view.getTag();

		if(item1 != null)
		{
			// content
			mImageLoader.displayImage(item1.getUrl(), holder.photo1ImageView, mDisplayImageOptions, mImageLoadingListener);
		}
		if(item2 != null)
		{
			// content
			holder.item2LinearLayout.setVisibility(View.VISIBLE);
			mImageLoader.displayImage(item2.getUrl(), holder.photo2ImageView, mDisplayImageOptions, mImageLoadingListener);
		}
		else
		{
			holder.item2LinearLayout.setVisibility(View.INVISIBLE);
		}
		if(item3 != null)
		{
			// content
			holder.item3LinearLayout.setVisibility(View.VISIBLE);
			mImageLoader.displayImage(item3.getUrl(), holder.photo3ImageView, mDisplayImageOptions, mImageLoadingListener);
		}
		else
		{
			holder.item3LinearLayout.setVisibility(View.INVISIBLE);
		}


		int dp4 = (int) mContext.getResources().getDimension(R.dimen.global_metric_4);
		int dp12 = (int) mContext.getResources().getDimension(R.dimen.global_metric_12);
		if(position == 0)
		{
			holder.itemLinearLayout.setPadding(dp4, dp12, dp4, 0);
		}
		else if(position == getCount()-1)
		{
			holder.itemLinearLayout.setPadding(dp4, dp4, dp4, dp12);
		}
		else
		{
			holder.itemLinearLayout.setPadding(dp4, dp4, dp4, 0);
		}

		holder.item1LinearLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(mListener != null)
					mListener.onItemClick(position * 3);
			}
		});

		holder.item2LinearLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(mListener != null)
					mListener.onItemClick(position * 3 + 1);
			}
		});

		holder.item3LinearLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(mListener != null)
					mListener.onItemClick(position * 3 + 2);
			}
		});

		return view;
	}


	@Override
	public int getCount()
	{
		if (mItemList != null)
		{
			if(mItemList.size() % 3 == 0)
				return mItemList.size() / 3;
			else
				return mItemList.size() / 3 + 1;
		}
		else return 0;
	}


	@Override
	public Object getItem(int position)
	{
		if (mItemList != null && mItemList.size() > position) return mItemList.get(position);
		else return null;
	}


	@Override
	public long getItemId(int position)
	{
		return position;
	}


	public void refill(Context context, ArrayList<FBPhotoEntity> items, OnGridItemClickListener listener)
	{
		mContext = context;
		mItemList = items;
		mListener = listener;
		notifyDataSetChanged();
	}


	public void stop()
	{
		// TODO: stop image loader
	}


	static class ViewHolder
	{
		LinearLayout itemLinearLayout;
		LinearLayout item1LinearLayout;
		ImageView photo1ImageView;
		LinearLayout item2LinearLayout;
		ImageView photo2ImageView;
		LinearLayout item3LinearLayout;
		ImageView photo3ImageView;
	}
}

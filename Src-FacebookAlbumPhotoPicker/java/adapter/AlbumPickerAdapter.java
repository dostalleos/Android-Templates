package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.example.R;
import com.example.entity.FBAlbumEntity;
import com.example.listener.AnimateImageLoadingListener;

import java.util.ArrayList;


public class AlbumPickerAdapter extends BaseAdapter
{
	private Context mContext;
	private ArrayList<FBAlbumEntity> mItems;
	private int mSelectedPosition = -1;
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private DisplayImageOptions mDisplayImageOptions;
	private ImageLoadingListener mImageLoadingListener;


	public AlbumPickerAdapter(Context context, ArrayList<FBAlbumEntity> items)
	{
		mContext = context;
		mItems = items;

		// image caching options
		mDisplayImageOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(android.R.color.transparent)
				.showImageForEmptyUri(R.drawable.placeholder_no_photo_gallery_round)
				.showImageOnFail(R.drawable.placeholder_no_photo_gallery_round)
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
			view = inflater.inflate(R.layout.fragment_album_picker_item, parent, false);

			// view holder
			ViewHolder holder = new ViewHolder();
			holder.nameTextView = (TextView) view.findViewById(R.id.fragment_album_picker_item_name);
			holder.photoImageView = (ImageView) view.findViewById(R.id.fragment_album_picker_item_photo);
			view.setTag(holder);
		}

		// entity
		FBAlbumEntity item = (FBAlbumEntity) mItems.get(position);

		if(item != null)
		{
			// view holder
			ViewHolder holder = (ViewHolder) view.getTag();

			// content
			mImageLoader.displayImage(item.getCoverPhoto(), holder.photoImageView, mDisplayImageOptions, mImageLoadingListener);

			holder.nameTextView.setText(item.getName());
		}

		return view;
	}


	@Override
	public int getCount()
	{
		if(mItems != null) return mItems.size();
		else return 0;
	}


	@Override
	public Object getItem(int position)
	{
		if(mItems != null) return mItems.get(position);
		else return null;
	}


	@Override
	public long getItemId(int position)
	{
		return position;
	}


	public void refill(Context context, ArrayList<FBAlbumEntity> items)
	{
		mContext = context;
		mItems = items;
		notifyDataSetChanged();
	}


	public void stop()
	{
		// TODO: stop image loader
	}


	static class ViewHolder
	{
		TextView nameTextView;
		ImageView photoImageView;
	}
}

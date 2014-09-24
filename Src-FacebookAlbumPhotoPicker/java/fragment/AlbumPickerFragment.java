package com.example.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.example.R;
import com.example.activity.PhotoPickerActivity;
import com.example.adapter.AlbumPickerAdapter;
import com.example.task.TaskListFragment;
import com.example.utility.Logcat;
import com.example.utility.NetworkManager;
import com.example.entity.FBAlbumEntity;
import com.example.view.ViewState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;


public class AlbumPickerFragment extends TaskListFragment
{
	public static final String FB_FIELDS_PARAM = "fields";
	public static final String FB_PHOTO_ALBUM_LIST = "albums.fields(id,name,cover_photo, photos.limit(1).fields(source))";

	private static final int LAZY_LOADING_TAKE = 15;
	private static final int LAZY_LOADING_OFFSET = 4;

	private boolean mLazyLoading = false;
	private boolean mActionBarProgress = false;
	private ViewState mViewState = null;
	private View mRootView;
	private View mFooterView;
	private AlbumPickerAdapter mAdapter;
	private String mAfterCursor;

	private ArrayList<FBAlbumEntity> mAlbumItems = new ArrayList<FBAlbumEntity>();


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		setRetainInstance(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.fragment_album_picker, container, false);
		return mRootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// load and show data
		if (mViewState == null || mViewState == ViewState.OFFLINE)
		{
			if(NetworkManager.isOnline(getActivity()))
			{
				loadData();
				showProgress();
			}
			else
			{
				showOffline();
			}
		}
		else if (mViewState == ViewState.CONTENT)
		{
			if (mAlbumItems != null) renderView();
			showList();
		}
		else if (mViewState == ViewState.PROGRESS)
		{
			showProgress();
		}

		// progress in action bar
		showActionBarProgress(mActionBarProgress);

		// progress in action bar
		showActionBarProgress(mActionBarProgress);
	}


	@Override
	public void onStart()
	{
		super.onStart();
	}


	@Override
	public void onResume()
	{
		super.onResume();
	}


	@Override
	public void onPause()
	{
		super.onPause();

		// stop adapter
		if (mAdapter != null) mAdapter.stop();
	}


	@Override
	public void onStop()
	{
		super.onStop();
	}


	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		mRootView = null;
	}


	@Override
	public void onDestroy()
	{
		super.onDestroy();

	}


	@Override
	public void onDetach()
	{
		super.onDetach();
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// save current instance state
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// action bar menu
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_photo, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// action bar menu behaviour
		switch(item.getItemId())
		{
			case android.R.id.home:
				getActivity().finish();
				return true;
			case R.id.ab_button_members_online:
				// TODO
				return true;
			case R.id.ab_button_my_profile:
				// TODO
				return true;
			case R.id.ab_button_settings:
				// TODO
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onListItemClick(ListView listView, View clickedView, int position, long id)
	{
		// list position
		int listPosition = getListPosition(position);

		// listview item onclick
		Intent intent = PhotoPickerActivity.newIntent(getActivity(), mAlbumItems.get(listPosition).getId(), mAlbumItems.get(listPosition).getName());
		startActivity(intent);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		try
		{
			Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
	}


	private void loadData()
	{
		Session.openActiveSession(getActivity(), AlbumPickerFragment.this, true, Arrays.asList("user_photos"), new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception)
			{
				if(mRootView == null) return;

				if(session.isOpened())
				{
					Logcat.d("fb.authorize.onComplete: " + session.getAccessToken());
					Bundle params = new Bundle();
					String request = FB_PHOTO_ALBUM_LIST + ".limit(" + LAZY_LOADING_TAKE + ")";

					params.putString(FB_FIELDS_PARAM, request);

					new Request(session, "me", params, HttpMethod.GET, new Request.Callback()
					{
						@Override
						public void onCompleted(Response response)
						{
							if (response.getGraphObject() != null)
							{
								JSONObject json = response.getGraphObject().getInnerJSONObject();

								try
								{
									JSONArray jsonFBAlbums = json.getJSONObject("albums").getJSONArray("data");
									JSONObject jsonFBPaging = json.getJSONObject("albums").getJSONObject("paging");

									mAfterCursor = jsonFBPaging.getJSONObject("cursors").getString("after");

									for (int i = 0; i < jsonFBAlbums.length(); i++)
									{
										JSONObject jsonAlbum = jsonFBAlbums.getJSONObject(i);

										FBAlbumEntity fbAlbum = new FBAlbumEntity();
										fbAlbum.setName(jsonAlbum.getString("name"));
										fbAlbum.setId(jsonAlbum.getString("id"));

										if (!jsonAlbum.has("photos"))
											continue;

										JSONArray jsonPhotos = jsonAlbum.getJSONObject("photos").getJSONArray("data");

										for (int j = 0; j < jsonPhotos.length(); j++)
										{
											JSONObject jsonFBPhoto = jsonPhotos.getJSONObject(j);
											fbAlbum.setCoverPhoto(jsonFBPhoto.getString("source"));
										}
										mAlbumItems.add(fbAlbum);
									}
									// render view
									if (mAlbumItems != null) renderView();

									// hide progress
									showLazyLoadingProgress(false);
									showList();
								}
								catch (JSONException e)
								{
									Logcat.e(e.getMessage());
								}
							}
						}
					}).executeAsync();
				}
			}
		});
	}


	private void loadLazyData(final String after)
	{
		Session session = Session.getActiveSession();

		Bundle params = new Bundle();
		String request = FB_PHOTO_ALBUM_LIST + ".limit(" + LAZY_LOADING_TAKE + ")";
		request += ".after(\"" + after + ("\")");

		params.putString(FB_FIELDS_PARAM, request);

		new Request(session, "me", params, HttpMethod.GET, new Request.Callback()
		{
			@Override
			public void onCompleted(Response response)
			{
				if(response.getGraphObject() != null)
				{
					JSONObject json = response.getGraphObject().getInnerJSONObject();

					try
					{
						JSONArray jsonFBAlbums = json.getJSONObject("albums").getJSONArray("data");
						JSONObject jsonFBPaging = json.getJSONObject("albums").getJSONObject("paging");

						mAfterCursor = jsonFBPaging.getJSONObject("cursors").getString("after");

						for(int i = 0; i < jsonFBAlbums.length(); i++)
						{
							JSONObject jsonAlbum = jsonFBAlbums.getJSONObject(i);

							FBAlbumEntity fbAlbum = new FBAlbumEntity();
							fbAlbum.setName(jsonAlbum.getString("name"));
							fbAlbum.setId(jsonAlbum.getString("id"));

							if(!jsonAlbum.has("photos"))
								continue;

							JSONArray jsonPhotos = jsonAlbum.getJSONObject("photos").getJSONArray("data");

							for(int j = 0; j < jsonPhotos.length(); j++)
							{
								JSONObject jsonFBPhoto = jsonPhotos.getJSONObject(j);
								fbAlbum.setCoverPhoto(jsonFBPhoto.getString("source"));
							}
							mAlbumItems.add(fbAlbum);
						}
						// render view
						if (mLazyLoading && mViewState == ViewState.CONTENT && mAdapter != null)
						{
							mAdapter.notifyDataSetChanged();
						}
						else
						{
							if (mAlbumItems != null) renderView();
						}

						// hide progress
						showLazyLoadingProgress(false);
						showList();
					}
					catch(JSONException e)
					{
						Logcat.e(e.getMessage());
					}
				}
			}
		}).executeAsync();
	}


	private void lazyLoadData()
	{
		if (NetworkManager.isOnline(getActivity()))
		{
			// show lazy loading progress
			showLazyLoadingProgress(true);

			// run async task
			loadLazyData(mAfterCursor);
		}
	}


	private void showLazyLoadingProgress(boolean visible)
	{
		if (visible)
		{
			mLazyLoading = true;

			// show footer
			ListView listView = getListView();
			listView.addFooterView(mFooterView);
		}
		else
		{
			// hide footer
			ListView listView = getListView();
			listView.removeFooterView(mFooterView);

			mLazyLoading = false;
		}
	}


	private void showActionBarProgress(boolean visible)
	{
		// show progress in action bar
		((ActionBarActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(visible);
		mActionBarProgress = visible;
	}


	private void showList()
	{
		// show list container
		ViewGroup containerList = (ViewGroup) mRootView.findViewById(R.id.container_list);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		containerList.setVisibility(View.VISIBLE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.GONE);
		mViewState = ViewState.CONTENT;
	}


	private void showProgress()
	{
		// show progress container
		ViewGroup containerList = (ViewGroup) mRootView.findViewById(R.id.container_list);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		containerList.setVisibility(View.GONE);
		containerProgress.setVisibility(View.VISIBLE);
		containerOffline.setVisibility(View.GONE);
		mViewState = ViewState.PROGRESS;
	}


	private void showOffline()
	{
		// show offline container
		ViewGroup containerList = (ViewGroup) mRootView.findViewById(R.id.container_list);
		ViewGroup containerProgress = (ViewGroup) mRootView.findViewById(R.id.container_progress);
		ViewGroup containerOffline = (ViewGroup) mRootView.findViewById(R.id.container_offline);
		containerList.setVisibility(View.GONE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.VISIBLE);
		mViewState = ViewState.OFFLINE;
	}


	private void renderView()
	{
		// reference
		ListView listView = getListView();

		// listview content
		if (mAdapter == null)
		{
			// create adapter
			mAdapter = new AlbumPickerAdapter(getActivity(), mAlbumItems);
		}
		else
		{
			// refill adapter
			mAdapter.refill(getActivity(), mAlbumItems);
		}

		// init footer, because addFooterView() must be called at least once before setListAdapter()
		mFooterView = getActivity().getLayoutInflater().inflate(R.layout.view_listview_footer, listView, false);
		listView.addFooterView(mFooterView);

		// set adapter
		listView.setAdapter(mAdapter);

		// hide footer
		listView.removeFooterView(mFooterView);

		// lazy loading
		listView.setOnScrollListener(new AbsListView.OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
			}


			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= LAZY_LOADING_OFFSET && mAlbumItems.size() % LAZY_LOADING_TAKE == 0 && !mAlbumItems.isEmpty())
				{
					if (!mLazyLoading) lazyLoadData();
				}
			}
		});
	}


	private int getListPosition(int globalPosition)
	{
		// list position without headers, should be used for getting data entities from collections
		ListView listView = getListView();
		int listPosition = globalPosition;
		if (listView != null)
			listPosition = globalPosition - listView.getHeaderViewsCount();
		return listPosition;
	}
}

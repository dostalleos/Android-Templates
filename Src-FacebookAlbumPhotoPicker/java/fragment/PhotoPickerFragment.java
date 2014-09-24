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
import com.example.adapter.PhotoPickerAdapter;
import com.example.listener.OnGridItemClickListener;
import com.example.task.TaskListFragment;
import com.example.utility.Logcat;
import com.example.utility.NetworkManager;
import com.example.entity.FBPhotoEntity;
import com.example.view.ViewState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;


public class PhotoPickerFragment extends TaskListFragment implements OnGridItemClickListener
{
	private static final int LAZY_LOADING_TAKE = 30;
	private static final int LAZY_LOADING_OFFSET = 2;

	private boolean mLazyLoading = false;
	private boolean mActionBarProgress = false;
	private ViewState mViewState = null;
	private View mRootView;
	private View mFooterView;
	private PhotoPickerAdapter mAdapter;
	private String mAfterCursor;
	private String mAlbumId;

	private ArrayList<FBPhotoEntity> mPhotoItems = new ArrayList<FBPhotoEntity>();


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

		// handle fragment arguments
		Bundle arguments = getActivity().getIntent().getExtras();
		if(arguments != null)
		{
			handleArguments(arguments);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.fragment_photo_picker, container, false);
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
			if (mPhotoItems != null) renderView();
			showList();
		}
		else if (mViewState == ViewState.PROGRESS)
		{
			showProgress();
		}

		// progress in action bar
		showActionBarProgress(mActionBarProgress);

		// lazy loading progress
		if (mLazyLoading) showLazyLoadingProgress(true);
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

		// free adapter
		setListAdapter(null);
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
	public void onItemClick(int position)
	{
		// listview item onclick
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


	private void handleArguments(Bundle arguments)
	{
		if(arguments.containsKey(PhotoPickerActivity.ARGUMENT_ALBUM_ID))
		{
			mAlbumId = (String) arguments.get(PhotoPickerActivity.ARGUMENT_ALBUM_ID);
		}
	}


	private void loadData()
	{
		Session.openActiveSession(getActivity(), PhotoPickerFragment.this, true, Arrays.asList("user_photos"), new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception)
			{
				if(mRootView == null) return;

				if(session.isOpened())
				{
					Bundle params = new Bundle();
					String request = "photos.limit(" + LAZY_LOADING_TAKE + ").fields(source)";

					params.putString("fields", request);

					new Request(session, mAlbumId, params, HttpMethod.GET, new Request.Callback()
					{
						@Override
						public void onCompleted(Response response)
						{
							if (response.getGraphObject() != null)
							{
								JSONObject json = response.getGraphObject().getInnerJSONObject();

								try
								{
									JSONArray jsonFBPhotos = json.getJSONObject("photos").getJSONArray("data");
									JSONObject jsonFBPaging = json.getJSONObject("photos").getJSONObject("paging");

									mAfterCursor = jsonFBPaging.getJSONObject("cursors").getString("after");

									for (int i = 0; i < jsonFBPhotos.length(); i++)
									{
										JSONObject jsonPhoto = jsonFBPhotos.getJSONObject(i);

										FBPhotoEntity fbPhoto = new FBPhotoEntity();
										fbPhoto.setId(jsonPhoto.getString("id"));
										fbPhoto.setUrl(jsonPhoto.getString("source"));
										mPhotoItems.add(fbPhoto);
									}
									// render view
									if (mPhotoItems != null) renderView();

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
		String request = "photos.limit(" + LAZY_LOADING_TAKE + ").fields(source)";
		request += ".after(\"" + after + ("\")");

		params.putString("fields", request);

		new Request(session, mAlbumId, params, HttpMethod.GET, new Request.Callback()
		{
			@Override
			public void onCompleted(Response response)
			{
				if(response.getGraphObject() != null)
				{
					JSONObject json = response.getGraphObject().getInnerJSONObject();

					try
					{
						JSONArray jsonFBPhotos = json.getJSONObject("photos").getJSONArray("data");
						JSONObject jsonFBPaging = json.getJSONObject("photos").getJSONObject("paging");

						mAfterCursor = jsonFBPaging.getJSONObject("cursors").getString("after");

						for(int i = 0; i < jsonFBPhotos.length(); i++)
						{
							JSONObject jsonPhoto = jsonFBPhotos.getJSONObject(i);

							FBPhotoEntity fbPhoto = new FBPhotoEntity();
							fbPhoto.setId(jsonPhoto.getString("id"));
							fbPhoto.setUrl(jsonPhoto.getString("source"));
							mPhotoItems.add(fbPhoto);
						}
						// render view
						if (mLazyLoading && mViewState == ViewState.CONTENT && mAdapter != null)
						{
							mAdapter.notifyDataSetChanged();
						}
						else
						{
							if (mPhotoItems != null) renderView();
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
		if (getListAdapter() == null)
		{
			// create adapter
			mAdapter = new PhotoPickerAdapter(getActivity(), mPhotoItems, this);
		}
		else
		{
			// refill adapter
			mAdapter.refill(getActivity(), mPhotoItems, this);
		}

		// add header
		//setListAdapter(null);
		//if(listView.getHeaderViewsCount()==0)
		//{
		//	mHeaderView = getActivity().getLayoutInflater().inflate(R.layout.fragment_listing_header, listView, false);
		//	listView.addHeaderView(mHeaderView);
		//}

		// init footer, because addFooterView() must be called at least once before setListAdapter()
		mFooterView = getActivity().getLayoutInflater().inflate(R.layout.view_listview_footer, listView, false);
		listView.addFooterView(mFooterView);

		// set adapter
		setListAdapter(mAdapter);

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
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= LAZY_LOADING_OFFSET && mPhotoItems.size() % LAZY_LOADING_TAKE == 0 && !mPhotoItems.isEmpty())
				{
					if (!mLazyLoading) lazyLoadData();
				}
			}
		});
	}
}

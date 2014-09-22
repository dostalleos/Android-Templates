package com.example.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonParseException;
import com.example.ExampleConfig;
import com.example.R;
import com.example.adapter.InstagramPhotoPickerAdapter;
import com.example.client.APICallListener;
import com.example.client.APICallManager;
import com.example.client.APICallTask;
import com.example.client.ResponseStatus;
import com.example.client.request.InstagramAccessTokenRequest;
import com.example.client.request.InstagramPhotosRequest;
import com.example.client.response.Response;
import com.example.dialog.InstagramLoginDialogFragment;
import com.example.entity.InstagramAccessTokenEntity;
import com.example.entity.InstagramPhotosEntity;
import com.example.listener.OnGridItemClickListener;
import com.example.task.TaskListFragment;
import com.example.utility.Logcat;
import com.example.utility.NetworkManager;
import com.example.utility.Preferences;
import com.example.view.ViewState;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class InstagramPhotoPickerFragment extends TaskListFragment implements OnGridItemClickListener,
		InstagramLoginDialogFragment.InstagramLoginDialogListener, APICallListener
{
	private static final String DIALOG_LOGIN = "login";

	private static final int LAZY_LOADING_TAKE = 30;
	private static final int LAZY_LOADING_OFFSET = 2;

	private boolean mLazyLoading = false;
	private boolean mActionBarProgress = false;
	private ViewState mViewState = null;
	private View mRootView;
	private View mFooterView;
	private InstagramPhotoPickerAdapter mAdapter;
	private String mNextId;

	private APICallManager mAPICallManager = new APICallManager();

	private ArrayList<String> mPhotoItems = new ArrayList<String>();


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
		mRootView = inflater.inflate(R.layout.fragment_instagram_photo_picker, container, false);
		return mRootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// load and show data
		if(mViewState == null || mViewState == ViewState.OFFLINE)
		{
			if(NetworkManager.isOnline(getActivity()))
			{
				Preferences prefs = new Preferences(getActivity());
				if(prefs.getInstagramAccessToken() != null)
				{
					loadData();
				}
				else
				{
					showLoginDialog();
				}
			}
			else
			{
				showOffline();
			}
		}
		else if(mViewState == ViewState.CONTENT)
		{
			if(mPhotoItems != null) renderView();
			showList();
		}
		else if(mViewState == ViewState.PROGRESS)
		{
			showProgress();
		}

		// progress in action bar
		showActionBarProgress(mActionBarProgress);

		// lazy loading progress
		if(mLazyLoading) showLazyLoadingProgress(true);
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
		if(mAdapter != null) mAdapter.stop();
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

		// cancel async tasks
		mAPICallManager.cancelAllTasks();
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

		// TODO
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// action bar menu behaviour
		return super.onOptionsItemSelected(item);

		// TODO
	}


	@Override
	public void onItemClick(int position)
	{
		// listview item onclick
	}


	@Override
	public void onInstagramLoginDialogLoginFinished(String token)
	{
		if(token.equals("error"))
		{
			//TODO toast error
		}
		else
		{
			getInstagramAccessToken(token);
		}
	}


	@Override
	public void onAPICallRespond(final APICallTask task, final ResponseStatus status, final Response<?> response)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView==null) return; // view was destroyed

				if(task.getRequest().getClass().equals(InstagramAccessTokenRequest.class))
				{
					Response<InstagramAccessTokenEntity> instagramAccessTokenResponse = (Response<InstagramAccessTokenEntity>) response;

					// error
					if(instagramAccessTokenResponse.isError())
					{
						Logcat.d("InstagramPhotoPickerFragment.onAPICallRespond(InstagramAccessTokenRequest): " + status.getStatusCode() + " " + status.getStatusMessage() +
								" / error / " + instagramAccessTokenResponse.getErrorType() + " / " + instagramAccessTokenResponse.getErrorMessage());

						// hide progress
						renderView();
						showList();

						Preferences prefs = new Preferences(getActivity());
						prefs.setInstagramAccessToken(null);

						// handle error
						handleError(instagramAccessTokenResponse.getErrorType(), instagramAccessTokenResponse.getErrorMessage());
					}

					// response
					else
					{
						Logcat.d("InstagramPhotoPickerFragment.onAPICallRespond(InstagramAccessTokenRequest): " + status.getStatusCode() + " " + status.getStatusMessage());

						// get data
						InstagramAccessTokenEntity entity = instagramAccessTokenResponse.getResponseObject();
						Preferences prefs = new Preferences(getActivity());
						prefs.setInstagramAccessToken(entity.getAccessToken());
						prefs.setInstagramUserId(entity.getUser().getUserId());
						loadData();

						// hide progress
						showList();
					}
				}
				else if(task.getRequest().getClass().equals(InstagramPhotosRequest.class))
				{
					Response<InstagramPhotosEntity> instagramPhotosResponse = (Response<InstagramPhotosEntity>) response;

					// error
					if(instagramPhotosResponse.isError())
					{
						Logcat.d("InstagramPhotoPickerFragment.onAPICallRespond(InstagramAccessTokenRequest): " + status.getStatusCode() + " " + status.getStatusMessage() +
								" / error / " + instagramPhotosResponse.getErrorType() + " / " + instagramPhotosResponse.getErrorMessage());

						// hide progress
						renderView();
						showList();
						showLazyLoadingProgress(false);

						// handle error
						handleError(instagramPhotosResponse.getErrorType(), instagramPhotosResponse.getErrorMessage());
					}

					// response
					else
					{
						Logcat.d("InstagramPhotoPickerFragment.onAPICallRespond(InstagramAccessTokenRequest): " + status.getStatusCode() + " " + status.getStatusMessage());

						// get data
						InstagramPhotosEntity entity = instagramPhotosResponse.getResponseObject();
						mNextId = entity.getNextMaxId();
						mPhotoItems.addAll(entity.getPhotos());

						// render view
						if(mLazyLoading && mViewState == ViewState.CONTENT && mAdapter != null)
						{
							mAdapter.notifyDataSetChanged();
						}
						else
						{
							if(mPhotoItems != null) renderView();
						}

						// hide progress
						showLazyLoadingProgress(false);
						showList();
					}
				}

				// finish request
				mAPICallManager.finishTask(task);

				// hide progress in action bar
				if(mAPICallManager.getTasksCount()==0) showActionBarProgress(false);
			}
		});
	}


	@Override
	public void onAPICallFail(final APICallTask task, final ResponseStatus status, final Exception exception)
	{
		runTaskCallback(new Runnable()
		{
			public void run()
			{
				if(mRootView==null) return; // view was destroyed

				if(task.getRequest().getClass().equals(InstagramAccessTokenRequest.class))
				{
					Logcat.d("InstagramPhotoPickerFragment.onAPICallFail(InstagramAccessTokenRequest): " + status.getStatusCode() + " " + status.getStatusMessage() +
							" / " + exception.getClass().getSimpleName() + " / " + exception.getMessage());

					// hide progress
					renderView();
					showList();

					Preferences prefs = new Preferences(getActivity());
					prefs.setInstagramAccessToken(null);

					// handle fail
					handleFail(exception);
				}

				// finish request
				mAPICallManager.finishTask(task);

				// hide progress in action bar
				if(mAPICallManager.getTasksCount()==0) showActionBarProgress(false);
			}
		});
	}

	private void handleError(String errorType, String errorMessage)
	{
		Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
	}


	private void handleFail(Exception exception)
	{
		int messageId;
		if(exception!=null && exception.getClass().equals(UnknownHostException.class)) messageId = R.string.global_apicall_unknown_host_toast;
		else if(exception!=null && exception.getClass().equals(FileNotFoundException.class)) messageId = R.string.global_apicall_not_found_toast;
		else if(exception!=null && exception.getClass().equals(SocketTimeoutException.class)) messageId = R.string.global_apicall_timeout_toast;
		else if(exception!=null && exception.getClass().equals(JsonParseException.class)) messageId = R.string.global_apicall_parse_fail_toast;
		else if(exception!=null && exception.getClass().equals(NumberFormatException.class)) messageId = R.string.global_apicall_parse_fail_toast;
		else if(exception!=null && exception.getClass().equals(ClassCastException.class)) messageId = R.string.global_apicall_parse_fail_toast;
		else messageId = R.string.global_apicall_fail_toast;
		Toast.makeText(getActivity(), messageId, Toast.LENGTH_LONG).show();
	}


	private void getInstagramAccessToken(String token)
	{
		if(NetworkManager.isOnline(getActivity()))
		{
			// show progress
			showProgress();

			InstagramAccessTokenRequest request = new InstagramAccessTokenRequest(token);
			mAPICallManager.executeTask(request, this);
		}
		else
		{
			showOffline();
		}
	}


	private void loadData()
	{
		if(NetworkManager.isOnline(getActivity()))
		{
			// show progress
			showProgress();

			Preferences prefs = new Preferences(getActivity());

			InstagramPhotosRequest request = new InstagramPhotosRequest(prefs.getInstagramAccessToken(), prefs.getInstagramUserId(), LAZY_LOADING_TAKE, null);
			mAPICallManager.executeTask(request, this);
		}
		else
		{
			showOffline();
		}
	}


	private void showLazyLoadingProgress(boolean visible)
	{
		if(visible)
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
		if(NetworkManager.isOnline(getActivity()))
		{
			// show lazy loading progress
			showLazyLoadingProgress(true);

			// run async task
			if(NetworkManager.isOnline(getActivity()))
			{
				Preferences prefs = new Preferences(getActivity());

				InstagramPhotosRequest request = new InstagramPhotosRequest(prefs.getInstagramAccessToken(), prefs.getInstagramUserId(), LAZY_LOADING_TAKE, mNextId);
				mAPICallManager.executeTask(request, this);
			}
			else
			{
				showOffline();
			}
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
		if(getListAdapter() == null)
		{
			// create adapter
			mAdapter = new InstagramPhotoPickerAdapter(getActivity(), mPhotoItems, this);
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
				if(totalItemCount - (firstVisibleItem + visibleItemCount) <= LAZY_LOADING_OFFSET && mPhotoItems.size() % LAZY_LOADING_TAKE == 0 && !mPhotoItems.isEmpty())
				{
					if(!mLazyLoading) lazyLoadData();
				}
			}
		});
	}


	private void showLoginDialog()
	{
		String url = ExampleConfig.INSTAGRAM_AUTH_URL + "?client_id=" + ExampleConfig.INSTAGRAM_CLIENT_ID + "&redirect_uri=" +
				ExampleConfig.INSTAGRAM_REDIRECT_URL + "&response_type=code";

		// create and show the dialog
		DialogFragment newFragment = InstagramLoginDialogFragment.newInstance(url);
		newFragment.setTargetFragment(this, 0);
		newFragment.show(getFragmentManager(), DIALOG_LOGIN);
	}
}

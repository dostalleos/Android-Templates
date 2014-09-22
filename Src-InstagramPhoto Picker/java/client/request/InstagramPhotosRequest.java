package com.example.client.request;

import com.example.ExampleConfig;
import com.example.client.parser.InstagramPhotosParser;
import com.example.client.response.Response;
import com.example.entity.InstagramPhotosEntity;
import com.example.utility.Logcat;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;


public class InstagramPhotosRequest extends Request
{
	private static final String REQUEST_METHOD = "GET";

	private String mAccessToken;
	private String mUserId;
	private int mCount;
	private String mMaxId;


	public InstagramPhotosRequest(String accessToken, String userId, int count, String maxId)
	{
		mAccessToken = accessToken;
		mUserId = userId;
		mCount = count;
		mMaxId = maxId;
	}

	@Override
	public String getRequestMethod()
	{
		return REQUEST_METHOD;
	}


	@Override
	public String getAddress()
	{
		StringBuilder builder = new StringBuilder();
		List<NameValuePair> params = new LinkedList<NameValuePair>();

		// params
		params.add(new BasicNameValuePair("access_token", mAccessToken));
		params.add(new BasicNameValuePair("count", mCount+""));
		if(mMaxId != null)
		{
			params.add(new BasicNameValuePair("max_id", mMaxId));
		}
		String paramsString = URLEncodedUtils.format(params, CHARSET);

		// url
		builder.append(ExampleConfig.INSTAGRAM_API_URL);
		builder.append("users/");
		builder.append(mUserId);
		builder.append("/media/recent");
		if(paramsString!=null && !paramsString.equals(""))
		{
			builder.append("?");
			builder.append(paramsString);
		}
		Logcat.e("URL " + builder.toString());
		return builder.toString();
	}


	@Override
	public Response<InstagramPhotosEntity> parseResponse(InputStream stream) throws IOException
	{
		return InstagramPhotosParser.parse(stream);
	}


	@Override
	public byte[] getContent()
	{
		return null;
	}


	@Override
	public String getBasicAuthUsername()
	{
		return null;
	}


	@Override
	public String getBasicAuthPassword()
	{
		return null;
	}
}
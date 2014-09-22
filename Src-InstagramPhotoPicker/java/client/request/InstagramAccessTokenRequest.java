package com.example.client.request;

import com.example.ExampleConfig;
import com.example.client.parser.InstagramAccessTokenParser;
import com.example.client.response.Response;
import com.example.entity.InstagramAccessTokenEntity;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;


public class InstagramAccessTokenRequest extends Request
{
	private static final String REQUEST_METHOD = "POST";

	private String mToken;

	public InstagramAccessTokenRequest(String token)
	{
		mToken = token;
	}

	@Override
	public String getRequestMethod()
	{
		return REQUEST_METHOD;
	}


	@Override
	public String getAddress()
	{
		return ExampleConfig.INSTAGRAM_TOKEN_URL;
	}


	@Override
	public Response<InstagramAccessTokenEntity> parseResponse(InputStream stream) throws IOException
	{
		return InstagramAccessTokenParser.parse(stream);
	}


	@Override
	public byte[] getContent()
	{
		StringBuilder builder = new StringBuilder();
		List<NameValuePair> params = new LinkedList<NameValuePair>();

		// params
		params.add(new BasicNameValuePair("client_id", ExampleConfig.INSTAGRAM_CLIENT_ID));
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
		params.add(new BasicNameValuePair("client_secret", ExampleConfig.INSTAGRAM_CLIENT_SECRET));
		params.add(new BasicNameValuePair("code", mToken));
		params.add(new BasicNameValuePair("redirect_uri", ExampleConfig.INSTAGRAM_REDIRECT_URL));
		String paramsString = URLEncodedUtils.format(params, CHARSET);
		builder.append(paramsString);

		return builder.toString().getBytes();
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
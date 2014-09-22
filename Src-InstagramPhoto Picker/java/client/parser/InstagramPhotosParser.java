package com.example.client.parser;


import com.google.gson.JsonIOException;
import com.example.ExampleApplication;
import com.example.R;
import com.example.client.response.Response;
import com.example.entity.InstagramPhotosEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class InstagramPhotosParser
{
	public static Response<InstagramPhotosEntity> parse(InputStream stream) throws JsonIOException
	{
		Response<InstagramPhotosEntity> response = new Response<InstagramPhotosEntity>();
		InstagramPhotosEntity photos = new InstagramPhotosEntity();
		ArrayList<String> photoList = new ArrayList<String>();

		try
		{
			JSONObject jsonObject = (JSONObject) new JSONTokener(streamToString(stream)).nextValue();
			JSONObject jsonPaging = jsonObject.getJSONObject("pagination");
			String next = "";
			if(jsonPaging.has("next_max_id"))
				next = jsonPaging.getString("next_max_id");
			photos.setNextMaxId(next);

			JSONArray photosArray = jsonObject.getJSONArray("data");
			for(int i = 0; i < photosArray.length(); i++)
			{
				JSONObject jsonPhoto = photosArray.getJSONObject(i).getJSONObject("images").getJSONObject("standard_resolution");
				photoList.add(jsonPhoto.getString("url"));
			}
			photos.setPhotos(photoList);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
			photos = null;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			photos = null;
		}
		catch(ClassCastException e)
		{
			e.printStackTrace();
			photos = null;
		}
		response.setResponseObject(photos);

		if(response.getResponseObject() == null)
		{
			response.setError(true);
			response.setErrorType("");
			response.setErrorMessage(ExampleeApplication.getContext().getString(R.string.global_apicall_fail_toast));
		}

		return response;
	}

	public static String streamToString(InputStream is) throws IOException
	{
		String string = "";
		if (is != null)
		{
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				while ((line = reader.readLine()) != null)
				{
					stringBuilder.append(line);
				}
				reader.close();
			}
			finally
			{
				is.close();
			}
			string = stringBuilder.toString();
		}
		return string;
	}
}

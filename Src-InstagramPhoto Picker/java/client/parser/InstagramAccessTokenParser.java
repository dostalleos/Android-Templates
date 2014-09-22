package com.example.client.parser;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.example.client.response.Response;
import com.example.entity.InstagramAccessTokenEntity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class InstagramAccessTokenParser
{
	public static Response<InstagramAccessTokenEntity> parse(InputStream stream) throws JsonIOException
	{
		Response<InstagramAccessTokenEntity> response = new Response<InstagramAccessTokenEntity>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		Gson gson = new GsonBuilder().create();
		response.setResponseObject(gson.fromJson(reader, InstagramAccessTokenEntity.class));

		if(response.getResponseObject() != null && response.getResponseObject().getErrorMessage() != null)
		{
			response.setError(true);
			response.setErrorType("");
			response.setErrorMessage(response.getResponseObject().getErrorMessage());
		}

		return response;
	}
}

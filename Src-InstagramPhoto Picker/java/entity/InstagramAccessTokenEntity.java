package com.example.entity;


import com.google.gson.annotations.SerializedName;


public class InstagramAccessTokenEntity
{
	@SerializedName("access_token")
	private String accessToken;
	@SerializedName("user")
	private InstagramUserEntity user;
	@SerializedName("error_message")
	private String errorMessage;


	public String getAccessToken()
	{
		return accessToken;
	}


	public void setAccessToken(String accessToken)
	{
		this.accessToken = accessToken;
	}


	public String getErrorMessage()
	{
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}


	public InstagramUserEntity getUser()
	{
		return user;
	}


	public void setUser(InstagramUserEntity user)
	{
		this.user = user;
	}
}

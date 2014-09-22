package com.example.entity;


import com.google.gson.annotations.SerializedName;


public class InstagramUserEntity
{
	@SerializedName("id")
	private String userId;


	public String getUserId()
	{
		return userId;
	}


	public void setUserId(String userId)
	{
		this.userId = userId;
	}
}

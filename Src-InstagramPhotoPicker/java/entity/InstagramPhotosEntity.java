package com.example.entity;


import java.util.ArrayList;


public class InstagramPhotosEntity
{
	private ArrayList<String> photos;
	private String nextMaxId;


	public ArrayList<String> getPhotos()
	{
		return photos;
	}


	public void setPhotos(ArrayList<String> photos)
	{
		this.photos = photos;
	}


	public String getNextMaxId()
	{
		return nextMaxId;
	}


	public void setNextMaxId(String nextMaxId)
	{
		this.nextMaxId = nextMaxId;
	}}

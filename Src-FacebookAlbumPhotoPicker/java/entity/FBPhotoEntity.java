package com.example.entity;

public class FBPhotoEntity
{

	private String id;
	private String url;

	public FBPhotoEntity(String id, String url)
	{
		super();
		this.id = id;
		this.url = url;
	}

	public FBPhotoEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
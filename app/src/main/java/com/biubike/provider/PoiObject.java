package com.biubike.provider;

public class PoiObject {

	public String address;
	public String lattitude;
	public String longitude;
	public String district;

	public PoiObject(String address, String lattitude, String longitude,String district) {
		this.address = address;
		this.lattitude = lattitude;
		this.longitude = longitude;
		this.district=district;
	}

}

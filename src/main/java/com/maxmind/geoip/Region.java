package com.maxmind.geoip;

public class Region{
	public String countryCode;
	public String countryName;
	public String region;

	@Override
	public String toString() {
		return "Region{" +
				"countryCode='" + countryCode + '\'' +
				", countryName='" + countryName + '\'' +
				", region='" + region + '\'' +
				'}';
	}
}


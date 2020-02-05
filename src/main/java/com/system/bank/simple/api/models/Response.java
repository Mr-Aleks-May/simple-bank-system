package com.system.bank.simple.api.models;

import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Response {
	private StringBuilder sb = null;

	public Response() {
		sb = new StringBuilder("{}");
	}

	public Response add(String key, String value) {
		sb.insert(sb.length() - 1, String.format("\"%s\":\"%s\",", key, value));
		return this;
	}

	public Response add(String key, byte value) {
		sb.insert(sb.length() - 1, String.format("\"%s\":%d,", key, value));
		return this;
	}

	public Response add(String key, int value) {
		sb.insert(sb.length() - 1, String.format("\"%s\":%d,", key, value));
		return this;
	}

	public Response add(String key, long value) {
		sb.insert(sb.length() - 1, String.format("\"%s\":%d,", key, value));
		return this;
	}

	public Response add(String key, float value) {
		sb.insert(sb.length() - 1, String.format("\"%s\":%d,", key, value));
		return this;
	}

	public Response add(String key, double value) {
		sb.insert(sb.length() - 1, String.format("\"%s\":%d,", key, value));
		return this;
	}

	public Response add(String key, BigDecimal value) {
		sb.insert(sb.length() - 1, String.format("\"%s\":%s,", key, value.toString()));
		return this;
	}

	public <T> Response add(String key, T value) {
		Gson gsonBuilder = new GsonBuilder().create();
		String json = gsonBuilder.toJson(value);
		sb.insert(sb.length() - 1, "\"" + key + "\":" + json + ",");
		return this;
	}

	public String toJSON() {
		return toString();
	}

	@Override
	public String toString() {
		if (sb.length() > 2)
			return sb.deleteCharAt(sb.length() - 2).toString(); // Delete ','
		else
			return sb.toString();
	}
}

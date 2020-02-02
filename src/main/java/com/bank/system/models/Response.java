package com.bank.system.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Response {
	private Map<String, String> params;

	public Response() {
		params = new HashMap<String, String>();
	}

	public Response add(String key, String value) {
		params.put(key, value);
		return this;
	}

	public Response add(String key, int value) {
		params.put(key, value + "");
		return this;
	}

	public Response add(String key, long value) {
		params.put(key, value + "");
		return this;
	}

	public Response add(String key, Object value) {
		params.put(key, value.toString());
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{");
		for (Entry<String, String> param : params.entrySet()) {
			sb.append(String.format("\"%s\":\"%s\",", param.getKey(), param.getValue()));
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}");

		return sb.toString();
	}
}

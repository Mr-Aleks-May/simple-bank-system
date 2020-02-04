package com.system.bank.simple.api.models;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	public <T> Response add(String key, List<T> value) {
		try {
			final StringWriter sw = new StringWriter();
			final ObjectMapper mapper = new ObjectMapper();

			mapper.writeValue(sw, value);

			sb.insert(sb.length() - 1, "\"" + key + "\":" + sw.toString() + ",");
		} catch (IOException e) {
			sb.insert(sb.length() - 1, "\"" + key + "\":null,");
			e.printStackTrace();
		}

		return this;
	}

	public String toJSON() {
		if (sb.length() > 2)
			return sb.deleteCharAt(sb.length() - 2).toString(); // Delete ','
		else
			return sb.toString();
	}

	@Override
	public String toString() {
		return toJSON();
	}
}

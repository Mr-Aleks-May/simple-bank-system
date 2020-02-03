package com.bank.system.models;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.bank.system.controllers.ResultSetSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

	public Response add(String key, BigDecimal value) {
		params.put(key, value.toString());
		return this;
	}

	public Response add(String key, ResultSet value) {
		try {
			SimpleModule module = new SimpleModule();
			module.addSerializer(new ResultSetSerializer());

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(module);

			// Use the DataBind Api here
			ObjectNode objectNode = objectMapper.createObjectNode();

			// put the resultset in a containing structure
			objectNode.putPOJO("results", value);

			// generate all
			StringWriter sw = new StringWriter();
			objectMapper.writeValue(sw, objectNode);

			this.params.put(key, sw.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return this;
	}

	public <T> Response add(String key, T value) {
		try {
			JSONObject obj = new JSONObject();
			obj.put(key, value);

			StringWriter sw = new StringWriter();
			obj.writeJSONString(sw);

			params.put(key, sw.toString());
		} catch (IOException e) {
			e.printStackTrace();
			params.put(key, "NULL");
		}
		return this;
	}

	public String toJSON() {
		return toString();
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

package com.github.karlnicholas.djsdist.client;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ObjectMapperWrapper {
	ObjectMapper objectMapper;
	public ObjectMapperWrapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	public <T> T readValue(String content, Class<T> t) {
		try {
			return objectMapper.readValue(content, t);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public <T> T treeToValue(TreeNode treeNode, Class<T> t) {
		try {
			return objectMapper.treeToValue(treeNode, t);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public String writeValueAsString(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public <T> T readValue(InputStream src, Class<T> valueType) {
		try {
			return objectMapper.readValue(src, valueType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

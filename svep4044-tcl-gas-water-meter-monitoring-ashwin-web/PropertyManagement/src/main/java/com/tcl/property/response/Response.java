package com.tcl.property.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_EMPTY)
public class Response<T> {

	public Response(Integer status, String message,Boolean error) {
		super();
		this.status = status;
		this.message = message;
		this.error=error;
	}

	public Response(Integer status, T data, String message,Boolean error) {
		super();
		this.status = status;
		this.data = data;
		this.message = message;
		this.error=error;
	}

	public Response(T data) {
		super();
		this.data = data;
	}

	public Response() {
	}

	Integer status;
	T data;
	String message;
	Boolean error;

	public Boolean getError() {
		return error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	public Integer getStatus() {
		return status;
	}

	public T getData() {
		return data;
	}

	public String getMessage() {
		return message;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public void setData(T data) {
		this.data = data;
	}


	public void setMessage(String message) {
		this.message = message;
	}

}

package org.whispersystems.textsecuregcm.mybatis.entity;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResponseEntity<T> implements Serializable {

	private static final long serialVersionUID = -5959572656768791879L;

	public static final Integer CODE_SUCCESS = 200;
	public static final String MSG_SUCCESS = "成功";

	private Map<String, Object> data = new LinkedHashMap<String, Object>();

	private Integer code = 1;
	private String message;
	private List<T> list;
	private Integer pageNo = null;
	private Integer pageSize = null;
	private Integer count = null;
	private Integer pages = null;

	public ResponseEntity() {
	}

	public ResponseEntity(T data) {
		this.data.put("data", data);
		this.code = CODE_SUCCESS;
		this.message = MSG_SUCCESS;
	}

	public ResponseEntity(List<T> list, int count, int pageNo, int pageSize) {
		this.data.put("list", list);
		this.count = count;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.pages = (count + pageSize - 1) / pageSize;
		this.code = CODE_SUCCESS;
		this.message = MSG_SUCCESS;
	}

	public ResponseEntity(T data, int count, BaseModel baseModel) {
		this.data.put("data", data);
		this.data.put("count", count);
		this.data.put("pageNo", baseModel.getPageNo());
		this.data.put("pageSize", baseModel.getPageSize());
		this.data.put("pages", (count + baseModel.getPageSize() - 1) / baseModel.getPageSize());
		this.code = CODE_SUCCESS;
		this.message = MSG_SUCCESS;
	}

	public ResponseEntity(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getPages() {
		return pages;
	}

	public void setPages(Integer pages) {
		this.pages = pages;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}
}

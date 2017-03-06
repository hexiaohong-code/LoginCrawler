package com.web.crawler.login.model;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * the result entity
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class HttpResult implements Serializable {

    private int statusCode;

    private String body;

    private String location;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String toString() {
        return "the deal result is " + JSONObject.toJSONString(this);
    }

}
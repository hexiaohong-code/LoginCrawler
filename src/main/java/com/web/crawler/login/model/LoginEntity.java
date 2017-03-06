package com.web.crawler.login.model;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class LoginEntity implements Serializable {

    private String loginUrl;// 登录网址

    private String actionUrl;// 请求网址

    private String codeUrl;// 验证码地址

    private String codeName;// 验证码命名

    private String cookie;// 登录页面的Cookie

    private Map<String, Object> params;// 用户输入的表单参数

    private Header[] headers;// 需设置的头部

    private String mark;// 登录失败的提示语(例如“用户名不为空;密码错误”)

    private String charset = "UTF-8";// 字符编码

    private String codePath = ConstantDefine.Code.PATH;// 验证码存储绝对路径

    private boolean unEscape;// 是否需要反转义字符串

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCodePath() {
        return codePath;
    }

    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public boolean isUnEscape() {
        return unEscape;
    }

    public void setUnEscape(boolean unEscape) {
        this.unEscape = unEscape;
    }

    public String toString() {
        return "the deal result is " + JSONObject.toJSONString(this);
    }
}

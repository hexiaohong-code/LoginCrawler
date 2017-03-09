package com.web.crawler.login.module.impl;

import com.web.crawler.login.model.ConstantDefine;
import com.web.crawler.login.model.HttpResult;
import com.web.crawler.login.model.LoginEntity;
import com.web.crawler.login.module.LoginModule;
import com.web.crawler.login.utils.LoginUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The login module
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class LoginModuleImpl implements LoginModule {

    private static final Logger logger = LoggerFactory.getLogger(LoginModuleImpl.class);

    public HttpResult access(LoginEntity loginEntity) {

        HttpResult httpResult = new HttpResult();
        CloseableHttpClient httpClient = LoginUtils.createHttpClient(loginEntity.getLoginUrl());
        HttpGet httpGet = null;
        try {

            httpGet = new HttpGet(loginEntity.getLoginUrl());
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(ConstantDefine.Http.DEFAULTTIMEOUT).setConnectionRequestTimeout(ConstantDefine.Http.DEFAULTTIMEOUT)
                    .setSocketTimeout(ConstantDefine.Http.DEFAULTTIMEOUT).setRedirectsEnabled(false).build();
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            httpResult.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            httpResult.setBody(EntityUtils.toString(httpEntity, loginEntity.getCharset()));
            String location = LoginUtils.getLocation(httpResponse);

            if (httpResult.getStatusCode() == 200) {
                loginEntity.setCookie(LoginUtils.getCookie(httpResponse));
                Map<String, Object> hiddenForm = LoginUtils.getFormMap(httpResult.getBody());
                if (loginEntity.getParams() != null) {
                    hiddenForm.putAll(loginEntity.getParams());
                }
                if (StringUtils.isBlank(loginEntity.getActionUrl())) {
                    loginEntity.setActionUrl(LoginUtils.getAbsolutePath(loginEntity.getLoginUrl(), String.valueOf(hiddenForm.get("formAction"))));
                }
                loginEntity.setParams(hiddenForm);
            }

            EntityUtils.consume(httpEntity);
            httpResponse.close();

            if (httpResult.getStatusCode() == 302) {
                location = LoginUtils.getAbsolutePath(loginEntity.getLoginUrl(), location);
                loginEntity.setLoginUrl(location);
                httpResult = access(loginEntity);
                httpResult.setLocation(location);
            }
        } catch (Exception e) {
            logger.info("failed open the url of login,the failed reason is ：", e);
            httpResult.setStatusCode(500);
            httpResult.setBody(e.getMessage());
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return httpResult;
    }

    public HttpResult request(LoginEntity loginEntity) {

        HttpResult httpResult = new HttpResult();
        CloseableHttpClient httpClient = LoginUtils.createHttpClient(loginEntity.getActionUrl());
        HttpPost httpPost = null;
        try {

            logger.info("Request url：{}", loginEntity.getActionUrl());
            httpPost = new HttpPost(loginEntity.getActionUrl());

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(ConstantDefine.Http.DEFAULTTIMEOUT).setConnectionRequestTimeout(ConstantDefine.Http.DEFAULTTIMEOUT)
                    .setSocketTimeout(ConstantDefine.Http.DEFAULTTIMEOUT).build();
            httpPost.setConfig(requestConfig);

            httpPost.setHeader("Cookie", loginEntity.getCookie());
            if (loginEntity.getHeaders() != null) {
                httpPost.setHeaders(loginEntity.getHeaders());
            }
            logger.info("Request head：");
            Header[] headers = httpPost.getAllHeaders();
            if (headers != null) {
                for (Header header : headers) {
                    logger.info(header.getName() + ":" + header.getValue());
                }
            }

            httpPost.setHeader("X-Forwarded-For",LoginUtils.getIp());

            logger.info("Request params：{}", loginEntity.getParams());
            LoginUtils.setParams(httpPost, loginEntity.getParams(), loginEntity.getCharset());

            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            String html = EntityUtils.toString(httpEntity, loginEntity.getCharset());
            if (loginEntity.isUnEscape()) {
                html = StringEscapeUtils.unescapeJava(html);
            }
            logger.info("Request the results:{}", html);
            String errorTip = LoginUtils.checkLogin(html, loginEntity.getMark());
            if (StringUtils.isNotBlank(errorTip)) {
                httpResult.setStatusCode(401);
                httpResult.setBody(errorTip);
                return httpResult;
            }

            httpResult.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            httpResult.setBody(html);

            int statusCode = httpResult.getStatusCode();
            if (statusCode == 200 || statusCode == 302) {
                String cookie = LoginUtils.getCookie(httpResponse);
                if (StringUtils.isNotBlank(cookie)) {
                    loginEntity.setCookie(cookie);
                }
            }

            if (statusCode == 302) {
                String location = LoginUtils.getAbsolutePath(loginEntity.getLoginUrl(), LoginUtils.getLocation(httpResponse));
                httpResult.setLocation(location);
            }

            httpResponse.close();
            EntityUtils.consume(httpEntity);
        } catch (Exception e) {
            logger.info("the failed reason is ：", e);
            httpResult.setStatusCode(500);
            httpResult.setBody(e.getMessage());
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return httpResult;
    }
}

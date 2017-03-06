package com.web.crawler.login.module.impl;

import com.web.crawler.login.model.ConstantDefine;
import com.web.crawler.login.model.HttpResult;
import com.web.crawler.login.model.LoginEntity;
import com.web.crawler.login.module.CodeModule;
import com.web.crawler.login.module.LoginModule;
import com.web.crawler.login.utils.LoginUtils;
import com.web.crawler.login.utils.MD5Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Captcha module
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class CodeModuleImpl implements CodeModule {

    private static final Logger logger = LoggerFactory.getLogger(CodeModuleImpl.class);

    public HttpResult download(LoginEntity loginEntity) {

        HttpResult httpResult = new HttpResult();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = null;
        try {

            LoginModule loginModule = new LoginModuleImpl();
            httpResult = loginModule.access(loginEntity);
            if (httpResult.getStatusCode() != 200) {
                return httpResult;
            }

            httpGet = new HttpGet(loginEntity.getCodeUrl());
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(ConstantDefine.Http.DEFAULTTIMEOUT).setConnectionRequestTimeout(ConstantDefine.Http.DEFAULTTIMEOUT)
                    .setSocketTimeout(ConstantDefine.Http.DEFAULTTIMEOUT).build();
            httpGet.setConfig(requestConfig);

            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();

            httpResult.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            if (httpResult.getStatusCode() != 200) {
                return httpResult;
            }

            loginEntity.setCookie(LoginUtils.getCookie(httpResponse));

            if (StringUtils.isBlank(loginEntity.getCodeName())) {
                loginEntity.setCodeName(MD5Utils.digest(loginEntity.getCodeUrl()) + ".jpg");
            }

            File file = new File(loginEntity.getCodePath());
            if (!file.exists()) {
                file.mkdir();
            }

            String filePath = loginEntity.getCodePath() + (loginEntity.getCodePath().endsWith("/") ? "" : "/") + loginEntity.getCodeName();
            file = new File(filePath);
            OutputStream outputStream = new FileOutputStream(file);
            InputStream inputStream = httpEntity.getContent();

            byte[] bytes = new byte[1024];
            int numReadByte = 0;
            while ((numReadByte = inputStream.read(bytes, 0, 1024)) > 0) {
                outputStream.write(bytes, 0, numReadByte);
            }

            outputStream.close();
            inputStream.close();

            EntityUtils.consume(httpEntity);
            httpResponse.close();

        } catch (Exception e) {
            logger.info("failed crawl,the failed reason is ：", e);
            httpResult.setStatusCode(500);
            httpResult.setBody(e.getMessage());
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return httpResult;
    }
}

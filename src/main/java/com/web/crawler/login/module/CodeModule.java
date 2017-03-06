package com.web.crawler.login.module;

import com.web.crawler.login.model.HttpResult;
import com.web.crawler.login.model.LoginEntity;

/**
 * Captcha module
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public interface CodeModule {

    /**
     * Download verification code (captcha images saved to codePath directory)
     *
     * @param loginEntity
     * @return
     */
    HttpResult download(LoginEntity loginEntity);
}

package com.web.crawler.login.module;


import com.web.crawler.login.model.HttpResult;
import com.web.crawler.login.model.LoginEntity;

/**
 * The login module
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public interface LoginModule {

    /**
     * Access to the login page and encapsulate the login page hidden form, and cookies
     *
     * @param loginEntity
     * @return
     */
    HttpResult access(LoginEntity loginEntity);

    /**
     * Login request
     *
     * @param loginEntity
     * @return
     */
    HttpResult request(LoginEntity loginEntity);
}

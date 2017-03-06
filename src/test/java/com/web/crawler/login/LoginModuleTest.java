package com.web.crawler.login;


import com.web.crawler.login.model.HttpResult;
import com.web.crawler.login.model.LoginEntity;
import com.web.crawler.login.module.LoginModule;
import com.web.crawler.login.module.impl.LoginModuleImpl;

/**
 * 测试成功
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class LoginModuleTest {
    public static void main(String[] args) {
        LoginModule loginModule = new LoginModuleImpl();
        LoginEntity loginEntity = new LoginEntity();
        loginEntity.setLoginUrl("http://jwc.xcc.sc.cn/default2.aspx");
        HttpResult httpResult = loginModule.access(loginEntity);
        System.out.println(loginEntity.toString());
    }
}

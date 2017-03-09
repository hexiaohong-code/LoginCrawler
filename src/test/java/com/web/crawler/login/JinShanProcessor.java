package com.web.crawler.login;

import com.web.crawler.login.model.LoginEntity;
import com.web.crawler.login.processor.PageLoginProcessor;
import com.web.crawler.login.utils.LoginUtils;
import com.web.crawler.login.utils.MD5Utils;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 知乎网站登录（不需要验证码,测试成功）
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class JinShanProcessor implements PageLoginProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JinShanProcessor.class);

    @Override
    public void process(Page page) {
        logger.info(page.getHtml().get());
    }

    @Override
    public void setLoginEntity(LoginEntity loginEntity) {
        Map<String, Object> params = new HashedMap();
        params.put("user", "fc7kj");
        params.put("pwd", MD5Utils.digest("zhang520"));
        params.put("service", "");
        params.put("rm", "1");
        params.put("cn", "b06aa38e7641826933720e37f481ac7c");
        loginEntity.setParams(params);
        loginEntity.setLoginUrl("https://login.ijinshan.com/login.html");
        loginEntity.setActionUrl("https://login.ijinshan.com/login");
        loginEntity.setCodeUrl("https://login.ijinshan.com/imgCode?_dc=" + (System.currentTimeMillis()) + "&cn=b06aa38e7641826933720e37f481ac7c");
        loginEntity.setCharset("UTF-8");
        loginEntity.setUnEscape(true);
        loginEntity.setMark("用户名或密码错误;验证错误;验证码错误;登录失败;");
        loginEntity.setActionUrl("https://login.ijinshan.com/login");
    }

    @Override
    public void beforeRequestLogin(LoginEntity loginEntity) {
        List<String> urlList = new ArrayList<String>();
        urlList.add("https://login.ijinshan.com/glt?_lt=" + System.currentTimeMillis());
        loginEntity.setUrlBeforLogin(urlList);
    }

    @Override
    public void setSite(Site site) {

    }

    public static void main(String[] args) {
        SpiderLogin spiderLogin = SpiderLogin.create(new JinShanProcessor());
        if (!spiderLogin.downloadCode()) {
            return;
        }
        // 输入验证码
        spiderLogin.setCodeValue("cc", LoginUtils.getInputStr());
        if (spiderLogin.startlogin()) {
            spiderLogin.addUrl("http://i.ijinshan.com/welcome").start();
        }
    }
}

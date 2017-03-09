package com.web.crawler.login;

import com.web.crawler.login.model.LoginEntity;
import com.web.crawler.login.processor.PageLoginProcessor;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;

import java.util.Map;

/**
 * 知乎网站登录（不需要验证码,测试成功）
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class ZhiHuProcessor implements PageLoginProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ZhiHuProcessor.class);

    @Override
    public void process(Page page) {
        logger.info(page.getHtml().get());
    }

    @Override
    public void setLoginEntity(LoginEntity loginEntity) {
        Map<String, Object> params = new HashedMap();
        params.put("phone_num", "15918726361");
        params.put("password", "2011626504");
        loginEntity.setParams(params);
        loginEntity.setLoginUrl("https://www.zhihu.com/");
        loginEntity.setActionUrl("https://www.zhihu.com/login/phone_num");
        loginEntity.setCharset("UTF-8");
        loginEntity.setUnEscape(true);
        loginEntity.setMark("账号或密码错误;不存在;密码错误;登录过于频繁;");
    }

    @Override
    public void setSite(Site site) {

    }

    public static void main(String[] args) {
        SpiderLogin spiderLogin = SpiderLogin.create(new ZhiHuProcessor());
        if (spiderLogin.startlogin()) {
            spiderLogin.addUrl(spiderLogin.getLoginIndexUrl()).start();
        }
    }
}

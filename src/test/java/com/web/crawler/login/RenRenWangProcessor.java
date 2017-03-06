package com.web.crawler.login;

import com.web.crawler.login.model.LoginEntity;
import com.web.crawler.login.processor.PageLoginProcessor;
import com.web.crawler.login.utils.LoginUtils;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;

import java.util.Map;

/**
 * （人人网登录（不需要验证码,测试成功）
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class RenRenWangProcessor implements PageLoginProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RenRenWangProcessor.class);

    @Override
    public void process(Page page) {
        logger.info(page.getHtml().get());
    }

    @Override
    public void setLoginEntity(LoginEntity loginEntity) {
        Map<String, Object> params = new HashedMap();
        params.put("email", "15918726361");
        params.put("password", "2011626504");
        loginEntity.setParams(params);
        loginEntity.setLoginUrl("http://www.renren.com/");
        loginEntity.setActionUrl("http://www.renren.com/ajaxLogin/login?1=1&uniqueTimestamp=2017202025800");
        loginEntity.setCharset("UTF-8");
        loginEntity.setCodeUrl("http://icode.renren.com/getcode.do?t=web_login&rnd=Math.random()");
        loginEntity.setMark("您的用户名和密码不匹配;验证码不正确;");
    }

    @Override
    public void setSite(Site site) {

    }

    public static void main(String[] args) {
        SpiderLogin spiderLogin = SpiderLogin.create(new RenRenWangProcessor());
        if (!spiderLogin.downloadCode()) {
            return;
        }
        spiderLogin.setCodeValue("icode", LoginUtils.getInputStr());
        if (spiderLogin.startlogin()) {
            spiderLogin.addUrl(spiderLogin.getLoginIndex()).start();
        }
    }
}

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
        params.put("user", "d4woe");
        params.put("pwd", MD5Utils.digest("zhang520"));
        params.put("service", "");
        params.put("rm", "1");
        params.put("cn", "b06aa38e7641826933720e37f481ac7c");
        loginEntity.setParams(params);
        loginEntity.setLoginUrl("https://login.ijinshan.com/glt?_lt="+System.currentTimeMillis());
        loginEntity.setActionUrl("https://login.ijinshan.com/login");
        loginEntity.setCodeUrl("https://login.ijinshan.com/imgCode?_dc="+System.currentTimeMillis()+"&cn=b06aa38e7641826933720e37f481ac7c");
        loginEntity.setCharset("UTF-8");
        loginEntity.setUnEscape(true);
        loginEntity.setMark("账号或密码错误;不存在;密码错误;登录过于频繁;登录失败;");
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
        //spiderLogin.setCodeValue("cc", LoginUtils.getInputStr());
        spiderLogin.setCodeValue("cc", "5641");// 此处不校验验证码,有漏洞,所以随便输入即可
        if (spiderLogin.startlogin()) {
            spiderLogin.addUrl(spiderLogin.getLoginIndex()).start();
        }
    }
}

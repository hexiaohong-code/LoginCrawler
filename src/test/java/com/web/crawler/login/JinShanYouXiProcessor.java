package com.web.crawler.login;

import com.alibaba.fastjson.JSONObject;
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
public class JinShanYouXiProcessor implements PageLoginProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JinShanYouXiProcessor.class);

    @Override
    public void process(Page page) {
        logger.info(page.getHtml().get());
    }

    @Override
    public void setLoginEntity(LoginEntity loginEntity) {
        Map<String, Object> params = new HashedMap();
        params.put("user", "fc7kj");
        params.put("pwd", MD5Utils.digest("zhang520"));
        params.put("service", "http://i.wan.liebao.cn/login?go=http://wan.liebao.cn/game_frame/play_1087.php?sid=22&supplier_id=2");
        params.put("rm", "1");
        params.put("cn", "a6742619ae9d91bfb87b0f9507c8d0ad");
        loginEntity.setParams(params);
        loginEntity.setLoginUrl("https://login.ijinshan.com/webgame/w/loginpage.html");
        loginEntity.setActionUrl("https://login.ijinshan.com/login");
        loginEntity.setCodeUrl("https://login.ijinshan.com/imgCode?_dc=" + (System.currentTimeMillis()) + "&cn=a6742619ae9d91bfb87b0f9507c8d0ad");
        loginEntity.setCharset("UTF-8");
        loginEntity.setUnEscape(true);
        loginEntity.setMark("用户名或密码错误;验证错误;验证码错误;登录失败;");
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
        SpiderLogin spiderLogin = SpiderLogin.create(new JinShanYouXiProcessor());
        if (!spiderLogin.downloadCode()) {
            return;
        }
        // 输入验证码
        spiderLogin.setCodeValue("cc", LoginUtils.getInputStr());
        if (spiderLogin.startlogin()) {
            JSONObject jsonObject = JSONObject.parseObject(spiderLogin.getResponseBody());
            String url = String.valueOf(jsonObject.get("url"));
            spiderLogin.addUrl(url).start();
        }
    }
}

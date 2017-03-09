package com.web.crawler.login;

import com.web.crawler.login.model.LoginEntity;
import com.web.crawler.login.processor.PageLoginProcessor;
import com.web.crawler.login.utils.LoginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;

import java.util.HashMap;
import java.util.Map;

/**
 * 正方教务系统（需要验证码,测试成功）
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class ZhengFangProcessor implements PageLoginProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ZhengFangProcessor.class);

    @Override
    public void process(Page page) {
        logger.info(page.getHtml().get());
    }

    @Override
    public void setLoginEntity(LoginEntity loginEntity) {
        Map<String, Object> studentMessage = new HashMap<String, Object>();
        // 为了安全,此处不提供正确的账号密码
        studentMessage.put("TextBox1", "2015180277");
        studentMessage.put("TextBox2", "44522119988191059");
        studentMessage.put("RadioButtonList1", "学生");
        loginEntity.setLoginUrl("http://61.142.33.204/default2.aspx");
        loginEntity.setCodeUrl("http://61.142.33.204/CheckCode.aspx");
        loginEntity.setParams(studentMessage);
        loginEntity.setMark("验证码不能为空;验证码不正确;用户名不存在;密码错误");
        loginEntity.setCharset("GBK");
        loginEntity.setUnEscape(true);
    }

    @Override
    public void beforeRequestLogin(LoginEntity loginEntity) {

    }

    @Override
    public void setSite(Site site) {
        site.setRetryTimes(1);
    }

    public static void main(String[] args) {
        SpiderLogin spiderLogin = SpiderLogin.create(new ZhengFangProcessor());
        if (!spiderLogin.downloadCode()) {
            return;
        }
        // 输入验证码
        spiderLogin.setCodeValue("TextBox3", LoginUtils.getInputStr());
        if (spiderLogin.startlogin()) {
            spiderLogin.addUrl(spiderLogin.getLoginIndexUrl()).start();
        }
    }
}

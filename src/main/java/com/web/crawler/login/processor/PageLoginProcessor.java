package com.web.crawler.login.processor;

import com.web.crawler.login.model.LoginEntity;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;

/**
 * login crawl processor
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public interface PageLoginProcessor {

    /**
     * process the page, extract urls to fetch, extract the data and store
     *
     * @param page page
     */
    void process(Page page);

    /**
     * Set the login information
     */
    void setLoginEntity(LoginEntity loginEntity);

    /**
     * The requested login before action
     *
     * @param loginEntity
     */
    void beforeRequestLogin(LoginEntity loginEntity);

    /**
     * set the site
     *
     * @param site
     * @return site
     * @see Site
     */
    void setSite(Site site);
}

package com.web.crawler.login;

import com.web.crawler.login.model.HttpResult;
import com.web.crawler.login.model.LoginEntity;
import com.web.crawler.login.module.CodeModule;
import com.web.crawler.login.module.LoginModule;
import com.web.crawler.login.module.impl.CodeModuleImpl;
import com.web.crawler.login.module.impl.LoginModuleImpl;
import com.web.crawler.login.processor.PageLoginProcessor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.CollectorPipeline;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.pipeline.ResultItemsCollectorPipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.scheduler.Scheduler;
import us.codecraft.webmagic.thread.CountableThreadPool;
import us.codecraft.webmagic.utils.UrlUtils;
import us.codecraft.webmagic.utils.WMCollections;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * To adjust the author spiders before
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class SpiderLogin implements Runnable, Task {

    protected Downloader downloader;

    protected List<Pipeline> pipelines = new ArrayList<Pipeline>();

    protected PageLoginProcessor pageLoginProcessor;

    protected List<Request> startRequests;

    protected Site site = Site.me();

    protected String uuid;

    protected Scheduler scheduler = new QueueScheduler();

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected CountableThreadPool threadPool;

    protected ExecutorService executorService;

    protected int threadNum = 1;

    protected AtomicInteger stat = new AtomicInteger(STAT_INIT);

    protected boolean exitWhenComplete = true;

    protected final static int STAT_INIT = 0;

    protected final static int STAT_RUNNING = 1;

    protected final static int STAT_STOPPED = 2;

    protected boolean spawnUrl = true;

    protected boolean destroyWhenExit = true;

    private ReentrantLock newUrlLock = new ReentrantLock();

    private Condition newUrlCondition = newUrlLock.newCondition();

    private List<SpiderListener> spiderListeners;

    private final AtomicLong pageCount = new AtomicLong(0);

    private Date startTime;

    private int emptySleepTime = 30000;

    private LoginEntity loginEntity = new LoginEntity();

    private String loginIndexUrl;

    private String responseBody;

    /**
     * create a spider with pageProcessor.
     *
     * @param pageLoginProcessor pageLoginProcessor
     * @return new spider
     * @see PageProcessor
     */
    public static SpiderLogin create(PageLoginProcessor pageLoginProcessor) {
        return new SpiderLogin(pageLoginProcessor);
    }

    /**
     * create a spider with pageProcessor.
     *
     * @param pageLoginProcessor pageLoginProcessor
     */
    public SpiderLogin(PageLoginProcessor pageLoginProcessor) {
        pageLoginProcessor.setLoginEntity(this.loginEntity);
        this.site.setCharset(this.loginEntity.getCharset());
        this.pageLoginProcessor = pageLoginProcessor;
        pageLoginProcessor.setSite(this.site);
        this.startRequests = this.site.getStartRequests();
    }

    /**
     * Set startUrls of Spider.<br>
     * Prior to startUrls of Site.
     *
     * @param startUrls startUrls
     * @return this
     */
    public SpiderLogin startUrls(List<String> startUrls) {
        checkIfRunning();
        this.startRequests = UrlUtils.convertToRequests(startUrls);
        return this;
    }

    /**
     * Set startUrls of Spider.<br>
     * Prior to startUrls of Site.
     *
     * @param startRequests startRequests
     * @return this
     */
    public SpiderLogin startRequest(List<Request> startRequests) {
        checkIfRunning();
        this.startRequests = startRequests;
        return this;
    }

    /**
     * Set an uuid for spider.<br>
     * Default uuid is domain of site.<br>
     *
     * @param uuid uuid
     * @return this
     */
    public SpiderLogin setUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * set scheduler for Spider
     *
     * @param scheduler scheduler
     * @return this
     * @see #setScheduler(Scheduler)
     */
    @Deprecated
    public SpiderLogin scheduler(Scheduler scheduler) {
        return setScheduler(scheduler);
    }

    /**
     * set scheduler for Spider
     *
     * @param scheduler scheduler
     * @return this
     * @see Scheduler
     * @since 0.2.1
     */
    public SpiderLogin setScheduler(Scheduler scheduler) {
        checkIfRunning();
        Scheduler oldScheduler = this.scheduler;
        this.scheduler = scheduler;
        if (oldScheduler != null) {
            Request request;
            while ((request = oldScheduler.poll(this)) != null) {
                this.scheduler.push(request, this);
            }
        }
        return this;
    }

    /**
     * add a pipeline for Spider
     *
     * @param pipeline pipeline
     * @return this
     * @see #addPipeline(Pipeline)
     * @deprecated
     */
    public SpiderLogin pipeline(Pipeline pipeline) {
        return addPipeline(pipeline);
    }

    /**
     * add a pipeline for Spider
     *
     * @param pipeline pipeline
     * @return this
     * @see Pipeline
     * @since 0.2.1
     */
    public SpiderLogin addPipeline(Pipeline pipeline) {
        checkIfRunning();
        this.pipelines.add(pipeline);
        return this;
    }

    /**
     * set pipelines for Spider
     *
     * @param pipelines pipelines
     * @return this
     * @see Pipeline
     * @since 0.4.1
     */
    public SpiderLogin setPipelines(List<Pipeline> pipelines) {
        checkIfRunning();
        this.pipelines = pipelines;
        return this;
    }

    /**
     * clear the pipelines set
     *
     * @return this
     */
    public SpiderLogin clearPipeline() {
        pipelines = new ArrayList<Pipeline>();
        return this;
    }

    /**
     * set the downloader of spider
     *
     * @param downloader downloader
     * @return this
     * @see #setDownloader(Downloader)
     * @deprecated
     */
    public SpiderLogin downloader(Downloader downloader) {
        return setDownloader(downloader);
    }

    /**
     * set the downloader of spider
     *
     * @param downloader downloader
     * @return this
     * @see Downloader
     */
    public SpiderLogin setDownloader(Downloader downloader) {
        checkIfRunning();
        this.downloader = downloader;
        return this;
    }

    protected void initComponent() {
        if (downloader == null) {
            this.downloader = new HttpClientDownloader();
        }
        if (pipelines.isEmpty()) {
            pipelines.add(new ConsolePipeline());
        }
        downloader.setThread(threadNum);
        if (threadPool == null || threadPool.isShutdown()) {
            if (executorService != null && !executorService.isShutdown()) {
                threadPool = new CountableThreadPool(threadNum, executorService);
            } else {
                threadPool = new CountableThreadPool(threadNum);
            }
        }
        if (startRequests != null) {
            for (Request request : startRequests) {
                addRequest(request);
            }
            startRequests.clear();
        }
        startTime = new Date();
    }

    @Override
    public void run() {
        checkRunningStat();
        initComponent();
        logger.info("Spider " + getUUID() + " started!");
        while (!Thread.currentThread().isInterrupted() && stat.get() == STAT_RUNNING) {
            Request request = scheduler.poll(this);
            if (request == null) {
                if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                    break;
                }
                // wait until new url added
                waitNewUrl();
            } else {
                final Request requestFinal = request;
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processRequest(requestFinal);
                            onSuccess(requestFinal);
                        } catch (Exception e) {
                            onError(requestFinal);
                            logger.error("process request " + requestFinal + " error", e);
                        } finally {
                            pageCount.incrementAndGet();
                            signalNewUrl();
                        }
                    }
                });
            }
        }
        stat.set(STAT_STOPPED);
        // release some resources
        if (destroyWhenExit) {
            close();
        }
    }

    protected void onError(Request request) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onError(request);
            }
        }
    }

    protected void onSuccess(Request request) {
        if (CollectionUtils.isNotEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onSuccess(request);
            }
        }
    }

    private void checkRunningStat() {
        while (true) {
            int statNow = stat.get();
            if (statNow == STAT_RUNNING) {
                throw new IllegalStateException("Spider is already running!");
            }
            if (stat.compareAndSet(statNow, STAT_RUNNING)) {
                break;
            }
        }
    }

    public void close() {
        destroyEach(downloader);
        destroyEach(pageLoginProcessor);
        destroyEach(scheduler);
        for (Pipeline pipeline : pipelines) {
            destroyEach(pipeline);
        }
        threadPool.shutdown();
    }

    private void destroyEach(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Process specific urls without url discovering.
     *
     * @param urls urls to process
     */
    public void test(String... urls) {
        initComponent();
        if (urls.length > 0) {
            for (String url : urls) {
                processRequest(new Request(url));
            }
        }
    }

    protected void processRequest(Request request) {
        Page page = downloader.download(request, this);
        if (page == null) {
            sleep(site.getSleepTime());
            onError(request);
            return;
        }
        // for cycle retry
        if (page.isNeedCycleRetry()) {
            extractAndAddRequests(page, true);
            sleep(site.getRetrySleepTime());
            return;
        }
        pageLoginProcessor.process(page);
        extractAndAddRequests(page, spawnUrl);
        if (!page.getResultItems().isSkip()) {
            for (Pipeline pipeline : pipelines) {
                pipeline.process(page.getResultItems(), this);
            }
        }
        //for proxy status management
        request.putExtra(Request.STATUS_CODE, page.getStatusCode());
        sleep(site.getSleepTime());
    }

    protected void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void extractAndAddRequests(Page page, boolean spawnUrl) {
        if (spawnUrl && CollectionUtils.isNotEmpty(page.getTargetRequests())) {
            for (Request request : page.getTargetRequests()) {
                addRequest(request);
            }
        }
    }

    private void addRequest(Request request) {
        if (site.getDomain() == null && request != null && request.getUrl() != null) {
            site.setDomain(UrlUtils.getDomain(request.getUrl()));
        }
        scheduler.push(request, this);
    }

    protected void checkIfRunning() {
        if (stat.get() == STAT_RUNNING) {
            throw new IllegalStateException("Spider is already running!");
        }
    }

    public void runAsync() {
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }

    /**
     * Add urls to crawl. <br>
     *
     * @param urls urls
     * @return this
     */
    public SpiderLogin addUrl(String... urls) {
        for (String url : urls) {
            addRequest(new Request(url));
        }
        signalNewUrl();
        return this;
    }

    /**
     * Download urls synchronizing.
     *
     * @param urls urls
     * @return list downloaded
     */
    public <T> List<T> getAll(Collection<String> urls) {
        destroyWhenExit = false;
        spawnUrl = false;
        startRequests.clear();
        for (Request request : UrlUtils.convertToRequests(urls)) {
            addRequest(request);
        }
        CollectorPipeline collectorPipeline = getCollectorPipeline();
        pipelines.add(collectorPipeline);
        run();
        spawnUrl = true;
        destroyWhenExit = true;
        return collectorPipeline.getCollected();
    }

    protected CollectorPipeline getCollectorPipeline() {
        return new ResultItemsCollectorPipeline();
    }

    public <T> T get(String url) {
        List<String> urls = WMCollections.newArrayList(url);
        List<T> resultItemses = getAll(urls);
        if (resultItemses != null && resultItemses.size() > 0) {
            return resultItemses.get(0);
        } else {
            return null;
        }
    }

    /**
     * Add urls with information to crawl.<br>
     *
     * @param requests requests
     * @return this
     */
    public SpiderLogin addRequest(Request... requests) {
        for (Request request : requests) {
            addRequest(request);
        }
        signalNewUrl();
        return this;
    }

    private void waitNewUrl() {
        newUrlLock.lock();
        try {
            //double check
            if (threadPool.getThreadAlive() == 0 && exitWhenComplete) {
                return;
            }
            newUrlCondition.await(emptySleepTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("waitNewUrl - interrupted, error {}", e);
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }

    public void start() {
        runAsync();
    }

    public void stop() {
        if (stat.compareAndSet(STAT_RUNNING, STAT_STOPPED)) {
            logger.info("Spider " + getUUID() + " stop success!");
        } else {
            logger.info("Spider " + getUUID() + " stop fail!");
        }
    }

    /**
     * start with more than one threads
     *
     * @param threadNum threadNum
     * @return this
     */
    public SpiderLogin thread(int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    /**
     * start with more than one threads
     *
     * @param executorService executorService to run the spider
     * @param threadNum       threadNum
     * @return this
     */
    public SpiderLogin thread(ExecutorService executorService, int threadNum) {
        checkIfRunning();
        this.threadNum = threadNum;
        if (threadNum <= 0) {
            throw new IllegalArgumentException("threadNum should be more than one!");
        }
        return this;
    }

    public boolean isExitWhenComplete() {
        return exitWhenComplete;
    }

    /**
     * Exit when complete. <br>
     * True: exit when all url of the site is downloaded. <br>
     * False: not exit until call stop() manually.<br>
     *
     * @param exitWhenComplete exitWhenComplete
     * @return this
     */
    public SpiderLogin setExitWhenComplete(boolean exitWhenComplete) {
        this.exitWhenComplete = exitWhenComplete;
        return this;
    }


    public boolean isSpawnUrl() {
        return spawnUrl;
    }

    /**
     * Get page count downloaded by spider.
     *
     * @return total downloaded page count
     * @since 0.4.1
     */
    public long getPageCount() {
        return pageCount.get();
    }

    /**
     * Get running status by spider.
     *
     * @return running status
     * @see Status
     * @since 0.4.1
     */
    public Status getStatus() {
        return Status.fromValue(stat.get());
    }


    public enum Status {
        Init(0), Running(1), Stopped(2);

        private Status(int value) {
            this.value = value;
        }

        private int value;

        int getValue() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            //default value
            return Init;
        }
    }

    /**
     * Get thread count which is running
     *
     * @return thread count which is running
     * @since 0.4.1
     */
    public int getThreadAlive() {
        if (threadPool == null) {
            return 0;
        }
        return threadPool.getThreadAlive();
    }

    /**
     * Whether add urls extracted to download.<br>
     * Add urls to download when it is true, and just download seed urls when it is false. <br>
     * DO NOT set it unless you know what it means!
     *
     * @param spawnUrl spawnUrl
     * @return this
     * @since 0.4.0
     */
    public SpiderLogin setSpawnUrl(boolean spawnUrl) {
        this.spawnUrl = spawnUrl;
        return this;
    }

    @Override
    public String getUUID() {
        if (uuid != null) {
            return uuid;
        }
        if (site != null) {
            return site.getDomain();
        }
        uuid = UUID.randomUUID().toString();
        return uuid;
    }

    public SpiderLogin setExecutorService(ExecutorService executorService) {
        checkIfRunning();
        this.executorService = executorService;
        return this;
    }

    @Override
    public Site getSite() {
        return site;
    }

    public List<SpiderListener> getSpiderListeners() {
        return spiderListeners;
    }

    public SpiderLogin setSpiderListeners(List<SpiderListener> spiderListeners) {
        this.spiderListeners = spiderListeners;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Set wait time when no url is polled.<br><br>
     *
     * @param emptySleepTime In MILLISECONDS.
     */
    public void setEmptySleepTime(int emptySleepTime) {
        this.emptySleepTime = emptySleepTime;
    }

    /**
     * download the code of url and set cookie
     *
     * @since 0.6.2
     */
    public boolean downloadCode() {
        CodeModule codeModule = new CodeModuleImpl();
        HttpResult httpResult = codeModule.download(this.loginEntity);
        if (httpResult.getStatusCode() != 200) {
            logger.warn("download the code is fail:{}" + httpResult.toString());
            return false;
        }

        if (StringUtils.isNotBlank(this.loginEntity.getCookie())) {
            this.site.addHeader("Cookie", this.loginEntity.getCookie());
        }
        return true;
    }

    public boolean prepareLogin() {
        LoginModule loginModule = new LoginModuleImpl();
        HttpResult httpResult = loginModule.access(this.loginEntity);
        if (httpResult.getStatusCode() != 200) {
            logger.warn("open the url of login is fail:{}" + httpResult.toString());
            return false;
        }
        return true;
    }

    /**
     * login validation
     *
     * @return
     * @since 0.6.2
     */
    public boolean startlogin() {
        if (StringUtils.isBlank(this.loginEntity.getCodeUrl()) && !prepareLogin()) {
            return false;
        }

        LoginModule loginModule = new LoginModuleImpl();
        HttpResult httpResult = loginModule.request(this.loginEntity);
        if (httpResult.getStatusCode() != 200 && httpResult.getStatusCode() != 302) {
            logger.warn("登录失败,状态码：{};响应错误信息：{}", httpResult.getStatusCode(), httpResult.getBody());
            return false;
        }

        this.site.addHeader("Cookie", this.loginEntity.getCookie());
        this.loginIndexUrl = this.loginEntity.getLoginUrl();
        this.responseBody = httpResult.getBody();
        if (httpResult.getStatusCode() == 302) {
            this.loginIndexUrl = httpResult.getLocation();
        }
        logger.info("登录成功后,主页链接：{}", loginIndexUrl);
        return true;
    }

    /**
     * set key and value of the code
     *
     * @param codeKey
     * @param codeValue
     * @since 0.6.2
     */
    public void setCodeValue(String codeKey, String codeValue) {
        this.loginEntity.getParams().put(codeKey, codeValue);
    }

    /**
     * get the login successed url of index
     *
     * @return
     * @since 0.6.2
     */
    public String getLoginIndexUrl() {
        return loginIndexUrl;
    }

    /**
     * get the response body
     *
     * @return
     * @since 0.6.2
     */
    public String getResponseBody() {
        return responseBody;
    }
}

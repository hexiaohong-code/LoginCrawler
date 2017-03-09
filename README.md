# LoginCrawler
LoginCrawler是一套基于[Webmagic](https://github.com/code4craft/webmagic)框架拓展了用户登录后抓取数据的模块，登录方式分为两种：有验证码和无验证码。由于对登录进行模块化,使用简单,直接封装相应的登录实体即可。暂时对JS动态追加的登录页面无法使用，例如[百度云盘](https://pan.baidu.com/),感兴趣的可以一起尝试下。

# Quick Start
## 引入maven依赖
    <dependency>
       <groupId>com.web.crawler</groupId>
       <artifactId>login-crawler</artifactId>
       <version>1.0.0-RC01</version>
    </dependency>
# 测试样例
*正方教务系统、人人网、知乎网测试登录成功并爬取主页信息。大家可以尝试不同的网站进行登录测试，发现问题可以通过以下方式联系我,谢谢(详细的测试样例见项目)
*2017/3/9 新增金山网络登陆测试。

## 验证码登录（正方教务系统）
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
                spiderLogin.addUrl(spiderLogin.getLoginIndex()).start();
            }
        }

## 无验证码登录（知乎网站）
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
                spiderLogin.addUrl(spiderLogin.getLoginIndex()).start();
            }
        }
    }

# 联系方式
email: 564137276@qq.com

package com.web.crawler.login.utils;

import com.web.crawler.login.model.ConstantDefine;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The login module tools
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public class LoginUtils {

    private static final Logger logger = LoggerFactory.getLogger(LoginUtils.class);

    private final static String USER_AGENT = "User-Agent:Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11";

    /**
     * @param url
     * @return
     */
    public static String getHost(String url) {
        String reg = ".*\\/\\/([^\\/\\:]*).*";
        return url.replaceAll(reg, "$1");
    }


    /**
     * @param t
     * @param headMap
     * @param <T>
     */
    public static <T> void setHeader(T t, Map<String, Object> headMap) {
        if (t instanceof HttpGet) {
            ((HttpGet) t).setHeader("User-Agent", USER_AGENT);
            if (null != headMap) {
                for (Map.Entry<String, Object> entry : headMap.entrySet()) {
                    ((HttpGet) t).setHeader(entry.getKey(),
                            String.valueOf(entry.getValue()));
                }
            }
        } else if (t instanceof HttpPost) {
            ((HttpPost) t).setHeader("User-Agent", USER_AGENT);
            if (null != headMap) {
                for (Map.Entry<String, Object> entry : headMap.entrySet()) {
                    ((HttpPost) t).setHeader(entry.getKey(),
                            String.valueOf(entry.getValue()));
                }
            }
        }
    }

    /**
     * @param html
     * @param mark 错误信息,多个错误以';'隔开
     * @return
     */
    public static String checkLogin(String html, String mark) {
        String[] marks = mark.split(";");
        if (marks != null) {
            for (String str : marks) {
                if (html.contains(str)) {
                    return str;
                }
            }
        }
        return "";
    }

    /**
     * @param html 登录页面html信息
     * @return
     */
    public static Map<String, Object> getFormMap(String html) {
        Map<String, Object> retVal = new HashMap<String, Object>();

        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("form").first()
                .select("input[type=hidden]");
        if (elements != null) {
            for (Element element : elements) {
                retVal.put(element.attr("name"), element.val());
            }
        }

        elements = doc.select("form").first().select("input");
        if (elements != null) {
            for (Element element : elements) {
                retVal.put(element.attr("name"), element.val());
            }
        }

        elements = doc.select("form").first().select("select");
        if (elements != null) {
            for (Element element : elements) {
                retVal.put(element.attr("name"),
                        element.select("option[selected]").val());
            }
        }

        retVal.put("formAction",
                doc.select("form").first().attr("action"));

        return retVal;
    }

    /**
     * @param httpResponse http响应
     * @return
     */
    public static String getCookie(HttpResponse httpResponse) {
        StringBuffer cookie = new StringBuffer();
        Header[] headers = httpResponse.getHeaders("Set-Cookie");
        if (headers != null) {
            for (Header header : headers) {
                String Set_Cookie = header.getValue().split(";")[0];
                cookie.append(Set_Cookie + ";");
            }
        }
        return cookie.toString();
    }

    /**
     * @param httpResponse http响应
     * @return
     */
    public static String getLocation(HttpResponse httpResponse) {
        Header header = httpResponse.getFirstHeader("Location");
        if (header != null) {
            return header.getValue();
        }
        return "";
    }

    /**
     * @param httpPost  请求方式
     * @param paramMaps 请求参数
     * @param <T>
     * @throws UnsupportedEncodingException
     */
    public static <T> void setParams(HttpPost httpPost,
                                     Map<String, Object> paramMaps, String coding) throws UnsupportedEncodingException {
        if (null != paramMaps && !paramMaps.isEmpty()) {
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            for (Map.Entry<String, Object> entry : paramMaps.entrySet()) {
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), String
                        .valueOf(entry.getValue())));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, coding);
            httpPost.setEntity(formEntity);
        }
    }

    /**
     * 提供用户输入验证码
     *
     * @return
     */
    public static String getInputStr() {
        BufferedReader strin = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("请输入验证码：");
        try {
            return strin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据网址和href相对路径计算出绝对路径
     *
     * @param url
     * @param href
     * @return
     */
    public static String getAbsolutePath(String url, String href) {
        try {
            URL urlStr = new URL(url);
            String port = urlStr.getPort() == -1 ? "" : ":" + urlStr.getPort();
            String path = urlStr.getPath();

            if (StringUtils.isBlank(path) || !path.matches("^/.+/.+$") || href.matches("^/.+")) {// 根目录
                return urlStr.getProtocol() + "://" + urlStr.getHost() + port + convertHref(href);
            }

            if (href.matches("^\\./.+") || href.matches("^(?!(/|./|../)).+")) {// 表示当前目录
                path = path.substring(0, path.lastIndexOf("/"));
                return urlStr.getProtocol() + "://" + urlStr.getHost() + port + path + convertHref(href);
            }

            if (href.matches("^\\.\\./.+")) {// 表示当前上一级目录
                int countHref = href.split("\\.\\./").length - 1;
                int countPath = path.split("/").length - 1;
                href = "/" + href.split("\\.\\./")[countHref];
                if (countPath <= countHref + 1) {
                    return urlStr.getProtocol() + "://" + urlStr.getHost() + port + convertHref(href);
                }

                String[] strs = path.split("/");
                StringBuffer pathBuffer = new StringBuffer();
                for (int i = 0; i < countPath - countHref; i++) {
                    if (StringUtils.isNotBlank(strs[i])) {
                        pathBuffer.append("/" + strs[i]);
                    }
                }
                return urlStr.getProtocol() + "://" + urlStr.getHost() + port + pathBuffer + convertHref(href);
            }
        } catch (MalformedURLException e) {
            logger.error("获取绝对路径失败,url={},href={},失败原因：", url, href, e);
        }
        return "";
    }

    public static String convertHref(String href) {
        if (href.matches("^(?!(/|./|../)).+")) {// 表示当前目录
            return "/" + href;
        }
        return match(href, "/(?!\\.\\.).+");
    }

    public static String match(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public static CloseableHttpClient createHttpClient(String url) {

        if (!url.startsWith("https")) {
            return HttpClients.createDefault();
        }

        RegistryBuilder<ConnectionSocketFactory> spb = RegistryBuilder.create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        spb.register("http", plainSF);
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs,
                                               String authType) {
                    logger.info("=== 检查客户端证书");
                }

                public void checkServerTrusted(X509Certificate[] certs,
                                               String authType) {
                    logger.info("=== 检查服务器端证书, 忽略 ===");
                }
            }}, new SecureRandom());
        } catch (Exception ex) {
            throw new RuntimeException("创建ssl连接出错", ex);
        }

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        spb.register("https", sslSocketFactory);
        Registry<ConnectionSocketFactory> registry = spb.build();
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setConnectionManager(cm);
        clientBuilder.setRedirectStrategy(new LaxRedirectStrategy());

        clientBuilder.setConnectionTimeToLive(ConstantDefine.Http.DEFAULTTIMEOUT, TimeUnit.MILLISECONDS);
        //setConnectTimeout：设置连接超时时间，单位毫秒。
        //setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
        //setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ConstantDefine.Http.DEFAULTTIMEOUT).setConnectionRequestTimeout(ConstantDefine.Http.DEFAULTTIMEOUT)
                .setSocketTimeout(ConstantDefine.Http.DEFAULTTIMEOUT).build();
        clientBuilder.setDefaultRequestConfig(requestConfig);

        return clientBuilder.build();
    }
}

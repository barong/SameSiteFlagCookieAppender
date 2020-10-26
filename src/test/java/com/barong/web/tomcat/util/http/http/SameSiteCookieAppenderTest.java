package com.barong.web.tomcat.util.http.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.SameSiteCookies;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.dropsUnrecognizedSameSiteCookies;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.hasWebKitSameSiteBug;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.isChromiumBased;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.isChromiumVersionAtLeast;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.isIosVersion;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.isMacEmbeddedBrowser;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.isMacosxVersion;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.isSafari;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.isUcBrowser;
import static com.barong.web.tomcat.util.http.http.SameSiteCookieAppender.isUcBrowserVersionAtLeast;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SameSiteCookieAppenderTest {

    @Mock
    HttpServletRequest request;

    @Test
    public void testAppendSameSite() {
        SameSiteLegacyCookieProcessor cookieProcessor = new SameSiteLegacyCookieProcessor();
        cookieProcessor.setSameSiteCookies(SameSiteCookies.NONE.getValue());
        Cookie cookie = new Cookie("test", "test");
        when(request.isSecure()).thenReturn(true);
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.1 Mobile/15E148 Safari/604.1";
        when(request.getHeader("user-agent")).thenReturn(userAgent);
        String header = cookieProcessor.generateHeader(cookie, request);
        assertTrue(header.contains("SameSite=None; Secure"));
        userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.1 Mobile/15E148 Safari/604.1";
        when(request.getHeader("user-agent")).thenReturn(userAgent);
        header = cookieProcessor.generateHeader(cookie, request);
        assertFalse(header.contains("SameSite=None; Secure"));
    }
    
    
    @Test
    public void testHasWebKitSameSiteBug() {
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.1 Mobile/15E148 Safari/604.1";
        assertTrue(hasWebKitSameSiteBug(userAgent));
        userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.1 Mobile/15E148 Safari/604.1";
        assertFalse(hasWebKitSameSiteBug(userAgent));
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1.2 Safari/605.1.15";
        assertTrue(hasWebKitSameSiteBug(userAgent));
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1.2 Safari/605.1.15";
        assertFalse(hasWebKitSameSiteBug(userAgent));
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/601.7.8 (KHTML, like Gecko)";
        assertTrue(hasWebKitSameSiteBug(userAgent));
    }

    @Test
    public void testDropsUnrecognizedSameSiteCookies() {
        String userAgent = "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; HUAWEI MT7-TL00 Build/HuaweiMT7-TL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 UCBrowser/11.3.8.909 Mobile Safari/537.36";
        assertTrue(dropsUnrecognizedSameSiteCookies(userAgent));
        userAgent = "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; HUAWEI MT7-TL00 Build/HuaweiMT7-TL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 UCBrowser/12.13.2.909 Mobile Safari/537.36";
        assertFalse(dropsUnrecognizedSameSiteCookies(userAgent));
        userAgent = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/51.0.2526.73 Chrome/51.0.2526.73 Safari/537.36";
        assertTrue(dropsUnrecognizedSameSiteCookies(userAgent));
        userAgent = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/50.0.2526.73 Chrome/50.0.2526.73 Safari/537.36";
        assertFalse(dropsUnrecognizedSameSiteCookies(userAgent));
        assertFalse(dropsUnrecognizedSameSiteCookies(userAgent));
        userAgent = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/67.0.2526.73 Chrome/67.0.2526.73 Safari/537.36";
        assertFalse(dropsUnrecognizedSameSiteCookies(userAgent));
    }

    @Test
    public void testIsIosVersion() {
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.1 Mobile/15E148 Safari/604.1";
        assertTrue(isIosVersion(12, userAgent));
        userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1";
        assertFalse(isIosVersion(12, userAgent));
        userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1";
        assertFalse(isIosVersion(12, userAgent));
    }

    @Test
    public void testIsMacosxVersion() {
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1.2 Safari/605.1.15";
        assertTrue(isMacosxVersion(10, 14, userAgent));
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/603.3.8 (KHTML, like Gecko) Version/10.1.2 Safari/603.3.8";
        assertFalse(isMacosxVersion(10, 11, userAgent));
    }

    @Test
    public void testIsSafari() {
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1 Mobile/15E148 Safari/604.1";
        assertTrue(isSafari(userAgent));
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Safari/605.1.15";
        assertTrue(isSafari(userAgent));
        userAgent = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.30 (KHTML, like Gecko) Ubuntu/10.10 Chromium/12.0.742.112 Chrome/12.0.742.112 Safari/534.30";
        assertFalse(isSafari(userAgent));
    }

    @Test
    public void testIsMacEmbeddedBrowser() {
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/601.7.8 (KHTML, like Gecko)";
        assertTrue(isMacEmbeddedBrowser(userAgent));
    }

    @Test
    public void testIsChromiumBased() {
        String userAgent = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.30 (KHTML, like Gecko) Ubuntu/10.10 Chromium/12.0.742.112 Chrome/12.0.742.112 Safari/534.30";
        assertTrue(isChromiumBased(userAgent));
        userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/538.1 (KHTML, like Gecko) Chromium/31.0.1650.63 Site-Shot/2.1 (http://www.site-shot.com/) Safari/538.1";
        assertTrue(isChromiumBased(userAgent));
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/601.7.8 (KHTML, like Gecko) Version/9.1.3 Safari/537.86.7";
        assertFalse(isChromiumBased(userAgent));
    }

    @Test
    public void testIsChromiumVersionAtLeast() {
        String userAgent = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.30 (KHTML, like Gecko) Ubuntu/10.10 Chromium/12.0.742.112 Chrome/12.0.742.112 Safari/534.30";
        assertTrue(isChromiumVersionAtLeast(12, userAgent));
        userAgent = "Mozilla/5.0 (X11; Linux armv7l) AppleWebKit/537.36 (KHTML, like Gecko) Raspbian Chromium/74.0.3729.157 Chrome/74.0.3729.157 Safari/537.36";
        assertTrue(isChromiumVersionAtLeast(12, userAgent));
        userAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Ubuntu/10.04 Chromium/6.0.472.62 Chrome/6.0.472.62 Safari/534.3";
        assertFalse(isChromiumVersionAtLeast(12, userAgent));
    }

    @Test
    public void testIsUcBrowser() {
        String userAgent = "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; HUAWEI MT7-TL00 Build/HuaweiMT7-TL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 UCBrowser/11.3.8.909 Mobile Safari/537.36";
        assertTrue(isUcBrowser(userAgent));
        userAgent = "UCWEB/2.0(Java; U; MIDP-2.0; fr-fr; nokia5530c-2) U2/1.0.0 UCBrowser/8.7.0.218 U2/1.0.0 Mobile UNTRUSTED/1.0 3gpp-gba";
        assertTrue(isUcBrowser(userAgent));
        userAgent = "userAgent = \"Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.30 (KHTML, like Gecko) Ubuntu/10.10 Chromium/12.0.742.112 Chrome/12.0.742.112 Safari/534.30\";";
        assertFalse(isUcBrowser(userAgent));
    }

    @Test
    public void testIsUcBrowserVersionAtLeast() {
        String userAgent = "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; HUAWEI MT7-TL00 Build/HuaweiMT7-TL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 UCBrowser/11.3.8.909 Mobile Safari/537.36";
        assertTrue(isUcBrowserVersionAtLeast(11, 3, 8, userAgent));
        assertTrue(isUcBrowserVersionAtLeast(10, 1, 1, userAgent));
        assertTrue(isUcBrowserVersionAtLeast(11, 1, 8, userAgent));
        assertTrue(isUcBrowserVersionAtLeast(11, 3, 7, userAgent));
        assertFalse(isUcBrowserVersionAtLeast(12, 3, 7, userAgent));
        assertFalse(isUcBrowserVersionAtLeast(11, 4, 7, userAgent));
        assertFalse(isUcBrowserVersionAtLeast(11, 3, 9, userAgent));
    }
}
package com.barong.web.tomcat.util.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.apache.tomcat.util.http.SameSiteCookies;

/**
 * Rfc6265CookieProcessor with custom SameSite flag appender
 */
public class SameSiteRfc6265CookieProcessor extends Rfc6265CookieProcessor implements SameSiteCookieAppender {

    /**
     * Override SameSiteCookies to skip appending it by parent class
     * @return SameSiteCookies.NONE
     */
    @Override
    public SameSiteCookies getSameSiteCookies() {
        return SameSiteCookies.NONE;
    }

    @Override
    public String generateHeader(Cookie cookie, HttpServletRequest request) {
        String header = super.generateHeader(cookie, request);
        appendSameSite(super.getSameSiteCookies(), new StringBuffer(header), request);
        return header;
    }
}

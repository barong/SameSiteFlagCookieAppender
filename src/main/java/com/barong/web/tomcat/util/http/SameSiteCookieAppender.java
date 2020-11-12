package com.barong.web.tomcat.util.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.SameSiteCookies;

/**
 * Appends SameSite flag to cookie response header for compatible user-agent
 * @see <a href="https://www.chromium.org/updates/same-site/incompatible-clients">https://www.chromium.org/updates/same-site/incompatible-clients</a>
 * <br>
 * <br>Last updated: Nov 18, 2019
 * <br>
 * <br>Some user agents are known to be incompatible with the `SameSite=None` attribute.
 * <br>Versions of Chrome from Chrome 51 to Chrome 66 (inclusive on both ends). These Chrome versions will reject a cookie with `SameSite=None`. This also affects older versions of Chromium-derived browsers, as well as Android WebView. This behavior was correct according to the version of the cookie specification at that time, but with the addition of the new "None" value to the specification, this behavior has been updated in Chrome 67 and newer. (Prior to Chrome 51, the SameSite attribute was ignored entirely and all cookies were treated as if they were `SameSite=None`.)
 * <br>Versions of UC Browser on Android prior to version 12.13.2. Older versions will reject a cookie with `SameSite=None`. This behavior was correct according to the version of the cookie specification at that time, but with the addition of the new "None" value to the specification, this behavior has been updated in newer versions of UC Browser.
 * <br>Versions of Safari and embedded browsers on MacOS 10.14 and all browsers on iOS 12. These versions will erroneously treat cookies marked with `SameSite=None` as if they were marked `SameSite=Strict`. This bug has been fixed on newer versions of iOS and MacOS.
 * <br>
 * <br>Here is a potential approach to working around incompatible clients (in pseudocode). If you implement this sample, we highly encourage you to do your own testing to ensure that your implementation is working as intended. Note: The sample regular expression patterns below may not be perfect, as User-Agent strings can vary widely; we encourage you to use a tested User-Agent parsing library if possible.
 * <br>
 * <br>
 * <br>// Copyright 2019 Google LLC.
 * <br>// SPDX-License-Identifier: Apache-2.0
 * <br>
 * <br>// Don’t send `SameSite=None` to known incompatible clients.
 * <br>
 * <br>bool shouldSendSameSiteNone(string useragent):
 * <br>    return !isSameSiteNoneIncompatible(useragent)
 * <br>
 * <br> // Classes of browsers known to be incompatible.
 * <br>
 * <br>bool isSameSiteNoneIncompatible(string useragent):
 * <br>    return hasWebKitSameSiteBug(useragent) ||
 * <br>           dropsUnrecognizedSameSiteCookies(useragent)
 * <br>
 * <br>bool hasWebKitSameSiteBug(string useragent):
 * <br>    return isIosVersion(major:12, useragent) ||
 * <br>           (isMacosxVersion(major:10, minor:14, useragent) &&
 * <br>            (isSafari(useragent) || isMacEmbeddedBrowser(useragent)))
 * <br>
 * <br>bool dropsUnrecognizedSameSiteCookies(string useragent):
 * <br>    if isUcBrowser(useragent):
 * <br>        return !isUcBrowserVersionAtLeast(major:12, minor:13, build:2, useragent)
 * <br>    return isChromiumBased(useragent) &&
 * <br>           isChromiumVersionAtLeast(major:51, useragent) &&
 * <br>           !isChromiumVersionAtLeast(major:67, useragent)
 * <br>
 * <br>// Regex parsing of User-Agent string. (See note above!)
 * <br>
 * <br>bool isIosVersion(int major, string useragent):
 * <br>    string regex = "\(iP.+; CPU .*OS (\d+)[_\d]*.*\) AppleWebKit\/"
 * <br>    // Extract digits from first capturing group.
 * <br>    return useragent.regexMatch(regex)[0] == intToString(major)
 * <br>
 * <br>bool isMacosxVersion(int major, int minor, string useragent):
 * <br>    string regex = "\(Macintosh;.*Mac OS X (\d+)_(\d+)[_\d]*.*\) AppleWebKit\/"
 * <br>    // Extract digits from first and second capturing groups.
 * <br>    return (useragent.regexMatch(regex)[0] == intToString(major)) &&
 * <br>           (useragent.regexMatch(regex)[1] == intToString(minor))
 * <br>
 * <br>bool isSafari(string useragent):
 * <br>    string safari_regex = "Version\/.* Safari\/"
 * <br>    return useragent.regexContains(safari_regex) &&
 * <br>           !isChromiumBased(useragent)
 * <br>
 * <br>bool isMacEmbeddedBrowser(string useragent):
 * <br>    string regex = "^Mozilla\/[\.\d]+ \(Macintosh;.*Mac OS X [_\d]+\) "
 * <br>                     + "AppleWebKit\/[\.\d]+ \(KHTML, like Gecko\)$"
 * <br>    return useragent.regexContains(regex)
 * <br>
 * <br>bool isChromiumBased(string useragent):
 * <br>    string regex = "Chrom(e|ium)"
 * <br>    return useragent.regexContains(regex)
 * <br>
 * <br>bool isChromiumVersionAtLeast(int major, string useragent):
 * <br>    string regex = "Chrom[^ \/]+\/(\d+)[\.\d]* "
 * <br>    // Extract digits from first capturing group.
 * <br>    int version = stringToInt(useragent.regexMatch(regex)[0])
 * <br>    return version >= major
 * <br>
 * <br>bool isUcBrowser(string useragent):
 * <br>    string regex = "UCBrowser\/"
 * <br>    return useragent.regexContains(regex)
 * <br>
 * <br>bool isUcBrowserVersionAtLeast(int major, int minor, int build, string useragent):
 * <br>    string regex = "UCBrowser\/(\d+)\.(\d+)\.(\d+)[\.\d]* "
 * <br>    // Extract digits from three capturing groups.
 * <br>    int major_version = stringToInt(useragent.regexMatch(regex)[0])
 * <br>    int minor_version = stringToInt(useragent.regexMatch(regex)[1])
 * <br>    int build_version = stringToInt(useragent.regexMatch(regex)[2])
 * <br>    if major_version != major:
 * <br>        return major_version > major
 * <br>    if minor_version != minor:
 * <br>        return minor_version > minor
 * <br>    return build_version >= build
 * 
 */ 
interface SameSiteCookieAppender
{
    Log log = LogFactory.getLog(SameSiteCookieAppender.class);
    
    String USER_AGENT_HEADER = "user-agent";
    
    default void appendSameSite(SameSiteCookies sameSiteCookiesValue, StringBuffer sb, HttpServletRequest request) {
        if (sameSiteCookiesValue == null) {
            throw new IllegalStateException("sameSiteCookiesValue is null");
        }
        if (sb == null) {
            throw new IllegalStateException("StringBuffer is null");
        }
        if (SameSiteCookies.UNSET.equals(sameSiteCookiesValue)) {
            log.debug("SameSiteCookies.UNSET value received.");
            return;
        }
        if (!SameSiteCookies.NONE.equals(sameSiteCookiesValue)) {
            log.debug("SameSiteCookies will be set with value: " + sameSiteCookiesValue.getValue());
            sb.append("; SameSite=");
            sb.append(sameSiteCookiesValue.getValue());
        }
        if (request == null) {
            log.debug("SameSiteCookies.NONE will NOT be set. request is null.");
            return;
        }
        if (!request.isSecure()) {
            log.debug("SameSiteCookies.NONE will NOT be set. request is not secure.");
            return;
        }
        // SameSiteCookies.NONE will correctly work only with Secure flag 
        // and only for compatible clients - https://www.chromium.org/updates/same-site/incompatible-clients 
        if (sameSiteSupportedUserAgent(request)) {
            log.debug("SameSiteCookies.NONE will be set for compatible user-agent.");
            sb.append("; SameSite=");
            sb.append(sameSiteCookiesValue.getValue());
            sb.append("; Secure");
        }
    }

    default boolean sameSiteSupportedUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER);
        if (userAgent == null || userAgent.isEmpty()) {
            log.debug("SameSiteCookies.NONE will NOT be set. user-agent is null or empty.");
            return false;
        }
        return !isSameSiteNoneIncompatible(userAgent);
    }

    // Classes of browsers known to be incompatible.
    static boolean isSameSiteNoneIncompatible(String userAgent) {
        return hasWebKitSameSiteBug(userAgent) ||
                dropsUnrecognizedSameSiteCookies(userAgent);
    }

    static boolean hasWebKitSameSiteBug(String userAgent) {
        return isIosVersion(12, userAgent) ||
                (isMacosxVersion(10, 14, userAgent) &&
                        (isSafari(userAgent) || isMacEmbeddedBrowser(userAgent)));
    }

    static boolean dropsUnrecognizedSameSiteCookies(String userAgent) {
        if (isUcBrowser(userAgent)) {
            return !isUcBrowserVersionAtLeast(12, 13, 2, userAgent);
        }
        return isChromiumBased(userAgent) &&
                isChromiumVersionAtLeast(51, userAgent) &&
                !isChromiumVersionAtLeast(67, userAgent);
    }

    static boolean isIosVersion(int major, String userAgent) {
        Pattern pattern = Pattern.compile("\\(iP.+; CPU .*OS (\\d+)[_\\d]*.*\\) AppleWebKit/");
        Matcher matcher = pattern.matcher(userAgent);
        // Extract digits from first capturing group.
        return matcher.find() && matcher.group(1).equals(String.valueOf(major));
    }

    static boolean isMacosxVersion(int major, int minor, String userAgent) {
        Pattern pattern = Pattern.compile("\\(Macintosh;.*Mac OS X (\\d+)_(\\d+)[_\\d]*.*\\) AppleWebKit/");
        Matcher matcher = pattern.matcher(userAgent);
        // Extract digits from first and second capturing groups.
        return matcher.find() && matcher.group(1).equals(String.valueOf(major)) &&
                matcher.group(2).equals(String.valueOf(minor));
    }

    static boolean isSafari(String userAgent) {
        Pattern pattern = Pattern.compile("Version/.* Safari/");
        Matcher matcher = pattern.matcher(userAgent);
        return matcher.find() && !isChromiumBased(userAgent);
    }

    static boolean isMacEmbeddedBrowser(String userAgent) {
        String regex = "^Mozilla/[.\\d]+ \\(Macintosh;.*Mac OS X [_\\d]+\\) "
                + "AppleWebKit/[.\\d]+ \\(KHTML, like Gecko\\)$";
        return userAgent.matches(regex);
    }

    static boolean isChromiumBased(String userAgent) {
        Pattern pattern = Pattern.compile("Chrom(e|ium)");
        Matcher matcher = pattern.matcher(userAgent);
        return matcher.find();    
    }

    static boolean isChromiumVersionAtLeast(int major, String userAgent) {
        Pattern pattern = Pattern.compile("Chrom[^ /]+/(\\d+)[.\\d]* ");
        Matcher matcher = pattern.matcher(userAgent);
        // Extract digits from first capturing group.
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1)) >= major;
            } catch (NumberFormatException e) {
                log.debug("isChromiumVersionAtLeast can not parse int.", e);
            }
        }
        return false;
    }

    static boolean isUcBrowser(String userAgent) {
        Pattern pattern = Pattern.compile("UCBrowser/");
        Matcher matcher = pattern.matcher(userAgent);
        return matcher.find();
    }

    static boolean isUcBrowserVersionAtLeast(int major, int minor, int build, String userAgent) {
        // Extract digits from three capturing groups.
        Pattern pattern = Pattern.compile("UCBrowser/(\\d+)\\.(\\d+)\\.(\\d+)[.\\d]* ");
        Matcher matcher = pattern.matcher(userAgent);
        if (matcher.find()) {
            try {
                int major_version = Integer.parseInt(matcher.group(1));
                if (major_version != major) {
                    return major_version > major;
                }
                int minor_version = Integer.parseInt(matcher.group(2));
                if (minor_version != minor) {
                    return minor_version > minor;
                }
                int build_version = Integer.parseInt(matcher.group(3));
                return build_version >= build;
            } catch (NumberFormatException e) {
                log.debug("isUcBrowserVersionAtLeast can not parse int.", e);
            }
        }
        return false;
    }
}
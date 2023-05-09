package com.qwe7002.telegram_sms.static_class;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.StrictMode;

import com.qwe7002.telegram_sms.config.proxy;

import org.jetbrains.annotations.NotNull;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.dnsoverhttps.DnsOverHttps;

public class
network_func {
    private static final String DNS_OVER_HTTP_ADDRSS = "https://dns.alidns.com/dns-query";

    public static boolean check_network_status(@NotNull Context context) {
        return true;
    }

    @NotNull
    public static String get_url(String apiDomain, String token, String func) {
        return "http://" + apiDomain + "/bot" + token + "/" + func;
    }

    @NotNull
    public static OkHttpClient get_okhttp_obj(boolean doh_switch, proxy proxy_item) {
        OkHttpClient.Builder okhttp = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        Proxy proxy = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (proxy_item.enable) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                InetSocketAddress proxyAddr = new InetSocketAddress(proxy_item.host, proxy_item.port);
                proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (getRequestingHost().equalsIgnoreCase(proxy_item.host)) {
                            if (proxy_item.port == getRequestingPort()) {
                                return new PasswordAuthentication(proxy_item.username, proxy_item.password.toCharArray());
                            }
                        }
                        return null;
                    }
                });
                okhttp.proxy(proxy);
                doh_switch = true;
            }
        }
        if (doh_switch) {
            OkHttpClient.Builder doh_http_client = new OkHttpClient.Builder().retryOnConnectionFailure(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (proxy_item.enable && proxy_item.dns_over_socks5) {
                    doh_http_client.proxy(proxy);
                }
            }
            okhttp.dns(new DnsOverHttps.Builder().client(doh_http_client.build())
                    .url(HttpUrl.get(DNS_OVER_HTTP_ADDRSS))
                    .bootstrapDnsHosts(get_by_ip("2400:3200::1"), get_by_ip("2400:3200:baba::1"), get_by_ip("223.5.5.5"), get_by_ip("223.6.6.6"))
                    .includeIPv6(true)
                    .build());
        }
        return okhttp.build();
    }

    private static InetAddress get_by_ip(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

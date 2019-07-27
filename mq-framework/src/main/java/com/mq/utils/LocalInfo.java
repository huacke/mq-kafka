package com.mq.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @Description 机器相关信息（IP/MAC）
 **/
public class LocalInfo {


    public static String mac = getMac();
    public static String ip = getIp();
    /**
     * @Description 获取网卡MAC地址
     **/
    public static String getMac() {
        String mac = null;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                byte[] bs = ni.getHardwareAddress();
                if (ni != null && ni.isUp() && bs != null && bs.length == 6) {
                    StringBuffer sb = new StringBuffer();
                    for (byte b : bs) {
                        sb.append(Integer.toHexString((b & 240) >> 4));
                        sb.append(Integer.toHexString(b & 15));
                        sb.append(":");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    mac = sb.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac;
    }


    /**
     * @Description 获取IP地址
     **/
    public static String getIp() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        System.out.println(mac);
        System.out.println(ip);
    }
}


package com.example.ws;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtils {

    public static List<String> getDeviceIpAddresses() {
        List<String> ipAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // 排除虚拟网络接口（比如 docker0）
                if (networkInterface.isVirtual()) continue;
                // 获取网络接口的名称
                String interfaceName = networkInterface.getName();
                // 获取网络接口的显示名称
                String displayName = networkInterface.getDisplayName();
                //该 获取网络接口上的所有 IP 地址
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    // 如果是 IPv4 地址
                    if (inetAddress instanceof Inet4Address) {
//                        ipAddresses.add("IPv4 (" + interfaceName + "): " + inetAddress.getHostAddress());
                        ipAddresses.add(inetAddress.getHostAddress());
                    }
                    // 如果是 IPv6 地址
//                    else if (inetAddress instanceof Inet6Address) {
////                        ipAddresses.add("IPv6 (" + interfaceName + "): " + inetAddress.getHostAddress());
//                        ipAddresses.add(inetAddress.getHostAddress());
//                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddresses;
    }
}


package com.haiercash.core.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 机器信息
 *
 * @author 许崇雷
 * @date 2017/6/7
 */
public final class HostInfo {
    private static final HostInfo instance = new HostInfo();
    private String hostName;
    private String[] macAddress;
    private String[] ipAddress;

    //构造函数
    private HostInfo() {
        this.refresh();
    }

    //byte 的高 4bit 或 低 4bit 转 char
    private static char getHexValue(int i) {
        return i < 10 ? (char) (i + '0') : (char) (i - 10 + 'A');
    }

    //mac 地址转 字符串
    private static String getPhysicalAddress(byte[] mac) {
        int arrayLen = mac.length * 3;
        char[] array = new char[arrayLen];
        for (int byteIndex = 0, charIndex = 0; charIndex < arrayLen; byteIndex++, charIndex += 3) {
            byte b = mac[byteIndex];
            array[charIndex] = getHexValue((b & 0xF0) >> 4);
            array[charIndex + 1] = getHexValue(b & 0x0F);
            array[charIndex + 2] = '-';
        }
        return new String(array, 0, arrayLen - 1);
    }

    /**
     * 获取主机名
     *
     * @return 主机名
     */
    public static String getHostName() {
        return instance.hostName;
    }

    /**
     * 获取 MAC 地址
     *
     * @return MAC 地址
     */
    public static String[] getMacAddress() {
        return instance.macAddress;
    }

    /**
     * 获取内网 IP 地址
     *
     * @return 内网 IP 地址
     */
    public static String[] getIpAddress() {
        return instance.ipAddress;
    }

    //刷新
    private void refresh() {
        try {
            //获取主机名
            InetAddress properties = InetAddress.getLocalHost();
            String hostName = properties.getHostName();
            //获取 mac 和 ip 地址
            List<String> macAddress = new ArrayList<>();
            List<String> ipAddress = new ArrayList<>();
            Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces();
            while (niEnum.hasMoreElements()) {
                NetworkInterface ni = niEnum.nextElement();
                byte[] mac;
                if (ni != null && ni.isUp() && !ni.isLoopback() && !ni.isPointToPoint() && !ni.isVirtual() && (mac = ni.getHardwareAddress()) != null) {
                    macAddress.add(getPhysicalAddress(mac));
                    Enumeration<InetAddress> ipEnum = ni.getInetAddresses();
                    while (ipEnum.hasMoreElements()) {
                        InetAddress ip = ipEnum.nextElement();
                        if (!ip.isMulticastAddress() && ip instanceof Inet4Address) {
                            ipAddress.add(ip.getHostAddress());
                            break;
                        }
                    }
                }
            }
            this.hostName = hostName;
            this.macAddress = macAddress.toArray(new String[0]);
            this.ipAddress = ipAddress.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

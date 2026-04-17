package net.frozenorb.potpvp.util;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.com.google.gson.JsonElement;
import net.minecraft.util.com.google.gson.JsonObject;
import net.minecraft.util.com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Skin {
   public static final String ID_API = "https://api.mojang.com/users/profiles/minecraft/";
   public static final String WEB_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
   public static final Map<String, Skin> SKINS = new ConcurrentHashMap<>();
   public static Skin DEFAULT_SKIN = new Skin("", "", "");
   @Getter @Setter private String name;
   @Getter @Setter private String value;
   @Getter @Setter private String signature;

   public static CompletableFuture<Skin> getSkinByName(String name) {
      return CompletableFuture.supplyAsync(() -> {
         if (SKINS.containsKey(name)) {
            return SKINS.get(name);
         } else {
            String texture;
            String signature;

            try {
               String idChecker = ID_API + name;
               URL urlChecker = new URL(idChecker);
               InputStreamReader reader = new InputStreamReader(urlChecker.openStream());
               JsonParser parser = new JsonParser();
               JsonElement element = parser.parse(reader);
               JsonObject object = element.getAsJsonObject();
               String id = object.get("id").getAsString();
               String link = WEB_API + id + "?unsigned=false";
               urlChecker = new URL(link);
               reader = new InputStreamReader(urlChecker.openStream());
               element = parser.parse(reader);
               JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
               texture = properties.get("value").getAsString();
               signature = properties.get("signature").getAsString();
               reader.close();
            } catch (Exception ex) {
               ex.printStackTrace();
               return DEFAULT_SKIN;
            }

            Skin skin = new Skin(name, texture, signature);
            SKINS.put(name, skin);
            return skin;
         }
      });
   }

   public Skin(String name, String value, String signature) {
      this.name = name;
      this.value = value;
      this.signature = signature;
   }

}

package net.frozenorb.potpvp.util;

import lombok.Getter;
import lombok.Setter;
import net.frozenorb.potpvp.PotPvPSI;
import net.minecraft.util.com.google.gson.JsonElement;
import net.minecraft.util.com.google.gson.JsonObject;
import net.minecraft.util.com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Skin {
   public static final String ID_API = "https://api.mojang.com/users/profiles/minecraft/";
   public static final String WEB_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
   private static final String CACHE_FILE_NAME = "skins.json";
   private static final long SKIN_REQUEST_INTERVAL_MILLIS = 1000L;
   public static final Map<String, Skin> SKINS = new ConcurrentHashMap<>();
   public static Skin DEFAULT_SKIN = new Skin("", "", "");
   private static final Object CACHE_LOCK = new Object();
   private static final Map<String, CompletableFuture<Skin>> PENDING_SKIN_LOADS = new ConcurrentHashMap<>();
   private static final ExecutorService SKIN_LOAD_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "PotPvP-SkinLoader");
      thread.setDaemon(true);
      return thread;
   });
   private static volatile boolean cacheLoaded = false;
   private static long lastSkinRequestAt = 0L;
   @Getter @Setter private String name;
   @Getter @Setter private String value;
   @Getter @Setter private String signature;

   public static CompletableFuture<Skin> getSkinByName(String name) {
      Skin cached = getCachedSkin(name);
      if (cached != null) {
         return CompletableFuture.completedFuture(cached);
      }

      String key = cacheKey(name);
      if (key.isEmpty()) {
         return CompletableFuture.completedFuture(DEFAULT_SKIN);
      }

      return PENDING_SKIN_LOADS.computeIfAbsent(key, ignored -> CompletableFuture.supplyAsync(() -> {
         Skin skin = fetchSkinByName(name);
         if (skin.isComplete()) {
            SKINS.put(key, skin);
            saveCache();
         }

         return skin;
      }, SKIN_LOAD_EXECUTOR).whenComplete((skin, throwable) -> PENDING_SKIN_LOADS.remove(key)));
   }

   public static Skin getCachedSkin(String name) {
      loadCache();
      Skin skin = SKINS.get(cacheKey(name));
      return skin != null && skin.isComplete() ? skin : null;
   }

   private static Skin fetchSkinByName(String name) {
      try {
         JsonParser parser = new JsonParser();
         String idChecker = ID_API + name;
         JsonElement idElement;

         try (InputStreamReader reader = openReader(idChecker)) {
            idElement = parser.parse(reader);
         }

         String id = idElement.getAsJsonObject().get("id").getAsString();
         String link = WEB_API + id + "?unsigned=false";
         JsonElement profileElement;

         try (InputStreamReader reader = openReader(link)) {
            profileElement = parser.parse(reader);
         }

         JsonObject properties = profileElement.getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
         return new Skin(name, properties.get("value").getAsString(), properties.get("signature").getAsString());
      } catch (Exception ex) {
         ex.printStackTrace();
         return DEFAULT_SKIN;
      }
   }

   private static InputStreamReader openReader(String link) throws Exception {
      waitForRequestSlot();
      return new InputStreamReader(new URL(link).openStream(), StandardCharsets.UTF_8);
   }

   private static void waitForRequestSlot() throws InterruptedException {
      long waitMillis = SKIN_REQUEST_INTERVAL_MILLIS - (System.currentTimeMillis() - lastSkinRequestAt);
      if (waitMillis > 0L) {
         Thread.sleep(waitMillis);
      }

      lastSkinRequestAt = System.currentTimeMillis();
   }

   public static void loadCache() {
      if (cacheLoaded) {
         return;
      }

      synchronized (CACHE_LOCK) {
         if (cacheLoaded) {
            return;
         }

         File cacheFile = getCacheFile();
         if (cacheFile == null || !cacheFile.exists()) {
            cacheLoaded = true;
            return;
         }

         try (InputStreamReader reader = new InputStreamReader(new FileInputStream(cacheFile), StandardCharsets.UTF_8)) {
            JsonElement element = new JsonParser().parse(reader);
            if (element == null || !element.isJsonObject()) {
               cacheLoaded = true;
               return;
            }

            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
               if (!entry.getValue().isJsonObject()) {
                  continue;
               }

               JsonObject object = entry.getValue().getAsJsonObject();
               Skin skin = new Skin(
                  getString(object, "name", entry.getKey()),
                  getString(object, "value", ""),
                  getString(object, "signature", "")
               );

               if (skin.isComplete()) {
                  SKINS.put(cacheKey(skin.getName()), skin);
               }
            }
         } catch (Exception ex) {
            ex.printStackTrace();
         } finally {
            cacheLoaded = true;
         }
      }
   }

   private static void saveCache() {
      synchronized (CACHE_LOCK) {
         File cacheFile = getCacheFile();
         if (cacheFile == null) {
            return;
         }

         File parent = cacheFile.getParentFile();
         if (parent != null && !parent.exists()) {
            parent.mkdirs();
         }

         JsonObject root = new JsonObject();
         for (Skin skin : SKINS.values()) {
            if (!skin.isComplete()) {
               continue;
            }

            JsonObject object = new JsonObject();
            object.addProperty("name", skin.getName());
            object.addProperty("value", skin.getValue());
            object.addProperty("signature", skin.getSignature());
            root.add(cacheKey(skin.getName()), object);
         }

         try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(cacheFile), StandardCharsets.UTF_8)) {
            writer.write(root.toString());
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      }
   }

   private static File getCacheFile() {
      PotPvPSI plugin = PotPvPSI.getInstance();
      return plugin == null ? null : new File(plugin.getDataFolder(), CACHE_FILE_NAME);
   }

   private static String getString(JsonObject object, String key, String fallback) {
      return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback;
   }

   private static String cacheKey(String name) {
      return name == null ? "" : name.toLowerCase(Locale.ROOT);
   }

   public Skin(String name, String value, String signature) {
      this.name = name;
      this.value = value;
      this.signature = signature;
   }

   public boolean isComplete() {
      return value != null && !value.isEmpty() && signature != null && !signature.isEmpty();
   }

}

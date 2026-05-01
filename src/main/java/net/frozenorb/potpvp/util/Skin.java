package net.frozenorb.potpvp.util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.Getter;
import lombok.Setter;
import net.frozenorb.potpvp.PotPvPSI;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;
import org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
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
   private static final Type SKIN_CACHE_TYPE = new TypeToken<Map<String, Skin>>() {}.getType();
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

         try (Reader reader = Files.newReader(cacheFile, Charsets.UTF_8)) {
            Map<String, Skin> loadedSkins = PotPvPSI.getGson().fromJson(reader, SKIN_CACHE_TYPE);
            if (loadedSkins == null) {
               return;
            }

            for (Map.Entry<String, Skin> entry : loadedSkins.entrySet()) {
               Skin skin = entry.getValue();
               if (skin == null) {
                  continue;
               }

               if (skin.getName() == null || skin.getName().isEmpty()) {
                  skin.setName(entry.getKey());
               }

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

         Map<String, Skin> cacheSnapshot = new LinkedHashMap<>();
         for (Skin skin : SKINS.values()) {
            if (!skin.isComplete()) {
               continue;
            }

            cacheSnapshot.put(cacheKey(skin.getName()), skin);
         }

         try {
            Files.write(
               PotPvPSI.getGson().toJson(cacheSnapshot, SKIN_CACHE_TYPE),
               cacheFile,
               Charsets.UTF_8
            );
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      }
   }

   private static File getCacheFile() {
      PotPvPSI plugin = PotPvPSI.getInstance();
      return plugin == null ? null : new File(plugin.getDataFolder(), CACHE_FILE_NAME);
   }

   private static String cacheKey(String name) {
      return name == null ? "" : name.toLowerCase(Locale.ROOT);
   }

   public Skin() {
      this("", "", "");
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

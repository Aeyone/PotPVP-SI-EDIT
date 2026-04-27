package net.frozenorb.potpvp.util;

import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SkinUtils {

    public static void setSkin(Player player, Skin skin) {
        if (player == null || skin == null || !skin.isComplete()) {
            return;
        }

        CraftPlayer cp = (CraftPlayer) player;
        GameProfile profile = cp.getProfile();
        profile.getProperties().removeAll("textures");
        profile.getProperties().put(
                "textures",
                new Property("textures", skin.getValue(), skin.getSignature())
        );
    }

}

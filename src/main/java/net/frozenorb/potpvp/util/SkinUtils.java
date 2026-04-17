package net.frozenorb.potpvp.util;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedEntitySpawn;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class SkinUtils {

    public static void setSkin(Player player, Skin skin) {
        CraftPlayer cp = (CraftPlayer) player;
        GameProfile profile = cp.getProfile();
        profile.getProperties().removeAll("textures");
        profile.getProperties().put(
                "textures",
                new Property("textures", skin.getValue(), skin.getSignature())
        );
    }

    public static void refreshAsPlayer(Player player) {
        if (player == null || !player.isOnline()) return;

        CraftPlayer cp = (CraftPlayer) player;
        EntityPlayer ep = cp.getHandle();

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(ep.getId());
        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(ep);

        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player)) continue;

            EntityPlayer viewer = ((CraftPlayer) p).getHandle();
            viewer.playerConnection.sendPacket(destroy);
            viewer.playerConnection.sendPacket(spawn);

            sendEquipment(viewer, player, ep.getId());
        }
    }

    private static void sendEquipment(EntityPlayer viewer, Player target, int entityId) {
        PlayerInventory inv = target.getInventory();

        send(viewer, new PacketPlayOutEntityEquipment(entityId, 0, CraftItemStack.asNMSCopy(inv.getItemInHand())));
        send(viewer, new PacketPlayOutEntityEquipment(entityId, 1, CraftItemStack.asNMSCopy(inv.getBoots())));
        send(viewer, new PacketPlayOutEntityEquipment(entityId, 2, CraftItemStack.asNMSCopy(inv.getLeggings())));
        send(viewer, new PacketPlayOutEntityEquipment(entityId, 3, CraftItemStack.asNMSCopy(inv.getChestplate())));
        send(viewer, new PacketPlayOutEntityEquipment(entityId, 4, CraftItemStack.asNMSCopy(inv.getHelmet())));
    }

    private static void send(EntityPlayer viewer, PacketPlayOutEntityEquipment packet) {
        viewer.playerConnection.sendPacket(packet);
    }
}
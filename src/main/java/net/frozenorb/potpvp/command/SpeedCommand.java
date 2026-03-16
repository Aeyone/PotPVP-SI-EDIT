package net.frozenorb.potpvp.command;

import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Param;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpeedCommand {

   @Command(
      names = {"speed"},
      permission = "op",
      description = "Change your walk or fly speed"
   )
   public static void speed(Player sender, @Param(name = "speed") int speed) {
      if (speed >= 0 && speed <= 10) {
         boolean fly = sender.isFlying();
         if (fly) {
            sender.setFlySpeed(getSpeed(speed, true));
         } else {
            sender.setWalkSpeed(getSpeed(speed, false));
         }

         sender.sendMessage(ChatColor.GOLD + (fly ? "Fly" : "Walk") + " set to " + ChatColor.WHITE + speed + ChatColor.GOLD + ".");
      } else {
         sender.sendMessage(ChatColor.RED + "Speed must be between 0 and 10.");
      }
   }

   private static float getSpeed(int speed, boolean isFly) {
      float defaultSpeed = isFly ? 0.1F : 0.2F;
      float maxSpeed = 1.0F;
      if ((float)speed < 1.0F) {
         return defaultSpeed * (float)speed;
      } else {
         float ratio = ((float)speed - 1.0F) / 9.0F * (1.0F - defaultSpeed);
         return ratio + defaultSpeed;
      }
   }
}

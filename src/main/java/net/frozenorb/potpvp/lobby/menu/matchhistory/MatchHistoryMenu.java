package net.frozenorb.potpvp.lobby.menu.matchhistory;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.lobby.menu.matchhistory.button.MatchHistoryButton;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.util.MongoUtils;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MatchHistoryMenu extends PaginatedMenu {

     private Map<Integer, Button> buttons = new HashMap<>();
     private UUID target;

     public MatchHistoryMenu(UUID target) {
         this.target = target;
         setPlaceholder(true);
     }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return ChatColor.GOLD.toString() + ChatColor.BOLD + UUIDUtils.name(target);
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        return this.buttons;
    }

    public void openMenuAsync(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PotPvPSI.getInstance(), () -> {
            MongoCollection<Document> collection = MongoUtils.getCollection(MatchHandler.MONGO_COLLECTION_NAME);
            FindIterable<Document> result = collection.find(Filters.eq("allPlayers", target.toString())).sort(new Document("startedAt", -1));

            Map<Integer, Button> buttons = new HashMap<>();
            int index = 0;
            for (Document doc : result) {
                buttons.put(index++, new MatchHistoryButton(doc, target));
            }

            Bukkit.getScheduler().runTask(PotPvPSI.getInstance(), () -> {
                this.setButtons(buttons);
                this.openMenu(player);
            });
        });
    }

    private void setButtons(Map<Integer, Button> buttons) {
        this.buttons = buttons;
    }

    @Override
    public int getMaxItemsPerPage(Player player) {
        return 9 * 5; // top row is dedicated to switching
    }
}

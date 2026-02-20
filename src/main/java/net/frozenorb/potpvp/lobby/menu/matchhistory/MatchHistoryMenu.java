package net.frozenorb.potpvp.lobby.menu.matchhistory;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import javafx.geometry.Pos;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.lobby.menu.matchhistory.button.MatchHistoryButton;
import net.frozenorb.potpvp.lobby.menu.statistics.StatisticsMenu;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.postmatchinv.menu.PostMatchMenu;
import net.frozenorb.potpvp.util.MongoUtils;
import net.frozenorb.potpvp.util.menu.MenuBackButton;
import net.frozenorb.qlib.menu.Button;
import net.frozenorb.qlib.menu.buttons.BackButton;
import net.frozenorb.qlib.menu.pagination.PaginatedMenu;
import net.frozenorb.qlib.util.UUIDUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class MatchHistoryMenu extends PaginatedMenu {

     private static MatchHistoryHandler matchHistoryHandler = PotPvPSI.getInstance().getMatchHistoryHandler();
     private Map<Integer, Button> buttons = new HashMap<>();
     private UUID target;
     private String matchId = null;

     public MatchHistoryMenu(UUID target) {
         this.target = target;
         isAutoUpdate();
         setPlaceholder(true);
     }
     public MatchHistoryMenu(UUID target, String matchId) {
         this.target = target;
         this.matchId = matchId;
         isAutoUpdate();
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

    @Override
    public int getMaxItemsPerPage(Player player) {
        return 9 * 5; // top row is dedicated to switching
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> globalButtons = new HashMap<>();
        globalButtons.put(4, new MenuBackButton( p -> {
            if (matchId == null) {
                new StatisticsMenu(target).openMenu(p);
            } else {
                new PostMatchMenu(matchId, target).openMenu(p);
            }
        }){
            @Override
            public List<String> getDescription(Player player) {
                return Collections.emptyList();
            }
        });
        return globalButtons;
    }

    @Override
    public void openMenu(Player player) {
        super.openMenu(player);

        Bukkit.getScheduler().runTaskAsynchronously(PotPvPSI.getInstance(), () -> {
            MongoCollection<Document> collection = MongoUtils.getCollection(MatchHandler.MONGO_COLLECTION_NAME);

            if (matchHistoryHandler.getMatchList(target) == null) {
                matchHistoryHandler.loadMatchList(target);
            }
            List<String> matchList = matchHistoryHandler.getMatchList(target);

            FindIterable<Document> result = collection.find(Filters.in("_id", matchList)).sort(Sorts.descending("startedAt"));

            Map<Integer, Button> matchHistoryButtons = new HashMap<>();
            int index = 0;
            for (Document doc : result) {
                matchHistoryButtons.put(index++, new MatchHistoryButton(doc, target));
            }
            Bukkit.getScheduler().runTask(PotPvPSI.getInstance(), () -> {
                this.setButtons(matchHistoryButtons);
                super.openMenu(player);
            });
        });
    }

    private void setButtons(Map<Integer, Button> buttons) {
        this.buttons = buttons;
    }


}

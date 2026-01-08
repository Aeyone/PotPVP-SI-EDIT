package net.frozenorb.potpvp.fakechat;

import net.frozenorb.potpvp.PotPvPSI;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeChatManager {

    private final PotPvPSI plugin;
    private final Map<UUID, FakeChatSession> activeSessions;
    @Getter
    private final FakeChatConfig config;

    public FakeChatManager(PotPvPSI plugin) {
        this.plugin = plugin;
        this.activeSessions = new ConcurrentHashMap<>();
        this.config = new FakeChatConfig();
    }

    public String getRandomChatterDisplay() {
        String name = config.getRandomPrefix() + config.getRandomSuffix();
        String title = config.getRandomTitle();
        return title + name;
    }

    public boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public void startSession(Player player, int speedLevel, boolean famous) {
        if (hasActiveSession(player)) {
            stopSession(player);
        }

        FakeChatSession session = new FakeChatSession(player, speedLevel, famous);
        activeSessions.put(player.getUniqueId(), session);
        session.start();

        String modeText = famous ? ChatColor.YELLOW + "Famous Mode" : ChatColor.GRAY + "Normal Mode";
        double multiplier = getSpeedMultiplier(speedLevel);
        player.sendMessage(ChatColor.GREEN + "Fake Chat enabled " + ChatColor.DARK_GRAY + "(" + modeText + ChatColor.DARK_GRAY + ", " + ChatColor.WHITE + multiplier + "x" + ChatColor.DARK_GRAY + ")");
    }

    public void stopSession(Player player) {
        FakeChatSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.stop();
            player.sendMessage(ChatColor.RED + "Fake Chat disabled");
        }
    }

    public void stopAllSessions() {
        for (FakeChatSession session : activeSessions.values()) {
            session.stop();
        }
        activeSessions.clear();
    }

    public FakeChatSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public static double getSpeedMultiplier(int level) {
        switch (level) {
            case 1: return 0.5;
            case 2: return 0.6;
            case 3: return 0.7;
            case 4: return 0.8;
            case 5: return 0.9;
            case 7: return 1.2;
            case 8: return 1.5;
            case 9: return 2.0;
            case 10: return 3.0;
            case 6:
            default: return 1.0;
        }
    }

    public class FakeChatSession {
        private final Player player;
        public final int speedLevel;
        private final double speedMultiplier;
        public boolean famous;
        @Getter
        private boolean lolSpamMode = false;
        private final Random random = new Random();

        private volatile boolean running = false;
        private BukkitTask mainLoopTask;
        private final List<BukkitTask> scheduledTasks = Collections.synchronizedList(new ArrayList<>());

        private ChatState currentState = ChatState.QUIET_START;
        private int stateTicksRemaining = 0;
        private String lastChatter = null;
        private int consecutiveMessages = 0;
        private String pendingResponse = null;
        private String pendingResponder = null;

        private final List<ActiveChatter> activeChatters = new ArrayList<>();
        private long lastActivityTime = 0;

        public FakeChatSession(Player player, int speedLevel, boolean famous) {
            this.player = player;
            this.speedLevel = Math.max(1, Math.min(10, speedLevel));
            this.speedMultiplier = getSpeedMultiplier(this.speedLevel);
            this.famous = famous;
            initializeActiveChatters();
        }

        private void initializeActiveChatters() {
            int chatterCount = 5 + random.nextInt(11);
            for (int i = 0; i < chatterCount; i++) {
                activeChatters.add(new ActiveChatter(
                        config.getRandomPrefix() + config.getRandomSuffix(),
                        config.getRandomTitle(),
                        random.nextDouble()
                ));
            }
        }

        public void start() {
            running = true;
            currentState = ChatState.QUIET_START;
            stateTicksRemaining = 20 + random.nextInt(80);
            lastActivityTime = System.currentTimeMillis();

            mainLoopTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!running || !player.isOnline()) {
                        stop();
                        return;
                    }
                    tick();
                }
            }.runTaskTimer(plugin, 1L, 1L);
        }

        public void stop() {
            running = false;

            if (mainLoopTask != null) {
                mainLoopTask.cancel();
                mainLoopTask = null;
            }

            synchronized (scheduledTasks) {
                for (BukkitTask task : scheduledTasks) {
                    if (task != null) {
                        task.cancel();
                    }
                }
                scheduledTasks.clear();
            }
        }

        private void tick() {
            if (!running) return;

            if (lolSpamMode) {
                return;
            }

            stateTicksRemaining--;

            if (stateTicksRemaining <= 0) {
                processState();
            }
        }

        private void processState() {
            if (!running) return;

            switch (currentState) {
                case QUIET_START:
                    transitionToNormal();
                    break;

                case NORMAL:
                    sendNormalMessage();
                    break;

                case BURST:
                    sendBurstMessage();
                    break;

                case CONVERSATION:
                    sendConversationMessage();
                    break;

                case QUIET_PERIOD:
                    transitionToNormal();
                    break;

                case RESPONSE_PENDING:
                    sendPendingResponse();
                    break;
            }
        }

        private void transitionToNormal() {
            currentState = ChatState.NORMAL;
            stateTicksRemaining = applySpeedMultiplier(10 + random.nextInt(50));
        }

        private void sendNormalMessage() {
            if (!running) return;

            int roll = random.nextInt(100);

            if (roll < 5) {
                currentState = ChatState.QUIET_PERIOD;
                stateTicksRemaining = applySpeedMultiplier(60 + random.nextInt(140));
                return;
            } else if (roll < 20) {
                currentState = ChatState.BURST;
                consecutiveMessages = 0;
                stateTicksRemaining = applySpeedMultiplier(3 + random.nextInt(7));
                return;
            } else if (roll < 35) {
                currentState = ChatState.CONVERSATION;
                lastChatter = getRandomChatter().name;
                consecutiveMessages = 0;
                stateTicksRemaining = applySpeedMultiplier(5 + random.nextInt(15));
                return;
            }

            scheduleTypingAndSend(getRandomChatter(), config.getRandomMessage());

            if (random.nextInt(100) < 20) {
                setupPendingResponse();
            }

            stateTicksRemaining = applySpeedMultiplier(20 + random.nextInt(80));
        }

        private void sendBurstMessage() {
            if (!running) return;

            ActiveChatter chatter = getRandomChatter();
            scheduleTypingAndSend(chatter, config.getRandomMessage());
            consecutiveMessages++;

            if (consecutiveMessages >= 3 + random.nextInt(5)) {
                currentState = ChatState.NORMAL;
                stateTicksRemaining = applySpeedMultiplier(30 + random.nextInt(60));
            } else {
                stateTicksRemaining = applySpeedMultiplier(2 + random.nextInt(8));
            }
        }

        private void sendConversationMessage() {
            if (!running) return;

            ActiveChatter chatter = findChatterByName(lastChatter);
            if (chatter == null) {
                chatter = getRandomChatter();
                lastChatter = chatter.name;
            }

            String message;
            if (consecutiveMessages == 0) {
                message = config.getRandomConversationStarter();
            } else {
                message = config.getRandomFollowUp();
            }

            scheduleTypingAndSend(chatter, message);
            consecutiveMessages++;

            if (consecutiveMessages >= 1 + random.nextInt(3)) {
                if (random.nextInt(100) < 60) {
                    setupPendingResponse();
                }
                currentState = ChatState.NORMAL;
                stateTicksRemaining = applySpeedMultiplier(20 + random.nextInt(40));
            } else {
                stateTicksRemaining = applySpeedMultiplier(10 + random.nextInt(30));
            }
        }

        private void setupPendingResponse() {
            ActiveChatter responder;
            do {
                responder = getRandomChatter();
            } while (responder.name.equals(lastChatter) && activeChatters.size() > 1);

            pendingResponder = responder.name;
            pendingResponse = config.getRandomResponse();

            currentState = ChatState.RESPONSE_PENDING;
            stateTicksRemaining = applySpeedMultiplier(10 + random.nextInt(30));
        }

        private void sendPendingResponse() {
            if (!running) return;

            if (pendingResponder != null && pendingResponse != null) {
                ActiveChatter responder = findChatterByName(pendingResponder);
                if (responder == null) {
                    responder = getRandomChatter();
                }
                scheduleTypingAndSend(responder, pendingResponse);
                pendingResponder = null;
                pendingResponse = null;
            }

            transitionToNormal();
        }

        private void scheduleTypingAndSend(ActiveChatter chatter, String message) {
            if (!running) return;

            int messageLength = message.length();
            int typingTicks = Math.max(5, messageLength / 3);
            typingTicks = Math.min(typingTicks, 60);
            typingTicks = applySpeedMultiplier(typingTicks);

            typingTicks += random.nextInt(10) - 5;
            typingTicks = Math.max(3, typingTicks);

            final String finalMessage = message;
            final ActiveChatter finalChatter = chatter;
            final int delay = typingTicks;

            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!running || !player.isOnline()) {
                        return;
                    }
                    sendChatMessage(finalChatter, finalMessage);
                    synchronized (scheduledTasks) {
                        scheduledTasks.remove(this);
                    }
                }
            }.runTaskLater(plugin, delay);

            synchronized (scheduledTasks) {
                if (running) {
                    scheduledTasks.add(task);
                } else {
                    task.cancel();
                }
            }
        }

        private void sendChatMessage(ActiveChatter chatter, String message) {
            if (!running || !player.isOnline()) return;

            if (random.nextInt(100) < 5 && message.length() > 5) {
                message = addTypo(message);
            }

            String fullMessage = chatter.title + chatter.name + ChatColor.WHITE + ": " + message;
            player.sendMessage(fullMessage);

            lastChatter = chatter.name;
            lastActivityTime = System.currentTimeMillis();
        }

        private String addTypo(String message) {
            int typoType = random.nextInt(4);
            int pos = 1 + random.nextInt(Math.max(1, message.length() - 2));

            switch (typoType) {
                case 0:
                    if (message.length() > 3) {
                        return message.substring(0, pos) + message.substring(pos + 1);
                    }
                    break;
                case 1:
                    char c = message.charAt(pos);
                    return message.substring(0, pos) + c + message.substring(pos);
                case 2:
                    if (pos < message.length() - 1) {
                        char[] chars = message.toCharArray();
                        char temp = chars[pos];
                        chars[pos] = chars[pos + 1];
                        chars[pos + 1] = temp;
                        return new String(chars);
                    }
                    break;
                case 3:
                    char[] chars = message.toCharArray();
                    if (Character.isLowerCase(chars[pos])) {
                        chars[pos] = Character.toUpperCase(chars[pos]);
                    } else if (Character.isUpperCase(chars[pos])) {
                        chars[pos] = Character.toLowerCase(chars[pos]);
                    }
                    return new String(chars);
            }
            return message;
        }

        private ActiveChatter getRandomChatter() {
            if (activeChatters.isEmpty()) {
                return new ActiveChatter(
                        config.getRandomPrefix() + config.getRandomSuffix(),
                        config.getRandomTitle(),
                        0.5
                );
            }

            double totalWeight = 0;
            for (ActiveChatter chatter : activeChatters) {
                totalWeight += chatter.chatFrequency;
            }

            double roll = random.nextDouble() * totalWeight;
            double current = 0;
            for (ActiveChatter chatter : activeChatters) {
                current += chatter.chatFrequency;
                if (roll <= current) {
                    return chatter;
                }
            }

            return activeChatters.get(random.nextInt(activeChatters.size()));
        }

        private ActiveChatter findChatterByName(String name) {
            for (ActiveChatter chatter : activeChatters) {
                if (chatter.name.equals(name)) {
                    return chatter;
                }
            }
            return null;
        }

        private int applySpeedMultiplier(int baseTicks) {
            double adjusted = baseTicks / speedMultiplier;
            return Math.max(1, (int) adjusted);
        }

        public void triggerLOLSpam(int messageCount) {
            this.lolSpamMode = true;

            BukkitTask task = new BukkitRunnable() {
                int sent = 0;
                int ticksUntilNext = 0;

                @Override
                public void run() {
                    if (!running || sent >= messageCount || !player.isOnline()) {
                        lolSpamMode = false;
                        synchronized (scheduledTasks) {
                            scheduledTasks.remove(this);
                        }
                        cancel();
                        return;
                    }

                    ticksUntilNext--;
                    if (ticksUntilNext <= 0) {
                        ActiveChatter chatter = getRandomChatter();
                        String message = config.getLolSpamMessage();
                        String fullMessage = chatter.title + chatter.name + ChatColor.WHITE + ": " + message;
                        player.sendMessage(fullMessage);
                        sent++;
                        ticksUntilNext = 2 + random.nextInt(8);
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);

            synchronized (scheduledTasks) {
                if (running) {
                    scheduledTasks.add(task);
                } else {
                    task.cancel();
                }
            }
        }

        public void refreshChatters() {
            if (random.nextInt(100) < 10 && !activeChatters.isEmpty()) {
                int index = random.nextInt(activeChatters.size());
                activeChatters.set(index, new ActiveChatter(
                        config.getRandomPrefix() + config.getRandomSuffix(),
                        config.getRandomTitle(),
                        random.nextDouble()
                ));
            }
        }
    }

    private static class ActiveChatter {
        final String name;
        final String title;
        final double chatFrequency;

        ActiveChatter(String name, String title, double chatFrequency) {
            this.name = name;
            this.title = title;
            this.chatFrequency = Math.max(0.1, chatFrequency);
        }
    }

    private enum ChatState {
        QUIET_START,
        NORMAL,
        BURST,
        CONVERSATION,
        QUIET_PERIOD,
        RESPONSE_PENDING
    }
}

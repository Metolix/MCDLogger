package myplugin.mcdlogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.File;
import java.io.IOException;

public final class MCDiscordLogger extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private ConsoleCommandSender console;

    @Override
    public void onEnable() {
        config = getConfig();
        console = Bukkit.getConsoleSender(); // Get the console sender

        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        logToConsole("§a[MCDLogger]---------- Plugin Started ----------");
        logToConsole("§a[MCDLogger]---------- Sending message through webhook if enabled ----------");
        logToConsole("§a[MCDLogger]---------- Made with ♡ by metolix (on discord) ----------");
        sendStartupMessageToDiscord();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        logToConsole("§a[MCDLogger]---------- Plugin Stopped ----------");
        logToConsole("§a[MCDLogger]---------- Sending message through webhook if enabled ----------");
        logToConsole("§a[MCDLogger]---------- Made with ♡ by metolix (on discord) ----------");
        sendShutdownMessageToDiscord();
    }

    private void logToConsole(String message) {
        console.sendMessage(message);
    }

    private void sendStartupMessageToDiscord() {
        boolean sendServerStartupMessage = config.getBoolean("sendServerStartupMessage", true);
        if (!sendServerStartupMessage) {
            return;
        }

        String startupWebhookURL = config.getString("startupWebhookURL");
        if (startupWebhookURL != null) {
            JSONObject embed = createEmbed("The server has started and could be joined now!", "Server Startup", "GREEN");
            sendMessageToDiscord(startupWebhookURL, embed);
            logToConsole("§a[MCDLogger]---------- Server Started ----------");
            logToConsole("§a[MCDLogger]---------- Sending message through webhook for server startup ----------");
            logToConsole("§a[MCDLogger]---------- Made with ♡ by metolix (on discord) ----------");
        } else {
            logToConsole("§c[MCDLogger] Startup webhook URL not found in the config.");
        }
    }

    private void sendShutdownMessageToDiscord() {
        boolean sendServerShutdownMessage = config.getBoolean("sendServerShutdownMessage", true);
        if (!sendServerShutdownMessage) {
            return;
        }

        String shutdownWebhookURL = config.getString("shutdownWebhookURL");
        if (shutdownWebhookURL != null) {
            JSONObject embed = createEmbed("The server is shutting down!", "Server Shutdown", "RED");
            sendMessageToDiscord(shutdownWebhookURL, embed);
            logToConsole("§a[MCDLogger]---------- Server Shutdown ----------");
            logToConsole("§a[MCDLogger]---------- Sending message through webhook for server shutdown ----------");
            logToConsole("§a[MCDLogger]---------- Made with ♡ by metolix (on discord) ----------");
        } else {
            logToConsole("§c[MCDLogger] Shutdown webhook URL not found in the config.");
        }
    }

    // Inside the MCDiscordLogger class

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        boolean sendLogMessages = config.getBoolean("sendLogMessages", true);
        boolean sendJoinMessages = config.getBoolean("joinMessages", true);

        if (!sendLogMessages || !sendJoinMessages) {
            return;
        }

        String playerName = event.getPlayer().getName();
        String joinMessage = config.getString("customJoinMessage");

        if (joinMessage != null && !joinMessage.isEmpty()) {
            joinMessage = joinMessage.replace("%player%", playerName);
            event.setJoinMessage(joinMessage);
        } else {
            event.setJoinMessage(playerName + " joined the server!");
        }

        String logsWebhookURL = config.getString("logsWebhookURL");
        if (logsWebhookURL != null) {
            JSONObject embed = createEmbed(event.getJoinMessage(), "Player Joined", "GREEN");
            sendMessageToDiscord(logsWebhookURL, embed);
        } else {
            logToConsole("Logs webhook URL not found in the config.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        boolean sendLogMessages = config.getBoolean("sendLogMessages", true);
        boolean sendLeaveMessages = config.getBoolean("leaveMessages", true);

        if (!sendLogMessages || !sendLeaveMessages) {
            return;
        }

        String playerName = event.getPlayer().getName();
        String leaveMessage = config.getString("customLeaveMessage");

        if (leaveMessage != null && !leaveMessage.isEmpty()) {
            leaveMessage = leaveMessage.replace("%player%", playerName);
            event.setQuitMessage(leaveMessage);
        } else {
            event.setQuitMessage(playerName + " left the server!");
        }

        String logsWebhookURL = config.getString("logsWebhookURL");
        if (logsWebhookURL != null) {
            JSONObject embed = createEmbed(event.getQuitMessage(), "Player Left", "RED");
            sendMessageToDiscord(logsWebhookURL, embed);
        } else {
            logToConsole("Logs webhook URL not found in the config.");
        }
    }




    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        boolean sendServerMessage = config.getBoolean("sendServerMessages", true);
        if (!sendServerMessage) {
            return;
        }
        String playerName = event.getPlayer().getName();
        String message = playerName + ": " + event.getMessage();
        String messageWebhookURL = config.getString("messageWebhookURL");
        if (messageWebhookURL != null) {
            JSONObject embed = createEmbed(message, "Player Chat", "BLUE");
            sendMessageToDiscord(messageWebhookURL, embed);
        } else {
            getLogger().warning("Message webhook URL not found in the config.");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        boolean sendLogMessages = config.getBoolean("sendLogMessages", true);
        if (!sendLogMessages) {
            return;
        }
        String playerName = event.getEntity().getName();
        String logsWebhookURL = config.getString("logsWebhookURL");
        if (logsWebhookURL != null) {
            JSONObject embed = createEmbed(playerName, "Player Died", "ORANGE");
            sendMessageToDiscord(logsWebhookURL, embed);
            logToConsole("§a[MCDLogger]---------- Player Died ----------");
            logToConsole("§a[MCDLogger]---------- Sending message through webhook for logs ----------");
            logToConsole("§a[MCDLogger]---------- Made with ♡ by metolix (on discord) ----------");
        } else {
            getLogger().warning("Log webhook URL not found in the config.");
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        boolean sendLogMessages = config.getBoolean("sendLogMessages", true);
        if (!sendLogMessages) {
            return;
        }
        String playerName = event.getPlayer().getName();
        String reason = event.getReason();
        String logsWebhookURL = config.getString("logsWebhookURL");
        if (logsWebhookURL != null) {
            JSONObject embed = createEmbed(playerName + " was kicked for: " + reason, "Player Kicked", "RED");
            sendMessageToDiscord(logsWebhookURL, embed);
            logToConsole("§a[MCDLogger]---------- Player Kicked ----------");
            logToConsole("§a[MCDLogger]---------- Sending message through webhook for logs ----------");
            logToConsole("§a[MCDLogger]---------- Made with ♡ by metolix (on discord) ----------");
        } else {
            getLogger().warning("Logs webhook URL not found in the config.");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        boolean sendLogMessages = config.getBoolean("sendLogMessages", true);
        if (!sendLogMessages) {
            return;
        }

        String playerName = event.getPlayer().getName();
        String command = event.getMessage();
        String logsWebhookURL = config.getString("logsWebhookURL");
        if (logsWebhookURL != null) {
            JSONObject embed = createEmbed(playerName + " used command: " + command, "Command Executed", "RED");
            sendMessageToDiscord(logsWebhookURL, embed);
            logToConsole("§a[MCDLogger]---------- Command Executed ----------");
            logToConsole("§a[MCDLogger]---------- Sending message through webhook for logs ----------");
            logToConsole("§a[MCDLogger]---------- Made with ♡ by metolix (on discord) ----------");
        } else {
            getLogger().warning("Logs webhook URL not found in the config.");
        }

    }

    private void sendMessageToDiscord(String webhookURL, JSONObject embed) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), embed.toJSONString());
        Request request = new Request.Builder().url(webhookURL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                response.close();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private JSONObject createEmbed(String description, String title, String color) {
        JSONObject embed = new JSONObject();
        embed.put("content", "");
        JSONArray embeds = new JSONArray();
        JSONObject embedObject = new JSONObject();

        embedObject.put("title", title);
        embedObject.put("description", description);
        embedObject.put("color", getColorCode(color));

        // Footer addition
        JSONObject footer = new JSONObject();
        footer.put("text", "Made with ♡ by metolix (on discord)");
        embedObject.put("footer", footer);

        embeds.add(embedObject);
        embed.put("embeds", embeds);

        return embed;
    }

    private int getColorCode(String color) {
        switch (color.toUpperCase()) {
            case "RED":
                return 0xFF0000;
            case "GREEN":
                return 0x00FF00;
            case "BLUE":
                return 0x0000FF;
            case "ORANGE":
                return 0xFFA500;
            default:
                return 0xFFFFFF; // White as default
        }
    }
}

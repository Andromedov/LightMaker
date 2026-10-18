package mov.naspen.lightmaker.util;

import mov.naspen.lightmaker.LightMaker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class HandWatcher implements Listener {

    public static void startWatching(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new HandWatcher(), plugin);
        plugin.getServer().getOnlinePlayers().forEach(LightMaker.projector::add);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        LightMaker.projector.add(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        LightMaker.projector.remove(event.getPlayer());
    }
}

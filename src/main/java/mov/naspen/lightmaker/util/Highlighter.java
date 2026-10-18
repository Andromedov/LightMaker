package mov.naspen.lightmaker.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Highlighter {

    private final Plugin plugin;
    private final long period;
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public Highlighter(Plugin plugin, long period) {
        this.plugin = plugin;
        this.period = period;
    }

    public void add(Player player) {
        tasks.computeIfAbsent(player.getUniqueId(), uuid ->
                player.getScheduler().runAtFixedRate(plugin, task -> {
                    if (Lights.isLight(player.getInventory().getItemInMainHand())
                            || Lights.isLight(player.getInventory().getItemInOffHand())) {
                        ParticleRender.render(plugin, player);
                    }
                }, () -> tasks.remove(uuid), 1L, period));
    }

    public void remove(Player player) {
        remove(player.getUniqueId());
    }

    public void remove(UUID uuid) {
        ScheduledTask task = tasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public boolean contains(UUID uuid) {
        return tasks.containsKey(uuid);
    }

    public void stop() {
        tasks.values().forEach(ScheduledTask::cancel);
        tasks.clear();
    }
}

package mov.naspen.lightmaker.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ParticleRender {

    private static final int RADIUS = 10;

    private record Marker(Location location, BlockData data) {}

    public static void summonMarker(Player player, Block block) {
        player.spawnParticle(Particle.BLOCK_MARKER, block.getLocation().toCenterLocation(), 1, block.getBlockData());
    }

    public static void render(Plugin plugin, Player player) {
        Location origin = player.getLocation();
        World world = origin.getWorld();
        int px = origin.getBlockX(), py = origin.getBlockY(), pz = origin.getBlockZ();

        for (int cx = (px - RADIUS) >> 4; cx <= (px + RADIUS) >> 4; cx++) {
            for (int cz = (pz - RADIUS) >> 4; cz <= (pz + RADIUS) >> 4; cz++) {
                int chunkX = cx, chunkZ = cz;
                plugin.getServer().getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> {
                    if (!plugin.isEnabled() || !world.isChunkLoaded(chunkX, chunkZ)) {
                        return;
                    }
                    List<Marker> markers = new ArrayList<>();
                    int minY = Math.max(world.getMinHeight(), py - RADIUS);
                    int maxY = Math.min(world.getMaxHeight() - 1, py + RADIUS);
                    for (int x = Math.max(px - RADIUS, chunkX << 4);
                         x <= Math.min(px + RADIUS, (chunkX << 4) + 15); x++) {
                        for (int z = Math.max(pz - RADIUS, chunkZ << 4);
                             z <= Math.min(pz + RADIUS, (chunkZ << 4) + 15); z++) {
                            for (int y = minY; y <= maxY; y++) {
                                int dx = x - px, dy = y - py, dz = z - pz;
                                if (dx * dx + dy * dy + dz * dz > RADIUS * RADIUS) {
                                    continue;
                                }
                                Block block = world.getBlockAt(x, y, z);
                                if (block.getType() == Material.LIGHT) {
                                    markers.add(new Marker(block.getLocation().toCenterLocation(), block.getBlockData()));
                                }
                            }
                        }
                    }
                    if (markers.isEmpty()) {
                        return;
                    }
                    // Only immutable scan results cross regions; player access stays on its entity scheduler.
                    player.getScheduler().run(plugin, playerTask -> {
                        if (!plugin.isEnabled() || player.getWorld() != world
                                || !(Lights.isLight(player.getInventory().getItemInMainHand())
                                || Lights.isLight(player.getInventory().getItemInOffHand()))) {
                            return;
                        }
                        Location current = player.getLocation();
                        for (Marker marker : markers) {
                            if (current.distanceSquared(marker.location()) <= RADIUS * RADIUS) {
                                player.spawnParticle(Particle.BLOCK_MARKER, marker.location(), 1, marker.data());
                            }
                        }
                    }, null);
                });
            }
        }
    }
}

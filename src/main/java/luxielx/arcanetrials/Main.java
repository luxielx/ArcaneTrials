package luxielx.arcanetrials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {
    HashMap<UUID, Long> downmap = new HashMap<>();
    final String armorstandkey = "armorstand-arcane";

    @Override
    public void onEnable() {
        ConfigManager.getInstance().setPlugin(this);
        saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void shift(EntityDismountEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getDismounted() instanceof ArmorStand) {
                ArmorStand dismounted = (ArmorStand) e.getDismounted();
                if (dismounted.hasMetadata(armorstandkey)) {
                    if(!e.getEntity().hasPermission("arcane.unride"))
                    e.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void interact(PlayerInteractAtEntityEvent e){
        if(e.getRightClicked() instanceof Player){
            Player victim = (Player) e.getRightClicked();
            Player player = e.getPlayer();

            if(downmap.containsKey(victim.getUniqueId())){
                Material m = Material.valueOf(ConfigManager.getConfig().getString("reviveitem")) ;
                if(player.getInventory().getItemInMainHand().getType() == m || player.getInventory().getItemInOffHand().getType() == m){
                    if(victim.isInsideVehicle()){
                        victim.getVehicle().remove();
                    }
                    downmap.remove(victim.getUniqueId());
                    victim.setHealth(ConfigManager.getConfig().getInt("healthafterrevive"));
                    victim.removePotionEffect(PotionEffectType.BLINDNESS);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void death(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity();
            if (downmap.containsKey(victim.getUniqueId())) {
                if(victim.isInsideVehicle()){
                    victim.getVehicle().remove();
                }
                downmap.remove(victim.getUniqueId());
                e.setCancelled(true);
                victim.setHealth(0);
            }else
            if (e.getDamage() > victim.getHealth()) {
                e.setCancelled(true);
                downState(victim);
            }
        }

    }

    // downtime = 5000
    // current time = 10000
    // timer = 6000
    // 10000-6000 > 5000
    // remaining = 5000 - (10000-6000)
    private void downState(Player victim) {
        downmap.put(victim.getUniqueId(), System.currentTimeMillis());
        int timer = ConfigManager.getConfig().getInt("revivetimer");
        String holo = ConfigManager.getConfig().getString("timerhologram");
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                timer * 20, 100, true));
        ArmorStand am = victim.getWorld().spawn(victim.getLocation().add(0, 0, 0), ArmorStand.class);
        am.setArms(false);
        am.setMarker(true);
        am.setBasePlate(false);
        am.setGravity(false);
        am.setSmall(false);
        am.setVisible(false);
        am.setCustomNameVisible(true);
        am.addPassenger(victim);
        am.setMetadata(armorstandkey, new FixedMetadataValue(this, true));
        am.setCustomName("");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (downmap.containsKey(victim.getUniqueId())) {
                    if (am.getPassengers().size() <= 0) {
                        am.remove();
                        if(downmap.containsKey(victim.getUniqueId())){
                            downmap.remove(victim.getUniqueId());
                        }
                        this.cancel();
                    }
                    if (System.currentTimeMillis() - downmap.get(victim.getUniqueId()) > timer * 1000) {
                        downmap.remove(victim.getUniqueId());
                        victim.setHealth(0);
                        am.remove();
                        this.cancel();
                    } else {
                        int time = (int) (((timer * 1000) - (System.currentTimeMillis() - downmap.get(victim.getUniqueId()))) / 1000);
                        String z = cc(holo.replaceAll("%player%", victim.getName()).replaceAll("%time%", time + ""));
                        am.setCustomName(z);
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(this, 5, 5);

    }


    public static String cc(String s) {

        return ChatColor.translateAlternateColorCodes('&', s);
    }

}

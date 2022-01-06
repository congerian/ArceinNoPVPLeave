package ru.arcein.plugins.nopvpleave;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class NoPVPLeavePlugin extends JavaPlugin {

    private static final Long combat_time = 10000L;

    Map<Player, Long> playersDamages = new HashMap<Player, Long>();

    public void onLoad() {
        
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(new NoPVPLeaveListener(this), this);
        new NoPVPLeaveRunnable(this).runTaskTimer(this, 0L, 20L);
        Bukkit.getLogger().info("[ARCEIN] NoPVPLeave has been enabled!");
    }

    public void onDisable(){

        Bukkit.getLogger().info("[ARCEIN] NoPVPLeave has been disabled.");
    }

    private class NoPVPLeaveListener implements Listener{
        NoPVPLeavePlugin plugin;
        public NoPVPLeaveListener(NoPVPLeavePlugin plugin){
            this.plugin = plugin;
        }


        @EventHandler
                (priority = EventPriority.LOWEST)
        public void onPlayerCombat(EntityDamageByEntityEvent event){
            if(event.isCancelled()) return;
            Entity attacked_entity = event.getEntity();
            Entity attacker_entity = event.getDamager();

            if(attacker_entity instanceof Projectile){
                attacker_entity = (Entity) ((Projectile) attacker_entity).getShooter();
            }

            if(attacked_entity instanceof Player && attacker_entity instanceof Player){
                Player player1 = (Player) attacked_entity;
                Player player2 = (Player) attacker_entity;
                if(!this.plugin.playersDamages.containsKey(player1)){
                    player1.sendMessage(ChatColor.RED.toString() + "Вы вошли в бой! Не выходите из игры!");
                }
                if(!this.plugin.playersDamages.containsKey(player2)){
                    player2.sendMessage(ChatColor.RED.toString() + "Вы вошли в бой! Не выходите из игры!");
                }
                playersDamages.put(player1, System.currentTimeMillis());
                playersDamages.put(player2, System.currentTimeMillis());
            }
        }

        @EventHandler
                (priority = EventPriority.HIGHEST)
        public void onPlayerLeave(PlayerQuitEvent event){
            if(this.plugin.playersDamages.containsKey(event.getPlayer())){
                event.getPlayer().setHealth(0.0);
                for(Player player : this.plugin.getServer().getOnlinePlayers()){
                    player.sendMessage(ChatColor.GOLD + event.getPlayer().getDisplayName() + " сматывается с поля боля! Позор!");
                }
            }
        }

    }



    public class NoPVPLeaveRunnable extends BukkitRunnable{
        NoPVPLeavePlugin plugin;
        public NoPVPLeaveRunnable(NoPVPLeavePlugin plugin){
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for(Player player : this.plugin.getServer().getOnlinePlayers()){
                if(!this.plugin.playersDamages.containsKey(player)){
                    continue;
                }
                if(System.currentTimeMillis() - this.plugin.playersDamages.get(player) >= NoPVPLeavePlugin.combat_time){
                    this.plugin.playersDamages.remove(player);
                    player.sendMessage(ChatColor.GREEN.toString() + "Вы вышли из боя!");
                }
            }
        }
    }

}


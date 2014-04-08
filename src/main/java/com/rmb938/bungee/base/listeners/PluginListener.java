package com.rmb938.bungee.base.listeners;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.jedis.JedisManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

public class PluginListener implements Listener {

    private final MN2BungeeBase plugin;

    public PluginListener(MN2BungeeBase plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getReceiver() instanceof ProxiedPlayer == false) {
            return;
        }
        if (event.getTag().equalsIgnoreCase("BungeeCord")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
            try {
                String subchannel = in.readUTF();

                if (subchannel.equalsIgnoreCase("connect")) {
                    String[] info = in.readUTF().split("\\.");
                    if (info.length == 1) {
                        ArrayList<ExtendedServerInfo> extendedServerInfos = ExtendedServerInfo.getExtendedInfos(info[0]);
                        for (ExtendedServerInfo extendedServerInfo : extendedServerInfos) {
                            if (extendedServerInfo.getFree() > 1) {
                                ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                                player.connect(extendedServerInfo.getServerInfo());
                                break;
                            }
                        }
                    } else if (info.length == 2) {
                        Jedis jedis = JedisManager.getJedis();
                        String uuid = jedis.get("server." + info[0] + "." + info[1]);
                        if (uuid != null) {
                            ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                            player.connect(plugin.getProxy().getServerInfo(uuid));
                        }
                        JedisManager.returnJedis(jedis);
                    }
                    event.setCancelled(true);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, null, e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, null, e);
                }
            }
        }
    }

}

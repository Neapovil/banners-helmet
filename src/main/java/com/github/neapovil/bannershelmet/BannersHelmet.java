package com.github.neapovil.bannershelmet;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.neapovil.bannershelmet.listener.BannerEntityListener;
import com.github.neapovil.bannershelmet.listener.MergeListener;
import com.github.neapovil.bannershelmet.listener.UnmergeListener;

public final class BannersHelmet extends JavaPlugin implements Listener
{
    private static BannersHelmet instance;
    private final List<Material> materials = Arrays.asList(Material.values())
            .stream()
            .filter(i -> i.toString().toLowerCase().endsWith("banner"))
            .filter(i -> !i.toString().toLowerCase().contains("wall"))
            .toList();
    public static NamespacedKey BANNER_KEY;
    public static NamespacedKey ENTITY_KEY;

    @Override
    public void onEnable()
    {
        instance = this;

        BANNER_KEY = new NamespacedKey(this, "banner");
        ENTITY_KEY = new NamespacedKey(this, "entity");

        this.getServer().getPluginManager().registerEvents(new BannerEntityListener(), this);
        this.getServer().getPluginManager().registerEvents(new MergeListener(), this);
        this.getServer().getPluginManager().registerEvents(new UnmergeListener(), this);
    }

    @Override
    public void onDisable()
    {
    }

    public static BannersHelmet getInstance()
    {
        return instance;
    }

    public List<Material> getBanners()
    {
        return this.materials;
    }
}

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
    private final NamespacedKey bannerKey = new NamespacedKey(this, "hasBanner");
    private final NamespacedKey bannerTypeKey = new NamespacedKey(this, "bannerType");
    private final NamespacedKey patternsCountKey = new NamespacedKey(this, "patternsCount");
    private final NamespacedKey clearEnchantsKey = new NamespacedKey(this, "clearEnchants");
    private final NamespacedKey removalKey = new NamespacedKey(this, "removal");

    @Override
    public void onEnable()
    {
        instance = this;

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

    public NamespacedKey getBannerKey()
    {
        return this.bannerKey;
    }

    public NamespacedKey getBannerTypeKey()
    {
        return this.bannerTypeKey;
    }

    public NamespacedKey getPatternsCountKey()
    {
        return this.patternsCountKey;
    }

    public NamespacedKey getClearEnchantsKey()
    {
        return this.clearEnchantsKey;
    }

    public NamespacedKey getRemovalKey()
    {
        return this.removalKey;
    }
}

package com.github.neapovil.zedge;

import org.bukkit.plugin.java.JavaPlugin;

public final class Zedge extends JavaPlugin
{
    private static Zedge instance;

    @Override
    public void onEnable()
    {
        instance = this;
    }

    @Override
    public void onDisable()
    {
    }

    public static Zedge getInstance()
    {
        return instance;
    }
}

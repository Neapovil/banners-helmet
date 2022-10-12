package com.github.neapovil.bannershelmet.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import com.github.neapovil.bannershelmet.BannersHelmet;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

public class MergeListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void merge(PlayerInteractEvent event)
    {
        if (!event.getAction().isRightClick())
        {
            return;
        }

        if (event.getClickedBlock() == null)
        {
            return;
        }

        if (!event.getClickedBlock().getType().toString().toLowerCase().endsWith("anvil"))
        {
            return;
        }

        if (plugin.getBanners().stream().noneMatch(i -> i.equals(event.getClickedBlock().getRelative(BlockFace.UP).getType())))
        {
            return;
        }

        event.setCancelled(true);

        final AnvilGui anvilgui = new AnvilGui("BannersHelmet (Merge)");

        anvilgui.setOnGlobalClick(e -> anvilgui.update());

        anvilgui.setOnBottomClick(e -> {
            if (e.getCurrentItem() == null)
            {
                return;
            }

            this.handleBottomFirstItem(anvilgui, e);
            this.handleBottomSecondItem(anvilgui, e);
            this.handleBottomResultItem(anvilgui, e);

            this.updateResultItem(anvilgui, e);
        });

        anvilgui.setOnTopClick(e -> {
            if (e.getCurrentItem() == null)
            {
                return;
            }

            this.handleTopFirstItem(anvilgui, e);
            this.handleTopSecondItem(anvilgui, e);
            this.handleTopResultItem(anvilgui, e);

            this.updateResultItem(anvilgui, e);
        });

        anvilgui.setOnClose(e -> {
            anvilgui.getResultComponent().getPanes().clear();

            if (anvilgui.getFirstItemComponent().hasItem())
            {
                e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), anvilgui.getFirstItemComponent().getItem(0, 0));
            }

            if (anvilgui.getSecondItemComponent().hasItem())
            {
                e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), anvilgui.getSecondItemComponent().getItem(0, 0));
            }
        });

        anvilgui.show(event.getPlayer());
    }

    private void handleBottomFirstItem(AnvilGui gui, InventoryClickEvent event)
    {
        if (gui.getFirstItemComponent().hasItem())
        {
            return;
        }

        if (!event.getCurrentItem().getType().toString().toLowerCase().endsWith("_helmet"))
        {
            return;
        }

        if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
        {
            return;
        }

        final ItemStack helmetitem = event.getCurrentItem().clone();

        event.getCurrentItem().setAmount(0);

        final StaticPane staticpane = new StaticPane(0, 0, 1, 1);

        staticpane.addItem(new GuiItem(helmetitem), 0, 0);

        gui.getFirstItemComponent().addPane(staticpane);

        gui.update();
    }

    private void handleBottomSecondItem(AnvilGui gui, InventoryClickEvent event)
    {
        if (gui.getSecondItemComponent().hasItem())
        {
            return;
        }

        if (plugin.getBanners().stream().noneMatch(i -> i.equals(event.getCurrentItem().getType())))
        {
            return;
        }

        final ItemStack banneritem = event.getCurrentItem().clone();

        event.getCurrentItem().setAmount(0);

        final StaticPane staticpane = new StaticPane(0, 0, 1, 1);

        staticpane.addItem(new GuiItem(banneritem), 0, 0);

        gui.getSecondItemComponent().addPane(staticpane);

        gui.update();
    }

    private void handleBottomResultItem(AnvilGui gui, InventoryClickEvent event)
    {
        if (!gui.getFirstItemComponent().hasItem())
        {
            return;
        }

        final ItemStack firstitemstack = gui.getFirstItemComponent().getItem(0, 0);

        if (!firstitemstack.getType().toString().toLowerCase().endsWith("_helmet"))
        {
            return;
        }

        if (!gui.getSecondItemComponent().hasItem())
        {
            return;
        }

        final ItemStack seconditemstack = gui.getSecondItemComponent().getItem(0, 0);

        if (plugin.getBanners().stream().noneMatch(i -> i.equals(seconditemstack.getType())))
        {
            return;
        }

        final ItemStack resultitem = firstitemstack.clone();

        resultitem.editMeta(meta -> {
            meta.getPersistentDataContainer().set(plugin.getBannerKey(), PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(plugin.getBannerTypeKey(), PersistentDataType.STRING, seconditemstack.getType().toString());

            final BannerMeta bannermeta = (BannerMeta) seconditemstack.getItemMeta();

            if (!bannermeta.getPatterns().isEmpty())
            {
                meta.getPersistentDataContainer().set(plugin.getPatternsCountKey(), PersistentDataType.INTEGER, bannermeta.numberOfPatterns());

                for (int i = 0; i < bannermeta.numberOfPatterns(); i++)
                {
                    final Pattern pattern = bannermeta.getPattern(i);

                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pattern-" + i + "-type"), PersistentDataType.STRING,
                            pattern.getPattern().getIdentifier());
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pattern-" + i + "-color"), PersistentDataType.STRING,
                            pattern.getColor().toString());
                }
            }
        });

        final StaticPane staticpane = new StaticPane(0, 0, 1, 1);

        staticpane.addItem(new GuiItem(resultitem), 0, 0);

        gui.getResultComponent().addPane(staticpane);

        gui.update();
    }

    private void handleTopFirstItem(AnvilGui gui, InventoryClickEvent event)
    {
        if (event.getRawSlot() != 0)
        {
            return;
        }

        if (!gui.getFirstItemComponent().hasItem())
        {
            return;
        }

        final ItemStack firstitem = gui.getFirstItemComponent().getItem(0, 0).clone();

        gui.getFirstItemComponent().getPanes().clear();

        final int freeslotindex = event.getWhoClicked().getInventory().firstEmpty();

        if (freeslotindex == -1)
        {
            event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), firstitem);
        }
        else
        {
            event.getWhoClicked().getInventory().setItem(freeslotindex, firstitem);
        }

        gui.update();
    }

    private void handleTopSecondItem(AnvilGui gui, InventoryClickEvent event)
    {
        if (event.getRawSlot() != 1)
        {
            return;
        }

        if (!gui.getSecondItemComponent().hasItem())
        {
            return;
        }

        final ItemStack seconditem = gui.getSecondItemComponent().getItem(0, 0).clone();

        gui.getSecondItemComponent().getPanes().clear();

        final int freeslotindex = event.getWhoClicked().getInventory().firstEmpty();

        if (freeslotindex == -1)
        {
            event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), seconditem);
        }
        else
        {
            event.getWhoClicked().getInventory().setItem(freeslotindex, seconditem);
        }

        gui.update();
    }

    private void handleTopResultItem(AnvilGui gui, InventoryClickEvent event)
    {
        if (event.getRawSlot() != 2)
        {
            return;
        }

        if (!gui.getResultComponent().hasItem())
        {
            return;
        }

        final ItemStack resultitem = gui.getResultComponent().getItem(0, 0).clone();

        gui.getFirstItemComponent().getPanes().clear();
        gui.getSecondItemComponent().getPanes().clear();
        gui.getResultComponent().getPanes().clear();

        final int freeslotindex = event.getWhoClicked().getInventory().firstEmpty();

        if (freeslotindex == -1)
        {
            event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), resultitem);
        }
        else
        {
            event.getWhoClicked().getInventory().setItem(freeslotindex, resultitem);
        }

        gui.update();
    }

    private void updateResultItem(AnvilGui gui, InventoryClickEvent event)
    {
        if (!gui.getFirstItemComponent().hasItem() || !gui.getSecondItemComponent().hasItem())
        {
            gui.getResultComponent().getPanes().clear();
            gui.update();
        }
    }
}

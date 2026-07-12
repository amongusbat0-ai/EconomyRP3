package fr.rp.economy.gui;

import fr.rp.economy.EconomyRP;
import fr.rp.economy.models.Quete;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class WalletGui {

    private final EconomyRP plugin;

    public WalletGui(EconomyRP plugin) {
        this.plugin = plugin;
    }

    public void ouvrirPorteMonnaie(Player joueur) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "💰 Porte-monnaie");

        double solde = plugin.getEconomyManager().getSolde(joueur.getUniqueId());

        ItemStack argent = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = argent.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Votre solde");
        meta.setLore(List.of(ChatColor.YELLOW + plugin.getEconomyManager().formater(solde)));
        argent.setItemMeta(meta);
        inv.setItem(13, argent);

        inv.setItem(11, creerItem(Material.EMERALD, ChatColor.GREEN + "Classement (/baltop)",
                List.of(ChatColor.GRAY + "Voir les joueurs les plus riches")));
        inv.setItem(15, creerItem(Material.CHEST, ChatColor.AQUA + "Quetes (/quests)",
                List.of(ChatColor.GRAY + "Voir les quetes disponibles")));

        joueur.openInventory(inv);
    }

    public void ouvrirQuetes(Player joueur) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "📜 Tableau des quetes");

        int slot = 0;
        for (Quete quete : plugin.getQuestManager().getQuetes().values()) {
            if (slot >= 27) break;

            Material icone = switch (quete.getDifficulte().toLowerCase()) {
                case "difficile" -> Material.NETHERITE_INGOT;
                case "moyen" -> Material.IRON_INGOT;
                default -> Material.COPPER_INGOT;
            };

            int possede = plugin.getQuestManager().compterMateriau(joueur, safeMaterial(quete.getMateriau()));
            boolean complete = possede >= quete.getQuantite();

            ItemStack item = new ItemStack(icone);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((complete ? ChatColor.GREEN : ChatColor.YELLOW) + quete.getNom());
            meta.setLore(List.of(
                    ChatColor.GRAY + quete.getDescription(),
                    ChatColor.GRAY + "Difficulte: " + ChatColor.WHITE + quete.getDifficulte(),
                    ChatColor.GRAY + "Recompense: " + ChatColor.GOLD + plugin.getEconomyManager().formater(quete.getRecompense()),
                    ChatColor.GRAY + "Progression: " + (complete ? ChatColor.GREEN : ChatColor.RED) + possede + "/" + quete.getQuantite(),
                    "",
                    complete ? ChatColor.GREEN + "Clique pour valider !" : ChatColor.RED + "Il te manque des objets"
            ));
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }

        joueur.openInventory(inv);
    }

    private Material safeMaterial(String nom) {
        try {
            return Material.valueOf(nom);
        } catch (IllegalArgumentException e) {
            return Material.DIRT;
        }
    }

    private ItemStack creerItem(Material material, String nom, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(nom);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}

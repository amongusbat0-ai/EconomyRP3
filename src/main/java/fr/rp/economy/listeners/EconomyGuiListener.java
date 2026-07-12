package fr.rp.economy.listeners;

import fr.rp.economy.EconomyRP;
import fr.rp.economy.managers.TradeManager;
import fr.rp.economy.models.Quete;
import fr.rp.economy.models.TradeSession;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class EconomyGuiListener implements Listener {

    private final EconomyRP plugin;

    public EconomyGuiListener(EconomyRP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String titre = event.getView().getTitle();

        if (titre.contains("Porte-monnaie")) {
            event.setCancelled(true);
            gererClicPorteMonnaie(event);
            return;
        }

        if (titre.contains("Tableau des quetes")) {
            event.setCancelled(true);
            gererClicQuete(event);
            return;
        }

        if (titre.contains("🔄 Echange")) {
            gererClicEchange(event);
        }
    }

    private void gererClicPorteMonnaie(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player joueur)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        if (item.getType() == Material.EMERALD) {
            joueur.performCommand("baltop");
            joueur.closeInventory();
        } else if (item.getType() == Material.CHEST) {
            plugin.getWalletGui().ouvrirQuetes(joueur);
        }
    }

    private void gererClicQuete(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player joueur)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        String nomAffiche = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Quete queteCliquee = plugin.getQuestManager().getQuetes().values().stream()
                .filter(q -> q.getNom().equals(nomAffiche))
                .findFirst().orElse(null);
        if (queteCliquee == null) return;

        boolean succes = plugin.getQuestManager().validerQuete(joueur, queteCliquee);
        if (succes) {
            joueur.sendMessage(ChatColor.GREEN + "✔ Quete '" + queteCliquee.getNom() + "' validee ! Vous recevez " +
                    plugin.getEconomyManager().formater(queteCliquee.getRecompense()) + ".");
            joueur.closeInventory();
        } else {
            joueur.sendMessage(ChatColor.RED + "Il te manque des objets pour valider cette quete.");
        }
    }

    private void gererClicEchange(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player joueur)) return;
        TradeManager tradeManager = plugin.getTradeManager();
        TradeSession session = tradeManager.getSession(joueur.getUniqueId());
        if (session == null) {
            event.setCancelled(true);
            return;
        }

        boolean estA = joueur.getUniqueId().equals(session.getJoueurA());
        int slot = event.getRawSlot();

        // Zone de statut / separateurs (9-17) : jamais modifiable
        if (slot >= 9 && slot <= 17) {
            event.setCancelled(true);
            // Boutons de confirmation
            if (slot == TradeManager.SLOT_CONFIRMER_A && estA) {
                tradeManager.confirmer(session, joueur.getUniqueId());
            } else if (slot == TradeManager.SLOT_CONFIRMER_B && !estA) {
                tradeManager.confirmer(session, joueur.getUniqueId());
            }
            return;
        }

        // Zone de l'inventaire du joueur (clic normal) : autorise
        if (slot >= TradeManager.TAILLE) return;

        // Chacun ne peut placer des objets que dans sa propre zone (0-8 pour A, 19-25 pour B)
        boolean zoneA = slot <= 8;
        boolean zoneB = slot >= 19 && slot <= 25;

        if ((estA && zoneB) || (!estA && zoneA)) {
            event.setCancelled(true);
            return;
        }

        // Toute modification reinitialise les confirmations
        session.resetConfirmations();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        String titre = event.getView().getTitle();
        if (!titre.contains("🔄 Echange")) return;
        if (!(event.getPlayer() instanceof Player joueur)) return;

        TradeSession session = plugin.getTradeManager().getSession(joueur.getUniqueId());
        if (session != null && !session.isConfirmeA() && !session.isConfirmeB()) {
            // Si personne n'a confirme, on considere que c'est une annulation volontaire
            plugin.getTradeManager().annuler(session);
        }
    }
}

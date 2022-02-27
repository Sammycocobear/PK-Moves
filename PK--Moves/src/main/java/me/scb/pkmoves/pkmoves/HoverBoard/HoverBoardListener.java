package me.scb.pkmoves.pkmoves.HoverBoard;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class HoverBoardListener implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
        CoreAbility slotAbility = bPlayer.getBoundAbility();
        if (!bPlayer.canBend(slotAbility)  || CoreAbility.hasAbility(p,slotAbility.getClass())) return;
        if (slotAbility.getName().equalsIgnoreCase("HoverBoard")){
            new HoverBoard(p);
        }
    }
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e){
        Player player = e.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (player.isSneaking()) return;
        if(CoreAbility.hasAbility(player, HoverBoard.class) && bPlayer.getBoundAbilityName().equalsIgnoreCase("HoverBoard") && !HoverBoard.getStatus()) {
            CoreAbility.getAbility(player, HoverBoard.class).jump(player);
        }
    }


}

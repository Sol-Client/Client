package me.mcblueparrot.client.hud;

import me.mcblueparrot.client.util.Position;

import me.mcblueparrot.client.events.EntityAttackEvent;
import me.mcblueparrot.client.events.EntityDamageEvent;
import me.mcblueparrot.client.events.EventHandler;

public class ComboCounterHud extends SimpleHud {

    private long hitTime = -1;
    private int combo;
    private int possibleTarget;
    private boolean wasClicking;

    public ComboCounterHud() {
        super("Combo Counter", "combo_counter", "Display the number of subsequent hits.");
    }

    @Override
    public void render(Position position, boolean editMode) {
        super.render(position, editMode);
        if((System.currentTimeMillis() - hitTime) > 2000) {
            combo = 0;
        }
    }

    @Override
    public String getText(boolean editMode) {
        if(editMode || combo == 0) {
            return "No combo";
        }
        else {
            return combo + " hit" + ((combo > 1) ? "s" : "");
        }
    }

    public void dealHit() {
        combo++;
        possibleTarget = -1;
        hitTime = System.currentTimeMillis();
    }

    @EventHandler
    public void onEntityAttack(EntityAttackEvent event) {
        possibleTarget = event.victim.getEntityId();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.entity.getEntityId() == possibleTarget) {
            dealHit();
        }
        else if(event.entity == mc.thePlayer) {
            takeHit();
        }
    }

    public void takeHit() {
        combo = 0;
    }

}

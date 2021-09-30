package me.mcblueparrot.client.mod;

import com.google.gson.annotations.Expose;

import me.mcblueparrot.client.events.EntityAttackEvent;
import me.mcblueparrot.client.events.EventHandler;
import me.mcblueparrot.client.mod.annotation.ConfigOption;
import me.mcblueparrot.client.mod.annotation.Slider;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;

public class ParticlesMod extends Mod {

//    public static ParticlesMod instance;
//    public static boolean enabled;
    @Expose
    @ConfigOption("Multiplier")
    @Slider(min = 1, max = 10, step = 1)
    public float multiplier = 1;
    @Expose
    @ConfigOption("Always Sharpness")
    public boolean sharpness;
    @Expose
    @ConfigOption("Snow")
    public boolean snow;
    @Expose
    @ConfigOption("Slime")
    public boolean slime;
    @Expose
    @ConfigOption("Flames")
    public boolean flames;

    public ParticlesMod() {
        super("Particles", "particles", "Change attack particles.", ModCategory.VISUAL);
//        instance = this;
    }

    @EventHandler
    public void onAttack(EntityAttackEvent event) {
        EntityPlayer player = mc.thePlayer;

        if(!(event.victim instanceof EntityLivingBase)) {
            return;
        }

        boolean crit = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater()
                && !player.isPotionActive(Potion.blindness) && player.ridingEntity == null;

        if(crit) {
            for(int i = 0; i < multiplier - 1; i++) {
                mc.effectRenderer.emitParticleAtEntity(event.victim, EnumParticleTypes.CRIT);
            }
        }

        boolean usuallySharpness = EnchantmentHelper.getModifierForCreature(player.getHeldItem(),
                ((EntityLivingBase) event.victim).getCreatureAttribute()) > 0;

        if(sharpness || usuallySharpness) {
            for(int i = 0; i < (usuallySharpness ? multiplier - 1 : multiplier); i++) {
                mc.effectRenderer.emitParticleAtEntity(event.victim, EnumParticleTypes.CRIT_MAGIC);
            }
        }

        if(snow) {
            for(int i = 0; i < multiplier; i++) {
                mc.effectRenderer.emitParticleAtEntity(event.victim, EnumParticleTypes.SNOWBALL);
            }
        }

        if(slime) {
            for(int i = 0; i < multiplier; i++) {
                mc.effectRenderer.emitParticleAtEntity(event.victim, EnumParticleTypes.SLIME);
            }
        }

        if(flames) {
            for(int i = 0; i < multiplier; i++) {
                mc.effectRenderer.emitParticleAtEntity(event.victim, EnumParticleTypes.FLAME);
            }
        }
    }

    //    @Override
//    public void onEnabledChange(boolean enabled) {
//        super.onEnabledChange(enabled);
//        ParticlesMod.enabled = enabled;
//    }

}

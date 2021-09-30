package me.mcblueparrot.client.mod;

public class NumeralPingMod extends Mod {

    public static boolean enabled;

    public NumeralPingMod() {
        super("Numeral Ping", "numeral_ping", "More detailed ping in player list.", ModCategory.UTILITY);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        enabled = true;
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        enabled = false;
    }

}

package me.mcblueparrot.client.hud;

import me.mcblueparrot.client.util.Position;
import me.mcblueparrot.client.util.Rectangle;

import com.google.gson.annotations.Expose;

import me.mcblueparrot.client.events.EventHandler;
import me.mcblueparrot.client.events.RenderEvent;
import me.mcblueparrot.client.mod.Mod;
import me.mcblueparrot.client.mod.ModCategory;
import me.mcblueparrot.client.mod.annotation.ConfigOption;
import me.mcblueparrot.client.mod.annotation.Slider;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public abstract class Hud extends Mod {

    @Expose
    private HudPosition position;
    @Expose
    @ConfigOption(value = "Scale", priority = 1)
    @Slider(min = 50, max = 150, step = 1)
    public float scale = 100;
    protected FontRenderer font;

    public Hud(String name, String id, String description) {
        super(name, id, description, ModCategory.HUD);
        this.font = mc.fontRendererObj;
        this.position = getDefaultPosition();
    }

    protected float getScale() {
        return scale / 100;
    }

    public Position getPosition() {
        return position.toAbsolute();
    }

    public Position getDividedPosition() {
        return new Position((int) (getPosition().getX() / getScale()), (int) (getPosition().getY() / getScale()));
    }

    public HudPosition getDefaultPosition() {
        return new HudPosition(0, 0);
    }

    public void setPosition(Position position) {
        this.position = HudPosition.fromAbsolute(position);
    }

    public boolean isVisible() {
        return true;
    }

    public Rectangle getBounds() {
        return getBounds(getPosition());
    }

    public Rectangle getBounds(Position position) {
        return null;
    }

    public Rectangle getMultipliedBounds() {
        Rectangle rectangle = getBounds(getPosition());
        if(rectangle == null) {
            return null;
        }
        return rectangle.multiply(getScale());
    }

    public void render(boolean editMode) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(getScale(), getScale(), getScale());
        render(getDividedPosition(), editMode);
        GlStateManager.popMatrix();
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        render(mc.currentScreen instanceof MoveHudsScreen);
    }

    public void render(Position position, boolean editMode) {}

    public boolean isSelected(int mouseX, int mouseY) {
        Rectangle bounds = getMultipliedBounds();
        return bounds != null && bounds.contains(mouseX, mouseY);
    }

}


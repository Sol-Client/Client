package me.mcblueparrot.client.mod;

import me.mcblueparrot.client.Client;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.gson.annotations.Expose;

import me.mcblueparrot.client.events.EventHandler;
import me.mcblueparrot.client.events.FovEvent;
import me.mcblueparrot.client.events.MouseClickEvent;
import me.mcblueparrot.client.events.ScrollEvent;
import me.mcblueparrot.client.events.TickEvent;
import me.mcblueparrot.client.mod.annotation.ConfigOption;
import me.mcblueparrot.client.mod.annotation.Slider;
import net.minecraft.util.MathHelper;

public class ZoomMod extends Mod {

    public static boolean enabled;
    public static ZoomMod instance;
    private KeyBinding key = new KeyBinding("Zoom", Keyboard.KEY_C, "Sol Client");
    private KeyBinding keyZoomOut = new KeyBinding("Zoom Out", Keyboard.KEY_MINUS, "Sol Client");
    private KeyBinding keyZoomIn = new KeyBinding("Zoom In", Keyboard.KEY_EQUALS, "Sol Client");

    @Expose
    @ConfigOption("Cinematic")
    private boolean cinematic = true;
    @Expose
    @ConfigOption("Reduce Sensitivity")
    private boolean reduceSensitivity = false;
    @Expose
    @ConfigOption("Scroll-to-Zoom")
    public boolean scrolling = true;
    @Expose
    @ConfigOption("Smooth Animation")
    private boolean smooth = true;
    @Expose
    @ConfigOption("Factor")
    @Slider(min = 2, max = 32, step = 1)
    private float factor = 4;
    private float currentFactor = 1;
    private float lastAnimatedFactor = 1;
    private float animatedFactor = 1;
    private float lastCalculatedAnimatedFactor = 1;
    private long lastUpdateTime = System.currentTimeMillis();
    public float lastSensitivity;
    public boolean wasCinematic;
    public boolean active;

    public ZoomMod() {
        super("Zoom", "zoom", "Zoom in when pressing a button.", ModCategory.UTILITY);
        Client.INSTANCE.registerKeybind(key);
        Client.INSTANCE.registerKeybind(keyZoomOut);
        Client.INSTANCE.registerKeybind(keyZoomIn);
//        instance = this;
    }

    public void start() {
        active = true;
        lastUpdateTime = System.currentTimeMillis();
        lastSensitivity = mc.gameSettings.mouseSensitivity;
        resetFactor();
        updateSensitivity();
        wasCinematic = this.mc.gameSettings.smoothCamera;
        mc.gameSettings.smoothCamera = cinematic;
        mc.renderGlobal.setDisplayListEntitiesDirty();
    }

    public void stop() {
        active = false;
        setFactor(1);
        mc.gameSettings.mouseSensitivity = lastSensitivity;
        mc.gameSettings.smoothCamera = wasCinematic;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if(key.isKeyDown()) {
            if(!active) {
                start();
            }
        }
        else if(active) {
            stop();
        }
        if(active) {
            if(keyZoomOut.isKeyDown()) {
                zoomOut();
            }
            else if(keyZoomIn.isKeyDown()) {
                zoomIn();
            }
        }
        if(smooth) {
            lastAnimatedFactor = animatedFactor;
//            float multiplier = 0.4F;
//            if(currentFactor < animatedFactor) {
//                // Natural Animation
//                multiplier = 0.6F;
//            }
            float multiplier = 0.75F;
            animatedFactor += (currentFactor - animatedFactor) * multiplier;
            //            if(animatedFactor > currentFactor) {
//                animatedFactor = currentFactor;
//            }
//            else if(animatedFactor < currentFactor) {
//                animatedFactor = currentFactor;
//            }
        }
    }

    @EventHandler
    public void onFov(FovEvent event) {
        if(smooth) {
            float calculatedAnimatedFactor = lastAnimatedFactor + (animatedFactor - lastAnimatedFactor) * event.partialTicks;
            if(calculatedAnimatedFactor != lastCalculatedAnimatedFactor) {
                mc.renderGlobal.setDisplayListEntitiesDirty();
            }
            lastCalculatedAnimatedFactor = calculatedAnimatedFactor;
            event.fov *= calculatedAnimatedFactor;
            return;
        }
        if(!active) {
            return;
        }
        event.fov *= currentFactor;
    }

//    @Override
//    public void onEnabledChange(boolean enabled) {
//        super.onEnabledChange(enabled);
//        ZoomMod.enabled = enabled;
//    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        if(active && scrolling && event.button == 2) {
            event.cancelled = true;
            resetFactor();
        }
    }

    @EventHandler
    public boolean onScroll(ScrollEvent event) {
        if(active && scrolling) {
            event.cancelled = true;
            if(Mouse.isButtonDown(2)) {
                return true;
            }
            if(event.amount < 0) {
                zoomOut();
            }
            else if(event.amount > 0) {
                zoomIn();
            }
        }
        return true;
    }

    public void zoomOut() {
        zoom(false);
    }

    public void zoomIn() {
        zoom(true);
    }

    public void resetFactor() {
        setFactor(1 / factor);
    }

    public void setFactor(float factor) {
        if(factor != currentFactor) {
            mc.renderGlobal.setDisplayListEntitiesDirty();
            updateSensitivity();
        }
        currentFactor = factor;
    }

    public void zoom(boolean in) {
//        float changedFactor;
//        float lesserChangedFactor;
//
        float changedFactor;
        float divFactor = 1 / currentFactor;
        if(in) {
            changedFactor = divFactor + 1;
//            lesserChangedFactor = currentFactor + 0.5f;
        }
        else {
            changedFactor = divFactor - 1;
//            lesserChangedFactor = currentFactor - 0.5f;
        }
//
//        if(currentFactor > factor) {
//            System.out.println("lesser");
//            changedFactor = lesserChangedFactor;
//        }

        setFactor(clamp(1 / changedFactor));
    }


    public float clamp(float factor) {
        return MathHelper.clamp_float(factor, 0, 0.5F);
    }

    public void updateSensitivity() {
        if(reduceSensitivity) {
            mc.gameSettings.mouseSensitivity = lastSensitivity * currentFactor;
        }
    }

}

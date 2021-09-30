package me.mcblueparrot.client.mod;

import lombok.SneakyThrows;
import me.mcblueparrot.client.util.Colour;
import me.mcblueparrot.client.util.Rectangle;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import me.mcblueparrot.client.Client;
import me.mcblueparrot.client.hud.MoveHudsScreen;
import me.mcblueparrot.client.mod.annotation.ConfigOption;
import me.mcblueparrot.client.mod.annotation.Slider;
import me.mcblueparrot.client.ui.Button;
import me.mcblueparrot.client.ui.Tickbox;
import me.mcblueparrot.client.util.SlickFontRenderer;
import me.mcblueparrot.client.util.Utils;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class ModsScreen extends GuiScreen {

    private int amountScrolled = 0;
    private int previousAmountScrolled;
    private int maxScrolling;
    private List<Mod> mods = Client.INSTANCE.getMods();
    private GuiScreen previous;
    private boolean wasMouseDown;
    private boolean mouseDown;
    private boolean openedWithMod;
    private Mod selectedMod;
    private ConfigOption.Cached selectedColour;
    private SlickFontRenderer font = SlickFontRenderer.DEFAULT;

    public ModsScreen(GuiScreen previous, Mod mod) {
        this.previous = previous;
        this.openedWithMod = true;
        this.selectedMod = mod;
    }

    public ModsScreen(GuiScreen previous) {
        this.previous = previous;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseButton == 0) {
            mouseDown = true;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if(state == 0) {
            mouseDown = false;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();

        if(dWheel != 0) {
            if(dWheel > 0) {
                dWheel = -1;
            }
            else if(dWheel < 0) {
                dWheel = 1;
            }

            amountScrolled += (float) (dWheel * (selectedMod != null ? 26 : 35));
            amountScrolled = MathHelper.clamp_int(amountScrolled, 0, maxScrolling);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if(keyCode == Client.INSTANCE.keyMods.getKeyCode() && previous == null) {
            mc.displayGuiScreen(null);
        }
    }

    @SneakyThrows
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(mc.theWorld == null) {
            drawRect(0, 0, width, height, new Colour(20, 20, 20).getValue());
        }
        else {
            drawWorldBackground(0);
        }
        String title = "Sol Client Mods";
        if(selectedMod != null) {
            title = selectedMod.getName();
        }
        font.drawString(title, (width / 2) - (font.getWidth(title) / 2), 15,
                -1);

        int y = 30;
        Mod newSelected = selectedMod;
        ConfigOption.Cached newSelectedColour = selectedColour;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Rectangle region = new Rectangle(0, 30, width, height - 60);
        Utils.scissor(region);
        y += 5;
        if(selectedMod == null) {
            for(ModCategory category : ModCategory.values()) {
                String categoryTitle = category.toString();
                font.drawString(categoryTitle, width / 2 - font.getWidth(categoryTitle) / 2, y - amountScrolled, -1);
                y += 15;
                for(Mod mod : category.getMods()) {
                    Rectangle rectangle = new Rectangle(width / 2 - 150, y - amountScrolled, 300, 30);
                    boolean containsMouse = rectangle.contains(mouseX, mouseY) && region.contains(mouseX, mouseY);

                    Colour fill = new Colour(0, 0, 0, 150);
                    Colour outline;
                    String description = mod.getDescription();
                    if(mod.isBlocked()) {
                        if(containsMouse) {
                            outline = new Colour(255, 80, 80);
                        }
                        else {
                            outline = new Colour(255, 0, 0);
                        }
                        description += " Blocked by server.";
                    }
                    else if(mod.isEnabled()) {
                        if(containsMouse) {
                            outline = new Colour(255, 220, 60);
                        }
                        else {
                            outline = new Colour(255, 180, 0);
                        }
                    }
                    else {
                        if(containsMouse) {
                            outline = new Colour(60, 60, 60);
                        }
                        else {
                            outline = new Colour(50, 50, 50);
                        }
                    }
                    Utils.drawRectangle(rectangle, fill);
                    if(containsMouse) {
                        GlStateManager.enableBlend();
                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                        GL11.glColor3ub((byte) 200, (byte) 200, (byte) 200);
                        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/mod_settings.png"));
                        boolean hasSettings = mod.getOptions().size() > 1 && !mod.isBlocked();
                        Rectangle modSettingsBounds = new Rectangle(rectangle.getX() + rectangle.getWidth() - 20,
                                rectangle.getY() + 7,
                                16,
                                16);
                        if(hasSettings) {
                            if(modSettingsBounds.contains(mouseX, mouseY)) {
                                GlStateManager.color(1F, 1F, 1F);
                            }

                            drawModalRectWithCustomSizedTexture(modSettingsBounds.getX(), modSettingsBounds.getY(), 0, 0
                                    , 16
                                    , 16
                                    , 16, 16);
                        }
                        if(mouseDown && !wasMouseDown) {
                            Utils.playClickSound();
                            if(modSettingsBounds.contains(mouseX, mouseY) && hasSettings) {
                                newSelected = mod;
                            }
                            else if(mod.isBlocked()) {
                                URI blockedModPage;
                                if((blockedModPage = Client.INSTANCE.detectedServer.getBlockedModPage()) != null) {
                                    Desktop.getDesktop().browse(blockedModPage);
                                }
                            }
                            else {
                                mod.toggle();
                            }
                        }
                    }
                    rectangle.stroke(outline);
                    font.drawString(mod.getName(), rectangle.getX() + 6, rectangle.getY() + 4, -1);
                    font.drawString(description, rectangle.getX() + 6, rectangle.getY() + 15, 8421504);

                    y += rectangle.getHeight() + 5;
                }
            }
            y += 31;
        }
        else {
            int x = 10;
            Rectangle colourSelectBox = null;
            for(ConfigOption.Cached option : selectedMod.getOptions()) {
                Rectangle rectangle = new Rectangle(width / 2 - 150, y - amountScrolled, 300, 21);
                Utils.drawRectangle(rectangle, new Colour(0, 0, 0, 150));
                font.drawString(option.name, rectangle.getX() + 5, rectangle.getY() + 5, -1);
                if(option.getType() == boolean.class) {
                    Tickbox box = new Tickbox(rectangle.getX() + rectangle.getWidth() - 18, rectangle.getY() + 3,
                            (boolean) option.getValue());
                    box.render(mouseX, mouseY, rectangle.contains(mouseX, mouseY));

                    if(rectangle.contains(mouseX, mouseY) && mouseDown && !wasMouseDown) {
                        option.setValue(!(boolean) option.getValue());
                        Utils.playClickSound();
                    }
                }
                else if(option.getType().isEnum()) {
                    String valueName = option.getValue().toString();

                    Colour valueColour = new Colour(200, 200, 200);
                    if(rectangle.contains(mouseX, mouseY)) {
                        valueColour = Colour.WHITE;
                    }

                    font.drawString(valueName, rectangle.getX() + rectangle.getWidth() - 5 - font.getWidth(valueName),
                            rectangle.getY() + 5,
                            valueColour.getValue());
                    if(rectangle.contains(mouseX, mouseY) && mouseDown && !wasMouseDown) {
                        int ordinal = ((Enum<?>) option.getValue()).ordinal();
                        try {
                            Enum[] values = (Enum[]) option.getType().getMethod("values").invoke(null);
                            if(++ordinal > values.length - 1) {
                                ordinal = 0;
                            }
                            option.setValue(values[ordinal]);
                        }
                        catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                | NoSuchMethodException | SecurityException error) {
                            throw new IllegalStateException(error);
                        }
                        Utils.playClickSound();
                    }
                }
                else if(option.getType() == Colour.class) {
                    Rectangle colourBox = new Rectangle(rectangle.getX() + rectangle.getWidth() - 18, rectangle.getY() + 3,
                            15,
                            15);
                    colourBox.fill((Colour) option.getValue());
                    colourBox.stroke(rectangle.contains(mouseX, mouseY) ? new Colour(120, 120, 120) : new Colour(100,
                            100,
                            100));

                    if(rectangle.contains(mouseX, mouseY) && mouseDown && !wasMouseDown) {
                        if(newSelectedColour != option) {
                            newSelectedColour = option;
                        }
                        else {
                            newSelectedColour = null;
                        }
                        Utils.playClickSound();
                    }

                    if(selectedColour == option) {
                        colourSelectBox = new Rectangle(rectangle.getX(), rectangle.getY() + rectangle.getHeight() + 1, 300,
                                100);
                        if(!colourSelectBox.contains(mouseX, mouseY) && !rectangle.contains(mouseX, mouseY) && mouseDown && !wasMouseDown) {
                            newSelectedColour = null;
//                            colourSelectBox = null;
                        }
                        else {
                            y += colourSelectBox.getHeight() + 1;
                        }
//                        Utils.drawRectangle(region, Color.CYAN);
//                        Utils.playClickSound();

                    }
                }
                else if(option.getType() == float.class
                        && option.field.isAnnotationPresent(Slider.class)) {
                    Slider slider = option.field.getAnnotation(Slider.class);
                    float min = slider.min();
                    float max = slider.max();
                    float step = slider.step();

                    Colour sliderColour = new Colour(200, 200, 200);
                    if(rectangle.contains(mouseX, mouseY)) {
                        sliderColour = Colour.WHITE;
                    }

                    Rectangle sliderBox = new Rectangle(rectangle.getX() + rectangle.getWidth() - 109, rectangle.getY() + 9,
                            104,
                            2);
                    Utils.drawRectangle(sliderBox, sliderColour);

                    float percentage = ((float) option.getValue() - min) / (max - min);
                    int px = (int) (sliderBox.getX() + (percentage * 100));

                    Rectangle sliderScrubber = new Rectangle(px, sliderBox.getY() - 4, 4, 10);
                    Utils.drawRectangle(sliderScrubber, sliderColour);
                    String valueText = new DecimalFormat("0.##").format(option.getValue());

                    if(slider.showValue()) {
                        font.drawString(valueText, sliderBox.getX() - font.getWidth(valueText) - 4, sliderScrubber.getY(),
                                sliderColour.getValue());
                    }

                    if(rectangle.contains(mouseX, mouseY)) {
                        if(mouseDown) {
                            if(!wasMouseDown) {
                                Utils.playClickSound();
                            }
                            if(mouseX < sliderBox.getX()) {
                                option.setValue(min);
                            }
                            else if(mouseX > sliderBox.getX() + sliderBox.getWidth()) {
                                option.setValue(max);
                            }
                            else {
                                for(float value = min; value < max + step; value += step) {
                                    Rectangle bounds =
                                            new Rectangle((int) (sliderBox.getX() + ((value - min) / (max - min) * 100)),
                                                    rectangle.getY(), 1000, rectangle.getHeight());
//                                    Utils.drawRectangle(bounds, Color.RED);

                                    if(value == min) {
                                        value = min;
                                    }
                                    if(value == max) {
                                        value = max;
                                    }
                                    if(bounds.contains(mouseX, mouseY)) {
                                        option.setValue(value);
//                                        break;
                                    }
                                }
                            }
                        }
//                        else if(wasMouseDown) {
////                            Utils.playClickSound();
//                        }
                    }
                }

                y += 26;
            }
            y += 31;
            if(colourSelectBox != null) {
                Colour selectedColour = ((Colour) this.selectedColour.getValue());
                Utils.drawRectangle(colourSelectBox, new Colour(30, 30, 30));
                for(int componentIndex = 0; componentIndex < 4; componentIndex++) {
                    int componentValue = selectedColour.getComponents()[componentIndex];
                    Rectangle componentBox = new Rectangle(colourSelectBox.getX() + 34,
                            colourSelectBox.getY() + 19 + (20 * componentIndex), 255, 10);
                    if (new Rectangle(colourSelectBox.getX(), componentBox.getY(), colourSelectBox.getWidth(),
                            componentBox.getHeight()).contains(mouseX, mouseY) && mouseDown) {
                        int clickedPosition = MathHelper.clamp_int(mouseX - componentBox.getX(), 0, 255);
                        int r = componentIndex == 0 ? clickedPosition : selectedColour.getRed();
                        int g = componentIndex == 1 ? clickedPosition : selectedColour.getGreen();
                        int b = componentIndex == 2 ? clickedPosition : selectedColour.getBlue();
                        int a = componentIndex == 3 ? clickedPosition : selectedColour.getAlpha();
                        if(!wasMouseDown && mouseDown) {
                            Utils.playClickSound();
                        }
                        this.selectedColour.setValue(new Colour(r, g, b, a));
                    }
                    String name = "Red";
                    switch(componentIndex) {
                        case 1:
                            name = "Green";
                            break;
                        case 2:
                            name = "Blue";
                            break;
                        case 3:
                            name = "Alpha";
                    }
                    font.drawString(name, componentBox.getX() - font.getWidth(name) - 5, componentBox.getY() - 1, -1);
                    if(componentIndex == 3) {
                        Utils.drawRectangle(componentBox, Colour.BLACK);
                    }
                    for(int colour = 0; colour < 256; colour += 1) {
                        Colour awtColour = null;
                        switch(componentIndex) {
                            case 0:
                                awtColour = new Colour(colour, 0, 0);
                                break;
                            case 1:
                                awtColour = new Colour(0, colour, 0);
                                break;
                            case 2:
                                awtColour = new Colour(0, 0, colour);
                                break;
                            case 3:
                                awtColour = new Colour(0, 255, 255, colour);
                        }
                        if(colour == componentValue) {
                            awtColour = Colour.WHITE;
                            font.drawString(Integer.toString(colour),
                                    componentBox.getX() + colour - (font.getWidth(Integer.toString(colour)) / 2),
                                    componentBox.getY() + 9, 0x777777);
                        }
                        Utils.drawRectangle(
                                new Rectangle(componentBox.getX() + colour,
                                        componentBox.getY(), 1, 10),
                                awtColour);
                    }
                }
                font.drawString("Select Colour (RGBA)",
                        colourSelectBox.getX() + (colourSelectBox.getWidth() / 2) - (font.getWidth("Select Colour " +
                                "(RGBA)") / 2),
                        colourSelectBox.getY() + 5, -1);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);



//            try {
//                GlStateManager.enableBlend();
//                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//                ResourceLocation resource = new ResourceLocation("mods/" + mod.getId() + ".png");
//                mc.getResourceManager().getResource(resource);
//                mc.getTextureManager().bindTexture(resource);
//                GlStateManager.color(0.247058824F, 0.247058824F, 0.247058824F, 1);
//                drawModalRectWithCustomSizedTexture(rectangle.getX() + 6, rectangle.getY() + 3, 0, 0, 75, 75, 75, 75);
//                GlStateManager.color(1, 1, 1, 1);
//                drawModalRectWithCustomSizedTexture(rectangle.getX() + 5, rectangle.getY() + 2, 0, 0, 75, 75, 75, 75);
//            }
//            catch(IOException error) {
////                error.printStackTrace();
//            }
        drawHorizontalLine(0, width, 29, 0xFF000000);
        drawHorizontalLine(0, width, height - 31, 0xFF000000);
        Button done = new Button("Done", new Rectangle(openedWithMod ? width / 2 - 50 : width / 2 - 103, height - 25, 100, 20), new Colour(0, 100, 0),
                new Colour(20, 120, 20));
        done.render(mouseX, mouseY);

        if(done.contains(mouseX, mouseY) && mouseDown && !wasMouseDown) {
            Utils.playClickSound();
            if(openedWithMod) {
                mc.displayGuiScreen(previous);
                return;
            }
            if(selectedMod == null) {
                mc.displayGuiScreen(previous);
            }
            else {
                newSelected = null;
                newSelectedColour = null;
            }
        }

        if(!openedWithMod) {
            Button edit = new Button("HUD Editor", new Rectangle(width / 2 + 3, height - 25, 100, 20), new Colour(255, 100, 0),
                    new Colour(255, 130, 30));
            edit.render(mouseX, mouseY);
            if(edit.contains(mouseX, mouseY) && mouseDown && !wasMouseDown) {
                Utils.playClickSound();
                mc.displayGuiScreen(new MoveHudsScreen(this, previous instanceof GuiMainMenu ? previous : null));
            }
        }

        wasMouseDown = mouseDown;

        maxScrolling = (y) - (height);
        if(maxScrolling < 0) {
            maxScrolling = 0;
        }
        amountScrolled = MathHelper.clamp_int(amountScrolled, 0, maxScrolling);

//        Utils.drawRectangle(new Rectangle(width / 2 + 155, amountScrolled, 10, (height - 60) / maxScrolling), Color.WHITE);


        if(newSelected != selectedMod) {
            selectedMod = newSelected;
            mouseDown = false;
            wasMouseDown = false;
            if(newSelected == null) {
                amountScrolled = previousAmountScrolled;
            }
            else {
                previousAmountScrolled = amountScrolled;
                amountScrolled = 0;
            }
        }

        selectedColour = newSelectedColour;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Client.INSTANCE.save();
    }

//    @Override
//    public void updateScreen() {
//        super.updateScreen();
//        if(previous instanceof GuiMainMenu) {
//            previous.updateScreen();
//        }
//    }

}

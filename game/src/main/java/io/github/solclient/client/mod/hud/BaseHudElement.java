package io.github.solclient.client.mod.hud;

import io.github.solclient.abstraction.mc.render.GlStateManager;
import io.github.solclient.client.todo.TODO;
import io.github.solclient.client.util.data.Position;
import io.github.solclient.client.util.data.Rectangle;
import org.lwjgl.opengl.GL11;

public abstract class BaseHudElement implements HudElement {

	public abstract HudPosition getHudPosition();

	public abstract void setHudPosition(HudPosition position);

	public abstract float getHudScale();

	@Override
	public Position getPosition() {
		return getHudPosition().toAbsolute();
	}

	@Override
	public Position getDividedPosition() {
		return new Position((int) (getPosition().getX() / getHudScale()), (int) (getPosition().getY() / getHudScale()));
	}

	@Override
	public HudPosition getDefaultPosition() {
		return new HudPosition(0, 0);
	}

	@Override
	public void setPosition(Position position) {
		setHudPosition(HudPosition.fromAbsolute(position));
	}

	@Override
	public Rectangle getBounds() {
		return getBounds(getPosition());
	}

	@Override
	public float[] getHighPrecisionMultipliedBounds() {
		Rectangle rectangle = getBounds();

		if(rectangle == null) {
			return null;
		}

		return rectangle.highPrecisionMultiply(getHudScale());
	}

	@Override
	public Rectangle getMultipliedBounds() {
		Rectangle rectangle = getBounds();

		if(rectangle == null) {
			return null;
		}

		return rectangle.multiply(getHudScale());
	}

	@Override
	public void render(boolean editMode) {
		// Don't render HUD in replay or if marked as invisible.
		if(!isVisible() || !(editMode || isShownInReplay() || TODO.L /* TODO replaymod */ == null)) return;

		GlStateManager.pushMatrix();
		GlStateManager.scale(getHudScale(), getHudScale(), getHudScale());
		render(getDividedPosition(), editMode);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean isSelected(int mouseX, int mouseY) {
		Rectangle bounds = getMultipliedBounds();
		return bounds != null && bounds.contains(mouseX, mouseY);
	}

}

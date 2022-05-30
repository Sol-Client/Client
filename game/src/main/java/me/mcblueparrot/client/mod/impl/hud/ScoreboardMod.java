package me.mcblueparrot.client.mod.impl.hud;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

import me.mcblueparrot.client.event.EventHandler;
import me.mcblueparrot.client.event.impl.ScoreboardRenderEvent;
import me.mcblueparrot.client.mod.Mod;
import me.mcblueparrot.client.mod.ModCategory;
import me.mcblueparrot.client.mod.annotation.Option;
import me.mcblueparrot.client.mod.hud.SimpleHudMod;
import me.mcblueparrot.client.util.Utils;
import me.mcblueparrot.client.util.data.Colour;
import net.minecraft.client.gui.Gui;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

public class ScoreboardMod extends Mod {

	public static ScoreboardMod instance;
	public static boolean enabled;

	@Expose
	@Option(translationKey = SimpleHudMod.TRANSLATION_KEY)
	public boolean background = true;
	@Expose
	@Option(translationKey = SimpleHudMod.TRANSLATION_KEY, applyToAllClass = Option.BACKGROUND_COLOUR_CLASS)
	public Colour backgroundColour = new Colour(1342177280);
	@Expose
	@Option(translationKey = SimpleHudMod.TRANSLATION_KEY)
	public boolean border = false;
	@Expose
	@Option(translationKey = SimpleHudMod.TRANSLATION_KEY, applyToAllClass = Option.BORDER_COLOUR_CLASS)
	public Colour borderColour = Colour.BLACK;
	@Expose
	@Option
	public Colour backgroundColourTop = new Colour(1610612736);
	@Expose
	@Option(translationKey = SimpleHudMod.TRANSLATION_KEY)
	public Colour textColour = Colour.WHITE;
	@Expose
	@Option(translationKey = SimpleHudMod.TRANSLATION_KEY)
	public boolean shadow = true;
	@Expose
	@Option
	public boolean numbers = true;
	@Expose
	@Option
	public Colour numbersColour = new Colour(-43691);

	@Override
	public String getId() {
		return "scoreboard";
	}

	@Override
	public ModCategory getCategory() {
		return ModCategory.HUD;
	}

	@Override
	public void onRegister() {
		super.onRegister();
		instance = this;
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

	@EventHandler
	public void onScoreboardRender(ScoreboardRenderEvent event) {
		event.cancelled = true;

		Scoreboard scoreboard = event.objective.getScoreboard();
		Collection<Score> collection = scoreboard.getSortedScores(event.objective);
		List<Score> list = Lists.newArrayList(Iterables.filter(collection,
				p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")));

		if(list.size() > 15) {
			collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
		}
		else {
			collection = list;
		}

		int i = mc.fontRendererObj.getStringWidth(event.objective.getDisplayName());

		for(Score score : collection) {
			ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
			String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": "
					+ EnumChatFormatting.RED + score.getScorePoints();
			i = Math.max(i, mc.fontRendererObj.getStringWidth(s));
		}

		int i1 = collection.size() * mc.fontRendererObj.FONT_HEIGHT;
		int j1 = event.scaledRes.getScaledHeight() / 2 + i1 / 3;
		int k1 = 3;
		int l1 = event.scaledRes.getScaledWidth() - i - k1;
		int j = 0;

		for(Score score1 : collection) {
			++j;
			ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
			String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
			String s2 = "" + score1.getScorePoints();
			int k = j1 - j * mc.fontRendererObj.FONT_HEIGHT;
			int l = event.scaledRes.getScaledWidth() - k1 + 2;

			if(background) {
				Gui.drawRect(l1 - 2, k, l, k + mc.fontRendererObj.FONT_HEIGHT, backgroundColour.getValue());
			}

			mc.fontRendererObj.drawString(s1, l1, k, textColour.getValue(), shadow);

			if(numbers) {
				mc.fontRendererObj.drawString(s2, l - mc.fontRendererObj.getStringWidth(s2) - (border ? 1 : 0), k,
						numbersColour.getValue(), shadow);
			}

			if(j == collection.size()) {
				String s3 = event.objective.getDisplayName();
				if (background) {
					Gui.drawRect(l1 - 2, k - mc.fontRendererObj.FONT_HEIGHT - 1, l, k - 1,
							backgroundColourTop.getValue());
					Gui.drawRect(l1 - 2, k - 1, l, k, backgroundColour.getValue());
				}
				mc.fontRendererObj.drawString(s3, l1 + i / 2 - mc.fontRendererObj.getStringWidth(s3) / 2,
						k - mc.fontRendererObj.FONT_HEIGHT, textColour.getValue(), shadow);
			}
		}

		if (border) {
			int top = ((j1 - j * mc.fontRendererObj.FONT_HEIGHT) - mc.fontRendererObj.FONT_HEIGHT) - 2;
			Utils.drawOutline(l1 - 3, top, event.scaledRes.getScaledWidth() - k1 + 2,
					top + mc.fontRendererObj.FONT_HEIGHT + 3 + i1, borderColour.getValue());
		}
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

}

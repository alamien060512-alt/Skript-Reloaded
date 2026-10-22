package ch.njol.skript.reloaded.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

@Name("Sidebar")
@Description({
	"Shows a per-player sidebar scoreboard with a title and up to 15 lines (top to bottom). "
		+ "Calling it again replaces the previous sidebar. Colour codes like &a are supported. "
		+ "'hide sidebar' restores the server's main scoreboard."
})
@Example("""
	show sidebar titled "&6&lMy Server" with lines "&7Rank: &f%{rank::%player%}%", "", "&eplay.example.com" to player
	hide sidebar from player
	""")
@Since("SkriptReloaded 1.0")
public class EffSidebar extends Effect {

	static {
		Skript.registerEffect(EffSidebar.class,
			"show sidebar titled %string% with [lines] %strings% to %players%",
			"hide sidebar (from|of) %players%");
	}

	private static final int MAX_LINES = 15;

	private boolean show;
	private @Nullable Expression<String> title, lines;
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		show = matchedPattern == 0;
		if (show) {
			title = (Expression<String>) exprs[0];
			lines = (Expression<String>) exprs[1];
			players = (Expression<Player>) exprs[2];
		} else {
			players = (Expression<Player>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!show) {
			for (Player player : players.getArray(event))
				player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			return;
		}
		String titleText = title.getSingle(event);
		if (titleText == null)
			return;
		String[] text = lines.getArray(event);
		for (Player player : players.getArray(event)) {
			Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
			Objective objective = board.registerNewObjective("sk_sidebar", Criteria.DUMMY, legacy(titleText));
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.numberFormat(NumberFormat.blank());
			int count = Math.min(text.length, MAX_LINES);
			for (int i = 0; i < count; i++) {
				// unique, invisible entry names; the visible text is the custom name
				Score score = objective.getScore("§" + Integer.toHexString(i) + "§r");
				score.customName(legacy(text[i]));
				score.setScore(count - i);
				score.numberFormat(NumberFormat.blank());
			}
			player.setScoreboard(board);
		}
	}

	private static Component legacy(String s) {
		return LegacyComponentSerializer.legacySection().deserialize(s);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (!show)
			return "hide sidebar from " + players.toString(event, debug);
		return "show sidebar titled " + title.toString(event, debug) + " with lines " + lines.toString(event, debug)
			+ " to " + players.toString(event, debug);
	}

}

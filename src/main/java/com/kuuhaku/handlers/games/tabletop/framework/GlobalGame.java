/*
 * This file is part of Shiro J Bot.
 * Copyright (C) 2020  Yago Gimenez (KuuHaKu)
 *
 * Shiro J Bot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shiro J Bot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shiro J Bot.  If not, see <https://www.gnu.org/licenses/>
 */

package com.kuuhaku.handlers.games.tabletop.framework;

import com.github.ygimenez.model.ThrowingBiConsumer;
import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.controller.postgresql.MatchDAO;
import com.kuuhaku.controller.postgresql.MatchMakingRatingDAO;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Hand;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Shoukan;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.SlotColumn;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.enums.Side;
import com.kuuhaku.model.common.DailyQuest;
import com.kuuhaku.model.common.MatchInfo;
import com.kuuhaku.model.enums.DailyTask;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.model.persistent.MatchHistory;
import com.kuuhaku.model.persistent.MatchMakingRating;
import com.kuuhaku.model.persistent.MatchRound;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.JSONObject;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class GlobalGame {
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final ShardManager handler;
	private final Board board;
	private final GameChannel channel;
	private final JSONObject custom;
	private final MatchHistory history = new MatchHistory();
	private final Map<String, Double> divergence = new HashMap<>();
	private final boolean ranked;
	private Consumer<Message> onExpiration;
	private Consumer<Message> onWO;
	private Future<?> timeout;
	private int round = 0;
	private User current;
	private boolean closed = false;
	private boolean wo = false;

	public GlobalGame(ShardManager handler, Board board, GameChannel channel, boolean ranked) {
		this.handler = handler;
		this.board = board;
		this.channel = channel;
		this.current = handler.getUserById(board.getPlayers().getCurrent().getId());
		this.ranked = ranked;
		this.custom = null;
	}

	public GlobalGame(ShardManager handler, Board board, GameChannel channel, boolean ranked, JSONObject custom) {
		this.handler = handler;
		this.board = board;
		this.channel = channel;
		this.current = handler.getUserById(board.getPlayers().getCurrent().getId());
		this.ranked = ranked;
		this.custom = custom;
	}

	public void setActions(Consumer<Message> onExpiration, Consumer<Message> onWO) {
		this.onExpiration = onExpiration;
		this.onWO = onWO;
	}

	public abstract void start();

	public abstract boolean canInteract(GuildMessageReceivedEvent evt);

	public abstract void play(GuildMessageReceivedEvent evt);

	public void resetTimer() {
		if (timeout != null) timeout.cancel(true);
		timeout = null;
		round++;
		Player p = null;
		while (p == null || !p.isInGame()) {
			p = board.getPlayers().getNext();
		}
		current = handler.getUserById(p.getId());
		assert current != null;
		if (round > 0)
			timeout = executor.schedule(() ->
					channel.sendMessage(current.getAsMention() + " perdeu por W.O.! (" + getRound() + " turnos)")
							.queue(s -> {
								onWO.accept(s);
								closed = true;
								wo = true;
							}), 3, TimeUnit.MINUTES);
		else timeout = executor.schedule(() ->
				channel.sendMessage("❌ | Tempo expirado, por favor inicie outra sessão.")
						.queue(s -> {
							onExpiration.accept(s);
							closed = true;
						}), 3, TimeUnit.MINUTES);

		for (int y = 0; y < board.getMatrix().length; y++) {
			for (int x = 0; x < board.getMatrix().length; x++) {
				Piece pc = board.getPieceOrDecoyAt(Spot.of(x, y));
				if (pc instanceof Decoy && current.getId().equals(pc.getOwnerId()))
					board.setPieceAt(Spot.of(x, y), null);
			}
		}
	}

	public void resetTimer(Shoukan shkn) {
		if (timeout != null) timeout.cancel(true);
		timeout = null;

		Hand top = shkn.getHands().get(Side.TOP);
		Hand bot = shkn.getHands().get(Side.BOTTOM);
		getCurrRound().setScript(new JSONObject() {{
			put("top", new JSONObject() {{
				put("id", top.getUser().getId());
				put("hp", top.getHp());
				put("mana", top.getMana());
				put("champions", shkn.getArena().getSlots().get(Side.TOP)
						.stream()
						.map(SlotColumn::getTop)
						.filter(Objects::nonNull)
						.count()
				);
				put("equipments", shkn.getArena().getSlots().get(Side.TOP)
						.stream()
						.map(SlotColumn::getBottom)
						.filter(Objects::nonNull)
						.count()
				);
				put("inHand", top.getCards().size());
				put("deck", top.getDeque().size());
			}});

			put("bottom", new JSONObject() {{
				put("id", bot.getUser().getId());
				put("hp", bot.getHp());
				put("mana", bot.getMana());
				put("champions", shkn.getArena().getSlots().get(Side.BOTTOM)
						.stream()
						.map(SlotColumn::getTop)
						.filter(Objects::nonNull)
						.count()
				);
				put("equipments", shkn.getArena().getSlots().get(Side.BOTTOM)
						.stream()
						.map(SlotColumn::getBottom)
						.filter(Objects::nonNull)
						.count()
				);
				put("inHand", bot.getCards().size());
				put("deck", bot.getDeque().size());
			}});
		}});

		round++;
		Player p = null;
		while (p == null || !p.isInGame()) {
			p = board.getPlayers().getNext();
		}
		current = handler.getUserById(p.getId());
		assert current != null;
		if (round > 0)
			timeout = executor.schedule(() ->
					channel.sendMessage(current.getAsMention() + " perdeu por W.O.! (" + getRound() + " turnos)")
							.queue(s -> {
								onWO.accept(s);
								closed = true;
								wo = true;
							}), 3, TimeUnit.MINUTES);
		else timeout = executor.schedule(() ->
				channel.sendMessage("❌ | Tempo expirado, por favor inicie outra sessão.")
						.queue(s -> {
							onExpiration.accept(s);
							closed = true;
						}), 3, TimeUnit.MINUTES);

		for (int y = 0; y < board.getMatrix().length; y++) {
			for (int x = 0; x < board.getMatrix().length; x++) {
				Piece pc = board.getPieceOrDecoyAt(Spot.of(x, y));
				if (pc instanceof Decoy && current.getId().equals(pc.getOwnerId()))
					board.setPieceAt(Spot.of(x, y), null);
			}
		}
	}

	public void resetTimerKeepTurn() {
		if (timeout != null) timeout.cancel(true);
		timeout = null;
		if (round > 0)
			timeout = executor.schedule(() ->
					channel.sendMessage(current.getAsMention() + " perdeu por W.O.! (" + getRound() + " turnos)")
							.queue(s -> {
								onWO.accept(s);
								closed = true;
								wo = true;
							}), 3, TimeUnit.MINUTES);
		else timeout = executor.schedule(() ->
				channel.sendMessage("❌ | Tempo expirado, por favor inicie outra sessão.")
						.queue(s -> {
							onExpiration.accept(s);
							closed = true;
						}), 3, TimeUnit.MINUTES);
	}

	public ShardManager getHandler() {
		return handler;
	}

	public Board getBoard() {
		return board;
	}

	public int getRound() {
		return round;
	}

	public User getCurrent() {
		return current;
	}

	public User getPlayerById(String id) {
		return handler.getUserById(id);
	}

	public JSONObject getCustom() {
		return custom;
	}

	public MatchHistory getHistory() {
		return history;
	}

	public Map<String, Double> getDivergence() {
		return divergence;
	}

	public abstract Map<String, ThrowingBiConsumer<Member, Message>> getButtons();

	public GameChannel getChannel() {
		return channel;
	}

	public MatchRound getCurrRound() {
		return history.getRound(round);
	}

	public boolean isOpen() {
		return !closed;
	}

	public void close() {
		if (closed) return;
		closed = true;
		if (timeout != null) timeout.cancel(true);
		timeout = null;
		executor.shutdownNow();

		if (round > 0 && custom == null) {
			history.setRanked(ranked);
			history.setWo(wo);
			MatchDAO.saveMatch(history);

			Map<Side, List<MatchInfo>> result = MatchMakingRating.calcMMR(history);
			for (Side s : Side.values()) {
				Side other = s == Side.TOP ? Side.BOTTOM : Side.TOP;

				for (MatchInfo info : result.get(s)) {
					Map<String, Integer> yourResult = info.getInfo();
					Map<String, Integer> theirResult = Helper.mergeInfo(result.get(other)).getInfo();
					MatchMakingRating yourMMR = MatchMakingRatingDAO.getMMR(info.getId());
					long theirMMR = Helper.getAverageMMR(result.get(other).stream().map(MatchInfo::getId).toArray(String[]::new));
					int spentMana = yourResult.get("mana");
					int damageDealt = theirResult.get("hp");

					if (history.getWinner() == s) {
						double manaEff = 1 + Math.max(-0.75, Math.min(spentMana * 0.5 / 5, 0.25));
						double damageEff = (double) -damageDealt / yourResult.size();
						double expEff = 5000d / yourResult.size();
						double sustainEff = 1 + yourResult.get("hp") / 5000f;
						long mmr = Math.round(250 * manaEff + (125 * (damageEff / expEff) + 125 * sustainEff));


						yourMMR.addMMR(mmr / (wo ? 2 : 1), theirMMR, ranked);
						yourMMR.addWin();
						if (ranked) yourMMR.increaseRankPoints(theirMMR);

						Account acc = AccountDAO.getAccount(yourMMR.getUid());
						if (acc.hasPendingQuest()) {
							Map<DailyTask, Integer> pg = acc.getDailyProgress();
							pg.merge(DailyTask.WINS_TASK, 1, Integer::sum);

							double div = divergence.getOrDefault(yourMMR.getUid(), -1d);
							if (div > -1) {
								DailyQuest dq = DailyQuest.getQuest(Long.parseLong(yourMMR.getUid()));
								if (div >= dq.getDivergence()) pg.merge(DailyTask.OFFMETA_TASK, 1, Integer::sum);
							}

							acc.setDailyProgress(pg);
							AccountDAO.saveAccount(acc);
						}
					} else if (history.getWinner() == other) {
						double manaEff = 1 + Math.max(-0.75, Math.min(5 * 0.5 / spentMana, 0.25));
						double damageEff = (double) -damageDealt / yourResult.size();
						double expEff = 5000d / yourResult.size();
						double sustainEff = 1 + yourResult.get("hp") / 5000d;
						long mmr = Math.round(250 * manaEff - (125 * (damageEff / expEff) + 125 * sustainEff));

						yourMMR.removeMMR(mmr * (wo ? 2 : 1), theirMMR, ranked);
						yourMMR.addLoss();
						if (ranked) yourMMR.decreaseRankPoints(theirMMR);
					}

					MatchMakingRatingDAO.saveMMR(yourMMR);
				}
			}
		}
	}
}

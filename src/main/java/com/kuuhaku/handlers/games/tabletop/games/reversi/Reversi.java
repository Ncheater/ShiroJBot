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

package com.kuuhaku.handlers.games.tabletop.games.reversi;

import com.kuuhaku.handlers.games.tabletop.framework.Board;
import com.kuuhaku.handlers.games.tabletop.framework.Game;
import com.kuuhaku.handlers.games.tabletop.framework.Piece;
import com.kuuhaku.handlers.games.tabletop.framework.Spot;
import com.kuuhaku.handlers.games.tabletop.framework.enums.BoardSize;
import com.kuuhaku.handlers.games.tabletop.games.reversi.pieces.Disk;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Reversi extends Game {
	private final Map<String, Piece> pieces;
	private final TextChannel channel;
	private Message message;
	private final ListenerAdapter listener = new ListenerAdapter() {
		@Override
		public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
			if (canInteract(event)) play(event);
		}
	};
	private boolean stalemate = false;

	public Reversi(JDA handler, TextChannel channel, int bet, User... players) {
		super(handler, new Board(BoardSize.S_8X8, bet, Arrays.stream(players).map(User::getId).toArray(String[]::new)), channel);
		this.channel = channel;
		this.pieces = Map.of(
				players[0].getId(), new Disk(players[0].getId(), false, "pieces/man.png"),
				players[1].getId(), new Disk(players[1].getId(), true, "pieces/man.png")
		);

		getBoard().setMatrix(new Piece[][]{
				new Piece[]{null, null, null, null, null, null, null, null},
				new Piece[]{null, null, null, null, null, null, null, null},
				new Piece[]{null, null, null, null, null, null, null, null},
				new Piece[]{null, null, null, pieces.get(players[0].getId()), pieces.get(players[1].getId()), null, null, null},
				new Piece[]{null, null, null, pieces.get(players[1].getId()), pieces.get(players[0].getId()), null, null, null},
				new Piece[]{null, null, null, null, null, null, null, null},
				new Piece[]{null, null, null, null, null, null, null, null},
				new Piece[]{null, null, null, null, null, null, null, null}
		});

		setActions(
				s -> close(),
				s -> getBoard().awardWinner(this, getBoard().getPlayers().getNext().getId())
		);
	}

	@Override
	public void start() {
		message = channel.sendMessage(getCurrent().getAsMention() + " você começa!").addFile(Helper.getBytes(getBoard().render()), "board.jpg").complete();
		getHandler().addEventListener(listener);
	}

	@Override
	public boolean canInteract(GuildMessageReceivedEvent evt) {
		Predicate<GuildMessageReceivedEvent> condition = e -> e.getChannel().getId().equals(channel.getId());

		return condition
				.and(e -> e.getAuthor().getId().equals(getCurrent().getId()))
				.and(e -> e.getMessage().getContentRaw().length() <= 6)
				.and(e -> {
					char[] chars = e.getMessage().getContentRaw().toCharArray();
					if (e.getMessage().getContentRaw().equalsIgnoreCase("ff")) return true;
					else if (e.getMessage().getContentRaw().equalsIgnoreCase("passar")) return true;
					else return chars.length == 2 && Character.isLetter(chars[0]) && Character.isDigit(chars[1]);
				})
				.test(evt);
	}

	@Override
	public void play(GuildMessageReceivedEvent evt) {
		Message message = evt.getMessage();
		String command = message.getContentRaw();

		if (command.equalsIgnoreCase("ff")) {
			channel.sendMessage(getCurrent().getAsMention() + " desistiu! (" + getRound() + " turnos)").queue();
			getBoard().awardWinner(this, getBoard().getPlayers().getNext().getId());
			close();
			return;
		} else if (command.equalsIgnoreCase("passar")) {
			if (stalemate) {
				int whiteCount = 0;
				int blackCount = 0;
				for (int i = 0; i < getBoard().getSize().getHeight(); i++) {
					whiteCount += (int) Arrays.stream(getBoard().getRow(i)).filter(p -> p != null && p.isWhite()).count();
					blackCount += (int) Arrays.stream(getBoard().getRow(i)).filter(p -> p != null && !p.isWhite()).count();
				}

				if (whiteCount > blackCount) {
					User winner = getPlayerById(pieces.entrySet().stream().filter(e -> e.getValue().isWhite()).map(Map.Entry::getKey).collect(Collectors.joining()));
					channel.sendMessage(winner.getAsMention() + " venceu! (" + whiteCount + " peças)").addFile(Helper.getBytes(getBoard().render()), "board.jpg").queue();
					getBoard().awardWinner(this, winner.getId());
					return;
				} else if (whiteCount < blackCount) {
					User winner = getPlayerById(pieces.entrySet().stream().filter(e -> !e.getValue().isWhite()).map(Map.Entry::getKey).collect(Collectors.joining()));
					channel.sendMessage(winner.getAsMention() + " venceu! (" + blackCount + " peças)").addFile(Helper.getBytes(getBoard().render()), "board.jpg").queue();
					getBoard().awardWinner(this, winner.getId());
					return;
				} else {
					channel.sendMessage("Temos um empate!").addFile(Helper.getBytes(getBoard().render()), "board.jpg").queue();
					close();
					return;
				}
			}
			User current = getCurrent();
			resetTimer();
			this.message.delete().queue();
			this.message = channel.sendMessage(current.getAsMention() + " passou a vez, agora é você " + getCurrent().getAsMention() + ".")
					.addFile(Helper.getBytes(getBoard().render()), "board.jpg")
					.complete();
			stalemate = true;
			return;
		}

		try {
			Spot s = Spot.of(command);

			if (!((Disk) pieces.get(getCurrent().getId())).validate(getBoard(), s, null)) {
				channel.sendMessage("❌ | Casa inválida!").queue();
				return;
			}

			if (getBoard().getPieceAt(s) == null) {
				getBoard().setPieceAt(s, pieces.get(getCurrent().getId()));
			} else {
				channel.sendMessage("❌ | Essa casa já está ocupada!").queue();
				return;
			}

			int whiteCount = 0;
			int blackCount = 0;
			for (int i = 0; i < getBoard().getSize().getHeight(); i++) {
				whiteCount += (int) Arrays.stream(getBoard().getRow(i)).filter(p -> p != null && p.isWhite()).count();
				blackCount += (int) Arrays.stream(getBoard().getRow(i)).filter(p -> p != null && !p.isWhite()).count();
			}

			if (whiteCount + blackCount == 64) {
				if (whiteCount > blackCount) {
					User winner = getPlayerById(pieces.entrySet().stream().filter(e -> e.getValue().isWhite()).map(Map.Entry::getKey).collect(Collectors.joining()));
					channel.sendMessage(winner.getAsMention() + " venceu! (" + whiteCount + " peças)").addFile(Helper.getBytes(getBoard().render()), "board.jpg").queue();
					getBoard().awardWinner(this, winner.getId());
				} else if (whiteCount < blackCount) {
					User winner = getPlayerById(pieces.entrySet().stream().filter(e -> !e.getValue().isWhite()).map(Map.Entry::getKey).collect(Collectors.joining()));
					channel.sendMessage(winner.getAsMention() + " venceu! (" + blackCount + " peças)").addFile(Helper.getBytes(getBoard().render()), "board.jpg").queue();
					getBoard().awardWinner(this, winner.getId());
				} else {
					channel.sendMessage("Temos um empate!").addFile(Helper.getBytes(getBoard().render()), "board.jpg").queue();
					close();
				}
			} else {
				resetTimer();
				this.message.delete().queue();
				this.message = channel.sendMessage("Turno de " + getCurrent().getAsMention()).addFile(Helper.getBytes(getBoard().render()), "board.jpg").complete();
				stalemate = false;
			}
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			channel.sendMessage("❌ | Coordenada inválida.").queue();
		}
	}

	@Override
	public void close() {
		super.close();
		getHandler().removeEventListener(listener);
	}
}
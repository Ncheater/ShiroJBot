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

package com.kuuhaku.handlers.games.tabletop.games.shoukan;

import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.enums.Race;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.enums.Side;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.interfaces.Drawable;
import com.kuuhaku.model.common.Profile;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.model.persistent.Card;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class Hand {
	private final Shoukan game;
	private final User user;
	private final LinkedList<Drawable> deque;
	private final List<Drawable> cards = new ArrayList<>();
	private final Side side;
	private final int startingCount;
	private final int manaPerTurn;
	private int mana;
	private int hp;

	public Hand(Shoukan game, User user, List<Drawable> deque, Side side) {
		Collections.shuffle(deque);

		this.user = user;
		this.deque = new LinkedList<>(deque);
		this.side = side;
		this.game = game;

		this.mana = game.getCustom() == null ? 0 : Helper.minMax(game.getCustom().optInt("mana", 0), 0, 20);
		this.hp = game.getCustom() == null ? 5000 : Helper.minMax(game.getCustom().optInt("hp", 5000), 500, 25000);
		this.startingCount = game.getCustom() == null ? 5 : Helper.minMax(game.getCustom().optInt("stcards", 5), 1, 10);
		this.manaPerTurn = game.getCustom() == null ? 5 : Helper.minMax(game.getCustom().optInt("manapt", 5), 1, 20);

		if (game.getCustom() != null) {
			if (!game.getCustom().optBoolean("banEquip", false))
				deque.removeIf(d -> d instanceof Equipment);
			if (!game.getCustom().optBoolean("banField", false))
				deque.removeIf(d -> d instanceof Field);
		}

		redrawHand();
	}

	public void draw() {
		try {
			if (cards.stream().filter(d -> d instanceof Equipment || d instanceof Field).count() == 4 && deque.stream().anyMatch(d -> d instanceof Champion))
				drawChampion();
			else cards.add(deque.removeFirst().copy());
		} catch (NoSuchElementException ignore) {
		}
	}

	public void draw(Card card) {
		try {
			Drawable dr = deque.stream().filter(c -> c.getCard().equals(card)).findFirst().orElseThrow().copy();
			deque.remove(dr);
			cards.add(dr);
		} catch (NoSuchElementException ignore) {
		}
	}

	public void draw(Drawable drawable) {
		Card card = drawable.getCard();
		try {
			Drawable dr = deque.stream().filter(c -> c.getCard().equals(card)).findFirst().orElseThrow().copy();
			deque.remove(dr);
			cards.add(dr);
		} catch (NoSuchElementException ignore) {
		}
	}

	public void drawChampion() {
		try {
			Drawable dr = deque.stream().filter(c -> c instanceof Champion).findFirst().orElseThrow().copy();
			deque.remove(dr);
			cards.add(dr);
		} catch (NoSuchElementException ignore) {
		}
	}

	public void drawEquipment() {
		try {
			Drawable dr = deque.stream().filter(c -> c instanceof Equipment).findFirst().orElseThrow().copy();
			deque.remove(dr);
			cards.add(dr);
		} catch (NoSuchElementException ignore) {
		}
	}

	public void drawRace(Race race) {
		try {
			Drawable dr = deque.stream().filter(c -> c instanceof Champion && ((Champion) c).getRace() == race).findFirst().orElseThrow().copy();
			deque.remove(dr);
			cards.add(dr);
		} catch (NoSuchElementException ignore) {
		}
	}

	public void redrawHand() {
		for (int i = 0; i < startingCount; i++) draw();
	}

	public User getUser() {
		return user;
	}

	public LinkedList<Drawable> getDeque() {
		return deque;
	}

	public List<Drawable> getCards() {
		return cards;
	}

	public Side getSide() {
		return side;
	}

	public void showHand() {
		BufferedImage bi = new BufferedImage(Math.max(5, cards.size()) * 300, 450, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(Profile.FONT.deriveFont(Font.PLAIN, 90));

		List<Drawable> cards = new ArrayList<>(getCards());
		Account acc = AccountDAO.getAccount(user.getId());

		for (int i = 0; i < cards.size(); i++) {
			g2d.drawImage(cards.get(i).drawCard(acc, false), bi.getWidth() / (cards.size() + 1) * (i + 1) - (225 / 2), 100, null);
			if (cards.get(i).isAvailable())
				Profile.printCenteredString(String.valueOf(i + 1), 225, bi.getWidth() / (cards.size() + 1) * (i + 1) - (225 / 2), 90, g2d);
		}

		g2d.dispose();

		user.openPrivateChannel().complete()
				.sendMessage("Escolha uma carta para jogar (digite a posição da carta na mão, no campo e se ela posicionada em modo de ataque (`A`), defesa (`D`) ou virada para baixo (`B`). Ex: `0,0,a`), mude a postura de uma carta (digite apenas a posição da carta no campo) ou use os botões na mensagem enviada para avançar o turno, comprar uma carta ou render-se.")
				.addFile(Helper.getBytes(bi, "png"), "hand.png")
				.queue();
	}

	public void showEnemyHand() {
		Hand enemy = game.getHands().get(side == Side.TOP ? Side.BOTTOM : Side.TOP);
		BufferedImage bi = new BufferedImage(Math.max(5, enemy.getCards().size()) * 300, 450, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(Profile.FONT.deriveFont(Font.PLAIN, 90));

		List<Drawable> cards = new ArrayList<>(enemy.getCards());
		Account acc = AccountDAO.getAccount(enemy.getUser().getId());

		for (int i = 0; i < cards.size(); i++)
			g2d.drawImage(cards.get(i).drawCard(acc, false), bi.getWidth() / (cards.size() + 1) * (i + 1) - (225 / 2), 100, null);

		try {
			BufferedImage so = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("shoukan/shinigami_overlay.png")));
			g2d.drawImage(so, 0, 0, null);
		} catch (IOException ignore) {
		}

		g2d.dispose();

		user.openPrivateChannel().complete()
				.sendMessage("Visualizando as cartas na mão inimiga.")
				.addFile(Helper.getBytes(bi, "png"), "hand.png")
				.queue();
	}

	public int sumAttack() {
		return cards.stream().filter(d -> d instanceof Champion).mapToInt(d -> ((Champion) d).getAtk()).sum();
	}

	public int getMana() {
		return mana;
	}

	public int getManaPerTurn() {
		return manaPerTurn;
	}

	public void setMana(int value) {
		mana = value;
	}

	public void addMana(int value) {
		mana += value;
	}

	public void removeMana(int value) {
		mana = Math.max(0, mana - value);
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int value) {
		hp = value;
	}

	public void addHp(int value) {
		hp += value;
	}

	public void removeHp(int value) {
		hp = Math.max(0, hp - value);
	}

	public void crippleHp(int value) {
		hp = Math.max(1, hp - value);
	}
}

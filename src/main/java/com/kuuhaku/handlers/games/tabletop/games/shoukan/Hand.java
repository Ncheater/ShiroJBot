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
import com.kuuhaku.handlers.games.tabletop.games.shoukan.enums.Side;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.interfaces.Drawable;
import com.kuuhaku.model.common.Profile;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.model.persistent.Card;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

public class Hand {
	private final User user;
	private final LinkedList<Drawable> deque;
	private final List<Drawable> cards = new ArrayList<>();
	private final Side side;
	private int mana = 999;
	private int hp = 5000;

	public Hand(User user, List<Drawable> deque, Side side) {
		Collections.shuffle(deque);

		this.user = user;
		this.deque = new LinkedList<>(deque);
		this.side = side;

		redrawHand();
	}

	public void draw() throws NoSuchElementException {
		cards.add(deque.removeFirst().copy());
	}

	public void draw(Card card) throws NoSuchElementException {
		Drawable dr = deque.stream().filter(c -> c.getCard().equals(card)).findFirst().orElseThrow().copy();
		deque.remove(dr);
		cards.add(dr);
	}

	public void redrawHand() {
		for (int i = 0; i < 5; i++) draw();
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
		BufferedImage bi = new BufferedImage(cards.size() * (225 + 6), 450, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(Profile.FONT.deriveFont(Font.PLAIN, 90));

		List<Drawable> cards = new ArrayList<>(getCards());
		Account acc = AccountDAO.getAccount(user.getId());

		for (int i = 0; i < cards.size(); i++) {
			g2d.drawImage(cards.get(i).drawCard(acc, false), bi.getWidth() / (cards.size() + 1) * (i + 1) - (225 / 2), 100, null);
			if (cards.get(i).isAvailable())
				Profile.printCenteredString(String.valueOf(i), 225, bi.getWidth() / (cards.size() + 1) * (i + 1) - (225 / 2), 90, g2d);
		}

		g2d.dispose();

		user.openPrivateChannel().complete()
				.sendMessage("Escolha uma carta para jogar ou digite `finalizar` para encerrar sua vez.")
				.addFile(Helper.getBytes(bi), "hand.png")
				.queue();
	}

	public int getMana() {
		return mana;
	}

	public void addMana(int value) {
		mana += value;
	}

	public void removeMana(int value) {
		mana -= value;
	}

	public int getHp() {
		return hp;
	}

	public void addHp(int value) {
		hp += value;
	}

	public void removeHp(int value) {
		hp -= value;
	}
}
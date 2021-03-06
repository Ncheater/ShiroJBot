/*
 * This file is part of Shiro J Bot.
 * Copyright (C) 2021  Yago Gimenez (KuuHaKu)
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

package com.kuuhaku.model.common;

import com.kuuhaku.controller.postgresql.RarityColorsDAO;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Champion;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Equipment;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.interfaces.Drawable;
import com.kuuhaku.model.enums.Fonts;
import com.kuuhaku.model.enums.KawaiponRarity;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.model.persistent.Card;
import com.kuuhaku.model.persistent.KawaiponCard;
import com.kuuhaku.model.persistent.RarityColors;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.NContract;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class KawaiponBook {
	private final Set<KawaiponCard> cards;
	private static final int COLUMN_COUNT = 20;
	private static final int CARD_WIDTH = 160;
	private static final int CARD_HEIGHT = 250;

	public KawaiponBook(Set<KawaiponCard> cards) {
		this.cards = cards;
	}

	public KawaiponBook() {
		this.cards = null;
	}

	public BufferedImage view(List<Card> cardList, String title, boolean foil) throws IOException, InterruptedException {
		String text;
		if (foil) text = "« " + title + " »";
		else text = title;
		assert this.cards != null;
		List<KawaiponCard> cards = List.copyOf(this.cards);
		cardList.sort(Comparator
				.comparing(Card::getRarity, Comparator.comparingInt(KawaiponRarity::getIndex).reversed())
				.thenComparing(Card::getName, String.CASE_INSENSITIVE_ORDER)
		);
		List<List<KawaiponCard>> chunks = Helper.chunkify(cardList.stream().map(c -> new KawaiponCard(c, foil)).collect(Collectors.toList()), COLUMN_COUNT);
		chunks.removeIf(List::isEmpty);

		BufferedImage header = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/header.png")));
		BufferedImage footer = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/footer.png")));
		BufferedImage slot = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/slot.png")));

		Graphics2D g2d = header.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(Fonts.DOREKING.deriveFont(Font.BOLD, 72));
		if (foil) g2d.setColor(Color.yellow);
		Profile.printCenteredString(text, 4026, 35, 168, g2d);

		NContract<BufferedImage> act = new NContract<>(chunks.size());
		act.setAction(imgs -> {
			BufferedImage bg = new BufferedImage(header.getWidth(), header.getHeight() + footer.getHeight() + (299 * imgs.size()), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bg.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.drawImage(header, 0, 0, null);

			for (int i = 0; i < imgs.size(); i++) {
				g.drawImage(imgs.get(i), 0, header.getHeight() + 299 * i, null);
			}

			g.drawImage(footer, 0, bg.getHeight() - footer.getHeight(), null);
			g.dispose();

			return bg;
		});

		ExecutorService th = Executors.newFixedThreadPool(5);
		for (int c = 0; c < chunks.size(); c++) {
			int finalC = c;
			th.execute(() -> {
				try {
					BufferedImage row = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/row.png")));
					Graphics2D g = row.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					List<KawaiponCard> chunk = chunks.get(finalC);
					for (int i = 0; i < chunk.size(); i++) {
						BufferedImage card = cards.contains(chunk.get(i)) ? chunk.get(i).getCard().drawCard(foil) : slot;

						int width = 4026 / COLUMN_COUNT;
						int actualWidth = width * chunk.size();
						int x = 35 + ((4026 - actualWidth) / 2) + ((width - CARD_WIDTH) / 2) + width * i;

						int height = row.getHeight();
						int y = ((height - CARD_HEIGHT) / 2);

						if (cards.contains(chunk.get(i))) {
							g.setFont(Fonts.DOREKING.deriveFont(Font.PLAIN, 20));
							RarityColors rc = RarityColorsDAO.getColor(chunk.get(i).getCard().getRarity());

							g2d.setColor(foil ? rc.getSecondary() : rc.getPrimary());

							g.drawImage(card, x, y, CARD_WIDTH, CARD_HEIGHT, null);
							Profile.printCenteredString(StringUtils.abbreviate(chunk.get(i).getName(), 15), CARD_WIDTH, x, y + 274, g);
						} else if (chunk.get(i).getCard().getRarity().equals(KawaiponRarity.ULTIMATE)) {
							g.setFont(Fonts.DOREKING.deriveFont(Font.PLAIN, 20));
							g.setBackground(Color.black);
							g.setColor(Color.white);

							g.drawImage(card, x, y, CARD_WIDTH, CARD_HEIGHT, null);
							Profile.printCenteredString(StringUtils.abbreviate(chunk.get(i).getName(), 15), CARD_WIDTH, x, y + 274, g);
						} else {
							g.setFont(Fonts.DOREKING.deriveFont(Font.PLAIN, 20));
							g.setBackground(Color.black);
							g.setColor(Color.white);

							g.drawImage(card, x, y, CARD_WIDTH, CARD_HEIGHT, null);
							Profile.printCenteredString("???", CARD_WIDTH, x, y + 274, g);
						}
					}

					g.dispose();

					act.addSignature(finalC, row);
					row.flush();
				} catch (IOException ignore) {
				}
			});
		}

		try {
			return act.get();
		} catch (ExecutionException e) {
			return null;
		} finally {
			th.shutdownNow();
		}
	}

	public BufferedImage view(List<Drawable> cardList, Account acc, String title, boolean senshi) throws IOException, InterruptedException {
		List<Drawable> cards;
		if (senshi)
			cards = cardList.stream()
					.peek(d -> d.setAcc(acc))
					.sorted(Comparator
							.comparing(d -> ((Champion) d).getMana())
							.reversed()
							.thenComparing(c -> ((Champion) c).getCard().getName(), String.CASE_INSENSITIVE_ORDER)
					)
					.collect(Collectors.toList());
		else
			cards = cardList.stream()
					.peek(d -> d.setAcc(acc))
					.sorted(Comparator
							.comparing(d -> ((Equipment) d).getTier())
							.reversed()
							.thenComparing(c -> ((Equipment) c).getCard().getName(), String.CASE_INSENSITIVE_ORDER)
					)
					.collect(Collectors.toList());

		List<List<Drawable>> chunks = Helper.chunkify(cards, COLUMN_COUNT);
		chunks.removeIf(List::isEmpty);

		BufferedImage header = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/header.png")));
		BufferedImage footer = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/footer.png")));

		Graphics2D g2d = header.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(Fonts.DOREKING.deriveFont(Font.BOLD, 72));
		Profile.printCenteredString(title, 4026, 35, 168, g2d);

		NContract<BufferedImage> act = new NContract<>(chunks.size());
		act.setAction(imgs -> {
			BufferedImage bg = new BufferedImage(header.getWidth(), header.getHeight() + footer.getHeight() + (299 * imgs.size()), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bg.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.drawImage(header, 0, 0, null);

			for (int i = 0; i < imgs.size(); i++) {
				g.drawImage(imgs.get(i), 0, header.getHeight() + 299 * i, null);
			}

			g.drawImage(footer, 0, bg.getHeight() - footer.getHeight(), null);
			g.dispose();

			return bg;
		});

		ExecutorService th = Executors.newFixedThreadPool(5);
		for (int c = 0; c < chunks.size(); c++) {
			int finalC = c;
			th.execute(() -> {
				try {
					BufferedImage row = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/row.png")));
					Graphics2D g = row.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setColor(Color.white);

					List<Drawable> chunk = chunks.get(finalC);
					for (int i = 0; i < chunk.size(); i++) {
						BufferedImage card = chunk.get(i).drawCard(false);

						int width = 4026 / COLUMN_COUNT;
						int actualWidth = width * chunk.size();
						int x = 35 + ((4026 - actualWidth) / 2) + ((width - CARD_WIDTH) / 2) + width * i;

						int height = row.getHeight();
						int y = ((height - CARD_HEIGHT) / 2);

						g.setFont(Fonts.DOREKING.deriveFont(Font.PLAIN, 20));
						g.drawImage(card, x, y, CARD_WIDTH, CARD_HEIGHT, null);
						Profile.printCenteredString(StringUtils.abbreviate(chunk.get(i).getCard().getName(), 15), CARD_WIDTH, x, y + 274, g);
					}

					g.dispose();

					act.addSignature(finalC, row);
					row.flush();
				} catch (IOException ignore) {
				}
			});
		}

		try {
			return act.get();
		} catch (ExecutionException e) {
			return null;
		} finally {
			th.shutdownNow();
		}
	}
}

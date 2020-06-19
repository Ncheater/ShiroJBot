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

package com.kuuhaku.model.persistent;

import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.KawaiponRarity;
import com.kuuhaku.utils.ShiroInfo;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.persistence.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Entity
@Table(name = "card")
public class Card {
	@Id
	private String id;

	@Column(columnDefinition = "VARCHAR(191) DEFAULT ''")
	private String name = "";

	@Column(columnDefinition = "VARCHAR(191) DEFAULT ''")
	private String anime = "";

	@Enumerated(EnumType.STRING)
	private KawaiponRarity rarity = KawaiponRarity.COMMON;

	@Column(columnDefinition = "VARCHAR(191) DEFAULT ''")
	private String imgurId = "";

	public String getName() {
		return name;
	}

	public String getAnime() {
		return anime;
	}

	public KawaiponRarity getRarity() {
		return rarity;
	}

	public BufferedImage getCard() {
		try {
			byte[] cardBytes = ShiroInfo.getCardCache().get(imgurId, () -> IOUtils.toByteArray(Helper.getImage("https://i.imgur.com/" + imgurId + ".jpg")));
			try (ByteArrayInputStream bais = new ByteArrayInputStream(cardBytes)) {
				BufferedImage card = ImageIO.read(bais);
				BufferedImage frame = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/frames/" + rarity.name().toLowerCase() + ".png")));
				BufferedImage canvas = new BufferedImage(frame.getWidth(), frame.getHeight(), frame.getType());

				Graphics2D g2d = canvas.createGraphics();
				g2d.drawImage(card, 10, 10, null);
				g2d.drawImage(frame, 0, 0, null);

				g2d.dispose();

				return canvas;
			}
		} catch (IOException | ExecutionException e) {
			Helper.logger(this.getClass()).error(e + " | " + e.getStackTrace()[0]);
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Card card = (Card) o;
		return Objects.equals(id, card.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}

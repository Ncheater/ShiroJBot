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

import com.kuuhaku.model.enums.Slot;

import java.util.ArrayList;
import java.util.List;

public class GamblePool {
	public static record Gamble(Slot slot, int weight) {
	}

	public void addGamble(Gamble gamble) {
		for (int i = 0; i < gamble.weight(); i++) {
			g.add(gamble);
		}
	}

	private static final List<Gamble> g = new ArrayList<>();

	public Slot[] getPool() {
		List<Slot> pool = new ArrayList<>();
		for (Gamble gamble : g) {
			pool.add(gamble.slot());
		}

		return pool.toArray(new Slot[0]);
	}
}

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

import com.kuuhaku.model.enums.TrophyType;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "trophy")
public class Trophy {
	@Id
	private String uid;

	@ElementCollection
	private Set<TrophyType> trophies = EnumSet.noneOf(TrophyType.class);

	public Trophy(String uid) {
		this.uid = uid;
	}

	public Trophy() {
	}

	public String getUid() {
		return uid;
	}

	public Set<TrophyType> getTrophies() {
		return trophies;
	}
}
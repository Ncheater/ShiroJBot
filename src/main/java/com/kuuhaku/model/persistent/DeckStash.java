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

import com.kuuhaku.handlers.games.tabletop.games.shoukan.Champion;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Equipment;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Field;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "deckstash")
public class DeckStash {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(columnDefinition = "VARCHAR(191) NOT NULL DEFAULT ''")
	private String uid = "";

	@LazyCollection(LazyCollectionOption.FALSE)
	@ManyToMany
	private List<Champion> champions = new ArrayList<>();

	@LazyCollection(LazyCollectionOption.FALSE)
	@ManyToMany
	private List<Equipment> equipments = new ArrayList<>();

	@LazyCollection(LazyCollectionOption.FALSE)
	@ManyToMany
	private List<Field> fields = new ArrayList<>();

	@Column(columnDefinition = "VARCHAR(191) NOT NULL DEFAULT ''")
	private String destinyDraw = "";

	public DeckStash(List<Champion> champions, List<Equipment> equipments, List<Field> fields, String destinyDraw) {
		this.champions = champions;
		this.equipments = equipments;
		this.fields = fields;
		this.destinyDraw = destinyDraw;
	}

	public DeckStash() {
	}

	public int getId() {
		return id;
	}

	public String getUid() {
		return uid;
	}

	public List<Champion> getChampions() {
		return champions;
	}

	public void setChampions(List<Champion> champions) {
		this.champions = champions;
	}

	public List<Equipment> getEquipments() {
		return equipments;
	}

	public void setEquipments(List<Equipment> equipments) {
		this.equipments = equipments;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public List<Integer> getDestinyDraw() {
		if (destinyDraw.isBlank()) return null;
		return Arrays.stream(destinyDraw.split(",")).map(Integer::parseInt).collect(Collectors.toList());
	}

	public void setDestinyDraw(Integer[] destinyDraw) {
		if (destinyDraw == null)
			this.destinyDraw = "";
		else
			this.destinyDraw = Arrays.stream(destinyDraw).map(String::valueOf).collect(Collectors.joining(","));
	}
}

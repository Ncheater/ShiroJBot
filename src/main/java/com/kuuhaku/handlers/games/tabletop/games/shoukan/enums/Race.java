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

package com.kuuhaku.handlers.games.tabletop.games.shoukan.enums;

public enum Race {
	HUMAN,
	ELF,
	BESTIAL,
	UNDEAD,
	MACHINE,
	ULTIMATE,
	DIVINITY,
	MYSTICAL,
	CREATURE,
	SPIRIT,
	DEMON;

	@Override
	public String toString() {
		switch (this) {
			case HUMAN:
				return "Humano";
			case ELF:
				return "Elfo";
			case BESTIAL:
				return "Bestial";
			case UNDEAD:
				return "Morto-vivo";
			case MACHINE:
				return "Máquina";
			case ULTIMATE:
				return "Ultimate";
			case DIVINITY:
				return "Divindade";
			case MYSTICAL:
				return "Místico";
			case CREATURE:
				return "Criatura";
			case SPIRIT:
				return "Espírito";
			case DEMON:
				return "Demônio";
			default:
				return null;
		}
	}
}
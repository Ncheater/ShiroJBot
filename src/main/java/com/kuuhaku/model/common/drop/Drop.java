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

package com.kuuhaku.model.common.drop;

import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.controller.postgresql.CardDAO;
import com.kuuhaku.controller.postgresql.ExceedDAO;
import com.kuuhaku.controller.postgresql.KawaiponDAO;
import com.kuuhaku.controller.sqlite.MemberDAO;
import com.kuuhaku.model.enums.AnimeName;
import com.kuuhaku.model.enums.ExceedEnum;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class Drop implements Prize {
	private final AnimeName anime = AnimeName.values()[Helper.rng(AnimeName.values().length, true)];
	private final ExceedEnum exceed = ExceedEnum.values()[Helper.rng(ExceedEnum.values().length, true)];
	private final int[] values = {
			1 + Helper.rng((int) CardDAO.totalCards(anime) - 1, false),
			1 + Helper.rng(6, false),
			1 + Helper.rng((int) CardDAO.totalCards() / 2 - 1, false),
			1 + Helper.rng(MemberDAO.getHighestLevel() / 2 - 1, false)
	};
	private final List<Pair<String, Function<User, Boolean>>> condition = new ArrayList<>() {{
		add(Pair.of("Ter " + values[2] + " carta" + (values[2] != 1 ? "s" : "") + " ou mais.", u ->
				KawaiponDAO.getKawaipon(u.getId()).getCards().size() >= values[2]));

		add(Pair.of("Ter " + values[0] + " carta" + (values[0] != 1 ? "s" : "") + " de " + anime.toString() + " ou mais.", u ->
				KawaiponDAO.getKawaipon(u.getId()).getCards().stream().filter(k -> k.getCard().getAnime().equals(anime)).count() >= values[0]));

		add(Pair.of("Ser level " + values[3] + " ou maior.", u ->
				MemberDAO.getMemberByMid(u.getId()).stream().anyMatch(m -> m.getLevel() >= values[3])));

		add(Pair.of("Ter até 1000 créditos.", u ->
				AccountDAO.getAccount(u.getId()).getBalance() <= 1000));

		add(Pair.of("Ter votado " + values[1] + " vez" + (values[1] != 1 ? "es" : "") + " seguidas ou mais.", u ->
				AccountDAO.getAccount(u.getId()).getStreak() >= values[1]));

		add(Pair.of("Ser membro da " + exceed.getName() + ".", u ->
				ExceedDAO.hasExceed(u.getId()) && ExceedDAO.getExceedMember(u.getId()).getExceed().equalsIgnoreCase(exceed.getName())));

		add(Pair.of("Ter dívida ativa.", u ->
				AccountDAO.getAccount(u.getId()).getLoan() > 0));
	}};
	private final Pair<String, Function<User, Boolean>> chosen = condition.get(Helper.rng(condition.size(), true));

	public AnimeName getAnime() {
		return anime;
	}

	public ExceedEnum getExceed() {
		return exceed;
	}

	public int[] getValues() {
		return values;
	}

	public List<Pair<String, Function<User, Boolean>>> getCondition() {
		return condition;
	}

	public Pair<String, Function<User, Boolean>> getChosen() {
		return chosen;
	}
}

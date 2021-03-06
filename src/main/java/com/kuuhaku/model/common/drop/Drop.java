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

package com.kuuhaku.model.common.drop;

import com.kuuhaku.controller.postgresql.*;
import com.kuuhaku.model.enums.ClanTier;
import com.kuuhaku.model.enums.DailyTask;
import com.kuuhaku.model.enums.ExceedEnum;
import com.kuuhaku.model.enums.RankedTier;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.model.persistent.AddedAnime;
import com.kuuhaku.model.persistent.Clan;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public abstract class Drop<P> implements Prize<P> {
	private final AddedAnime anime;
	private final ExceedEnum exceed;
	private final ClanTier tier;
	private final RankedTier ranked;
	private final int[] values;
	private final List<Pair<String, Function<User, Boolean>>> condition;
	private final Pair<String, Function<User, Boolean>> chosen;
	private final P prize;
	private String captcha;

	protected Drop(P prize) {
		List<AddedAnime> animes = List.copyOf(CardDAO.getValidAnime());
		anime = animes.get(Helper.rng(animes.size(), true));
		exceed = ExceedEnum.values()[Helper.rng(ExceedEnum.values().length, true)];
		tier = ClanTier.values()[Helper.rng(ClanTier.values().length, true)];
		ranked = RankedTier.values()[1 + Helper.rng(RankedTier.values().length - 1, true)];
		values = new int[]{
				1 + Helper.rng((int) CardDAO.totalCards(anime.getName()) - 1, false),
				1 + Helper.rng(6, false),
				1 + Helper.rng((int) CardDAO.totalCards() / 2 - 1, false),
				1 + Helper.rng(MemberDAO.getHighestLevel() / 2 - 1, false)
		};
		condition = new ArrayList<>() {{
			add(Pair.of("Ter " + values[2] + " carta" + (values[2] != 1 ? "s" : "") + " ou mais.", u ->
					KawaiponDAO.getKawaipon(u.getId()).getCards().size() >= values[2]));

			add(Pair.of("Ter " + values[0] + " carta" + (values[0] != 1 ? "s" : "") + " de " + anime.toString() + " ou mais.", u ->
					KawaiponDAO.getKawaipon(u.getId()).getCards().stream().filter(k -> k.getCard().getAnime().equals(anime)).count() >= values[0]));

			add(Pair.of("Ser level " + values[3] + " ou maior.", u ->
					MemberDAO.getMembersByUid(u.getId()).stream().anyMatch(m -> m.getLevel() >= values[3])));

			add(Pair.of("Ter até 1.000 créditos.", u ->
					AccountDAO.getAccount(u.getId()).getBalance() <= 1000));

			add(Pair.of("Ter votado " + values[1] + " vez" + (values[1] != 1 ? "es" : "") + " seguida" + (values[1] != 1 ? "s" : "") + " ou mais.", u ->
					AccountDAO.getAccount(u.getId()).getStreak() >= values[1]));

			add(Pair.of("Ser membro da " + exceed.getName().toLowerCase(Locale.ROOT) + ".", u ->
					ExceedDAO.hasExceed(u.getId()) && ExceedDAO.getExceedMember(u.getId()).getExceed().equalsIgnoreCase(exceed.getName())));

			add(Pair.of("Ter dívida ativa.", u ->
					AccountDAO.getAccount(u.getId()).getLoan() > 0));

			add(Pair.of("Ter conta vinculada com o canal do meu Nii-chan.", u ->
					!AccountDAO.getAccount(u.getId()).getTwitchId().isBlank()));

			add(Pair.of("Estar em um clã com tier " + tier.getName().toLowerCase(Locale.ROOT) + " ou superior.", u -> {
				Clan c = ClanDAO.getUserClan(u.getId());
				if (c == null) return false;
				else return c.getTier().ordinal() >= tier.ordinal();
			}));

			add(Pair.of("Possuir ranking " + ranked.getName() + " no Shoukan ou superior.", u ->
					MatchMakingRatingDAO.getMMR(u.getId()).getTier().ordinal() >= ranked.ordinal()));
		}};
		chosen = condition.get(Helper.rng(condition.size(), true));
		this.prize = prize;
	}

	public AddedAnime getAnime() {
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

	@Override
	public String getCaptcha() {
		return Helper.noCopyPaste(getRealCaptcha());
	}

	@Override
	public String getRealCaptcha() {
		if (captcha == null)
			captcha = Helper.getOr(Helper.generateRandomHash(6), String.valueOf(System.currentTimeMillis()).substring(0, 6));
		return captcha;
	}

	@Override
	public P getPrize() {
		return prize;
	}

	@Override
	public Map.Entry<String, Function<User, Boolean>> getRequirement() {
		return chosen;
	}

	@Override
	public void awardInstead(User u, int prize) {
		Account acc = AccountDAO.getAccount(u.getId());
		acc.addCredit(prize, this.getClass());

		if (acc.hasPendingQuest()) {
			Map<DailyTask, Integer> pg = acc.getDailyProgress();
			pg.merge(DailyTask.DROP_TASK, 1, Integer::sum);
			acc.setDailyProgress(pg);
		}

		AccountDAO.saveAccount(acc);
	}
}

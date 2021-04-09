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

package com.kuuhaku.command.commands.discord.misc;

import com.github.ygimenez.method.Pages;
import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.controller.postgresql.*;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.annotations.Requires;
import com.kuuhaku.model.enums.I18n;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.model.persistent.Card;
import com.kuuhaku.model.persistent.StockMarket;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.ShiroInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

@Command(
		name = "retirar",
		aliases = {"recover"},
		usage = "req_card-value",
		category = Category.MISC
)
@Requires({
		Permission.MESSAGE_EMBED_LINKS,
		Permission.MESSAGE_MANAGE,
		Permission.MESSAGE_ADD_REACTION
})
public class RecoverCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		if (args.length < 2) {
			channel.sendMessage("❌ | Você precisa informar uma carta e um valor para recuperar.").queue();
			return;
		} else if (!StringUtils.isNumeric(args[1])) {
			channel.sendMessage(ShiroInfo.getLocale(I18n.PT).getString("err_amount-not-valid")).queue();
			return;
		}

		Card c = CardDAO.getRawCard(args[0]);
		if (c == null) {
			channel.sendMessage("❌ | Essa carta não existe.").queue();
			return;
		}

		Account acc = AccountDAO.getAccount(author.getId());
		StockMarket sm = StockMarketDAO.getCardInvestment(author.getId(), c);
		int amount = Integer.parseInt(args[1]);
		if (sm.getInvestment() < amount) {
			channel.sendMessage("❌ | Você não tem ações suficientes.").queue();
			return;
		}

		double stock = 1 + DoubleStream.of(
				CardMarketDAO.getStockValue(c),
				EquipmentMarketDAO.getStockValue(c),
				FieldMarketDAO.getStockValue(c)
		).filter(d -> d > 0).average().orElse(0);

		int readjust = (int) Math.round(amount * stock);

		sm.setInvestment(sm.getInvestment() - amount);

		Main.getInfo().getConfirmationPending().put(author.getId(), true);
		channel.sendMessage("Você está prestes vender " + amount + " ações (" + Helper.separate(readjust) + " créditos) da carta " + c.getName() + ", deseja confirmar?")
				.queue(s -> Pages.buttonize(s, Map.of(Helper.ACCEPT, (mb, ms) -> {
							Main.getInfo().getConfirmationPending().remove(author.getId());
							acc.addCredit(readjust, this.getClass());
							AccountDAO.saveAccount(acc);

							StockMarketDAO.saveInvestment(sm);

							s.delete().flatMap(d -> channel.sendMessage("✅ | Ações vendidas com sucesso!")).queue();
						}), true, 1, TimeUnit.MINUTES,
						u -> u.getId().equals(author.getId()),
						ms -> Main.getInfo().getConfirmationPending().remove(author.getId())
				));
	}
}

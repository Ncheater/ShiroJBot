/*
 * This file is part of Shiro J Bot.
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

package com.kuuhaku.command.commands.fun;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.mysql.AccountDAO;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.model.persistent.Slots;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SlotsCommand extends Command {
	private final List<String> rolled = new ArrayList<>();

	public SlotsCommand() {
		super("slots", new String[]{"roleta"}, "<aposta>", "Aposta um quantidade de créditos nos slots.", Category.MISC);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		if (args.length == 0) {
			channel.sendMessage(":x: | Você não fez nenhuma aposta.").queue();
			return;
		} else if (!StringUtils.isNumeric(args[0]) || Integer.parseInt(args[0]) < 0) {
			channel.sendMessage(":x: | A aposta deve ser um valor numérico maior que zero.").queue();
			return;
		}

		Account acc = AccountDAO.getAccount(author.getId());
		int bet = Integer.parseInt(args[0]);

		if (acc.getBalance() < bet) {
			channel.sendMessage(":x: | Você não tem créditos suficientes.").queue();
			return;
		}

		channel.sendMessage(":white_flower: | Aposta de " + author.getAsMention() + ": " + args[0] + "\n").queue(s -> {
			for (int i = 0; i < 6; i++) {
				s.editMessage(s.getContentRaw() + rollSlot(i)).queueAfter(2 + (2 * i), TimeUnit.SECONDS);
			}
		});
	}

	private String rollSlot(int phase) {
		for (int i = 0; i < 5; i++) {
			rolled.add(Slots.getSlot());
		}
		switch (phase) {
			case 0:
				return "|" + Slots.SLOT + "|" + Slots.SLOT + "|" + Slots.SLOT + "|" + Slots.SLOT + "|" + Slots.SLOT + "|";
			case 1:
				return "|" + rolled.get(0) + "|" + Slots.SLOT + "|" + Slots.SLOT + "|" + Slots.SLOT + "|" + Slots.SLOT + "|";
			case 2:
				return "|" + rolled.get(0) + "|" + rolled.get(1) + "|" + Slots.SLOT + "|" + Slots.SLOT + "|" + Slots.SLOT + "|";
			case 3:
				return "|" + rolled.get(0) + "|" + rolled.get(1) + "|" + rolled.get(2) + "|" + Slots.SLOT + "|" + Slots.SLOT + "|";
			case 4:
				return "|" + rolled.get(0) + "|" + rolled.get(1) + "|" + rolled.get(2) + "|" + rolled.get(3) + "|" + Slots.SLOT + "|";
			case 5:
				return "|" + rolled.get(0) + "|" + rolled.get(1) + "|" + rolled.get(2) + "|" + rolled.get(3) + "|" + rolled.get(4) + "|";
			default:
				return "";
		}
	}
}

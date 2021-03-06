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

package com.kuuhaku.command.commands.discord.fun;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.controller.postgresql.ExceedDAO;
import com.kuuhaku.controller.postgresql.LeaderboardsDAO;
import com.kuuhaku.controller.postgresql.PStateDAO;
import com.kuuhaku.handlers.games.disboard.model.PoliticalState;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.enums.ExceedEnum;
import com.kuuhaku.model.enums.I18n;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.*;

import java.util.Locale;

@Command(
		name = "pedrapapeltesoura",
		aliases = {"rockpaperscissors", "jankenpon", "rps", "ppt", "jkp"},
		usage = "req_rockpaperscissors",
		category = Category.FUN
)
public class JankenponCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		if (args.length < 1) {
			channel.sendMessage(I18n.getString("err_rock-paper-scissors-invalid-arguments")).queue();
			return;
		}

		Account acc = AccountDAO.getAccount(author.getId());

		int pcOption = Helper.rng(2, false);
		int win = 2;

		switch (args[0].toLowerCase(Locale.ROOT)) {
			case "pedra", ":punch:" -> win = switch (pcOption) {
				case 1 -> 0;
				case 2 -> 1;
				default -> win;
			};
			case "papel", ":raised_back_of_hand:" -> win = switch (pcOption) {
				case 0 -> 1;
				case 2 -> 0;
				default -> win;
			};
			case "tesoura", ":v:" -> win = switch (pcOption) {
				case 0 -> 0;
				case 1 -> 1;
				default -> win;
			};
			default -> {
				channel.sendMessage(I18n.getString("err_rock-paper-scissors-invalid-arguments")).queue();
				return;
			}
		}

		String pcChoice = switch (pcOption) {
			case 0 -> ":punch:";
			case 1 -> ":raised_back_of_hand:";
			case 2 -> ":v:";
			default -> "";
		};

		int finalWin = win;
		channel.sendMessage("Saisho wa guu!\nJan...Ken...Pon! " + pcChoice + (
				switch (finalWin) {
					case 0 -> {
						if (ExceedDAO.hasExceed(author.getId())) {
							PoliticalState ps = com.kuuhaku.controller.postgresql.PStateDAO.getPoliticalState(ExceedEnum.getByName(ExceedDAO.getExceed(author.getId())));
							ps.modifyInfluence(false);
							com.kuuhaku.controller.postgresql.PStateDAO.savePoliticalState(ps);
						}
						int lost = LeaderboardsDAO.getUserScore(author.getId(), JankenponCommand.class);
						if (lost > 0)
							LeaderboardsDAO.submit(author, JankenponCommand.class, -lost);
						yield "\nVocê perdeu!";
					}
					case 1 -> {
						int crd = Math.max(10, Helper.rng(50, false));
						acc.addCredit(crd, this.getClass());
						AccountDAO.saveAccount(acc);
						if (ExceedDAO.hasExceed(author.getId())) {
							PoliticalState ps = com.kuuhaku.controller.postgresql.PStateDAO.getPoliticalState(ExceedEnum.getByName(ExceedDAO.getExceed(author.getId())));
							ps.modifyInfluence(2);
							PStateDAO.savePoliticalState(ps);
						}
						LeaderboardsDAO.submit(author, JankenponCommand.class, 1);
						yield "\nVocê ganhou! Aqui, " + Helper.separate(crd) + " créditos por ter jogado comigo!";
					}
					case 2 -> "\nEmpate!";
					default -> throw new IllegalStateException("Unexpected value: " + finalWin);
				}
		)).queue();
	}
}

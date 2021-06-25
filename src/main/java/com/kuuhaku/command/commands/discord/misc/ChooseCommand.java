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

package com.kuuhaku.command.commands.discord.misc;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.model.annotations.Command;
import net.dv8tion.jda.api.entities.*;

import java.util.Locale;
import java.util.Random;

@Command(
		name = "escolha",
		aliases = {"choose"},
		usage = "req_options",
		category = Category.MISC
)
public class ChooseCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		if (args.length == 0) {
			channel.sendMessage("❌ | Você não me deu nenhuma opção.").queue();
			return;
		} else if (!args[0].contains(";")) {
			channel.sendMessage("❌ | Você precisa me dar ao menos duas opções.").queue();
			return;
		}

		String[] opts = args[0].split(";");
		long seed = 0;

		for (char c : args[0].toLowerCase(Locale.ROOT).toCharArray()) {
			seed += (int) c;
		}

		int choice = new Random(seed).nextInt(opts.length);

		channel.sendMessage(":question: | Eu escolho a opção " + (choice + 1) + ": **" + opts[choice] + "**!").queue();
	}
}

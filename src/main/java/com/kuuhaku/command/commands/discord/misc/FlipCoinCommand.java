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

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Command(
		name = "caracoroa",
		aliases = {"flipcoin", "headstails"},
		category = Category.MISC
)
public class FlipCoinCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		channel.sendMessage("Calculando cara ou coroa....").queue((msg) -> msg.editMessage(((new Random()).nextBoolean() ? "<@" + author.getId() + "> **CARA** :smile:!" : "<@" + author.getId() + "> **COROA** :crown:!")).queueAfter(750, TimeUnit.MILLISECONDS));
	}
}

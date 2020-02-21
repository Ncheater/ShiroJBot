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
import net.dv8tion.jda.api.entities.*;

import java.util.Random;

public class SlotsCommand extends Command {

	public SlotsCommand() {
		super("slots", "<aposta>", "Aposta um quantidade de créditos nos slots.", Category.MISC);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		if (args.length == 0) {
			channel.sendMessage(":x: | Você não fez nenhum pergunta.").queue();
			return;
		}

		String[] res = new String[]{"Sim", "Não", "Provavelmente sim", "Provavelmente não", "Talvez", "Prefiro não responder"};
		long seed = 0;
		String question = String.join(" ", args);

		for (char c : question.toLowerCase().toCharArray()) {
			seed += (int) c;
		}

		channel.sendMessage(":8ball: | " + res[new Random(seed).nextInt(res.length)] + ".").queue();
	}
}

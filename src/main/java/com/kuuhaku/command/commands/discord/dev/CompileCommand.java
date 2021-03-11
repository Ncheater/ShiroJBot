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

package com.kuuhaku.command.commands.discord.dev;

import bsh.Interpreter;
import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.annotations.Requires;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Command(
		name = "compilar",
		aliases = {"compile", "eval", "exec"},
		usage = "req_code",
		category = Category.DEV
)
@Requires({Permission.MESSAGE_EXT_EMOJI})
public class CompileCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		channel.sendMessage("<a:loading:697879726630502401> | Compilando...").queue(m -> {
			try {
				Future<?> execute = Main.getInfo().getCompilationPool().submit(() -> {
					final long start = System.currentTimeMillis();
					try {
						String code = argsAsText.replace("```java", "").replace("```", "");
						Interpreter i = new Interpreter();
						i.set("msg", message);
						i.eval(code);
						Object out = i.get("out");
						m.getChannel().sendMessage("<a:loading:697879726630502401> | Executando...").queue(d ->
								d.editMessage("-> " + out).queue());
						message.delete().queue();
						m.editMessage("✅ | Tempo de execução: " + (System.currentTimeMillis() - start) + " ms").queue();
					} catch (Exception e) {
						m.editMessage("❌ | Erro ao compilar: ```" + e.toString().replace("`", "´") + "```").queue();
					}
					return null;
				});
				try {
					execute.get();
				} catch (InterruptedException | ExecutionException e) {
					Helper.logger(this.getClass()).error(e + " | " + e.getStackTrace()[0]);
				}
			} catch (Exception e) {
				m.editMessage("❌ | Erro ao compilar: ```" + e.toString().replace("`", "´") + "```").queue();
			}
		});
	}


}

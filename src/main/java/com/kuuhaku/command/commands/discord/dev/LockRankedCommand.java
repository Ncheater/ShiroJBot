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

package com.kuuhaku.command.commands.discord.dev;

import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.common.MatchMaking;
import net.dv8tion.jda.api.entities.*;

@Command(
		name = "rlock",
		category = Category.DEV
)
public class LockRankedCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		MatchMaking mm = Main.getInfo().getMatchMaking();
		if (mm.isLocked())
			channel.sendMessage("❌ | A fila ranqueada já está bloqueada (" + mm.getGames().size() + " jogos restantes).").queue();
		else {
			mm.getSoloLobby().clear();
			mm.setLocked(true);
			channel.sendMessage("✅ | Fila ranqueada bloqueada com sucesso até a reinicialização.").queue();
		}
	}
}

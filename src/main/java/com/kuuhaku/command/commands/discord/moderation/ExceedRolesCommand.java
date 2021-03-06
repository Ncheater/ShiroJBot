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

package com.kuuhaku.command.commands.discord.moderation;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.controller.postgresql.GuildDAO;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.annotations.Requires;
import com.kuuhaku.model.persistent.guild.GuildConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

@Command(
		name = "cargosexceed",
		aliases = {"exceedroles", "exroles", "cargosex"},
		category = Category.MODERATION
)
@Requires({Permission.MANAGE_ROLES})
public class ExceedRolesCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		GuildConfig gc = GuildDAO.getGuildById(guild.getId());

		gc.toggleAutoExceedRoles();
		if (gc.isAutoExceedRoles())
			channel.sendMessage("✅ | Cargos de Exceed automáticos ativados, eu irei gerenciar os cargos que possuam `imanity`, `flugel`, `ex-machina`, `werebeast`, `seiren` ou `elf` no nome.").queue();
		else
			channel.sendMessage("✅ | Cargos de Exceed automáticos desativados.").queue();

		GuildDAO.updateGuildSettings(gc);
	}
}

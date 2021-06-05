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

package com.kuuhaku.command.commands.discord.moderation;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.controller.postgresql.GuildDAO;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.persistent.guild.GuildConfig;
import net.dv8tion.jda.api.entities.*;

@Command(
		name = "semguindaste",
		aliases = {"antihoist", "semhoist"},
		category = Category.MODERATION
)
public class AntiHoistCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		GuildConfig gc = GuildDAO.getGuildById(guild.getId());

		gc.toggleAntiHoist();
		if (!gc.isAntiHoist())
			channel.sendMessage("✅ | Nomes que iniciem com caractéres que façam o nome subir na lista serão permitidos.").queue();
		else
			channel.sendMessage("✅ | Nomes que iniciem com caractéres que façam o nome subir na lista serão renomeados.").queue();

		GuildDAO.updateGuildSettings(gc);
	}
}
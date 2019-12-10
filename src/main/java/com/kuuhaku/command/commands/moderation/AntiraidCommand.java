/*
 * This file is part of Shiro J Bot.
 *
 *     Shiro J Bot is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Shiro J Bot is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Shiro J Bot.  If not, see <https://www.gnu.org/licenses/>
 */

package com.kuuhaku.command.commands.moderation;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.SQLite.GuildDAO;
import com.kuuhaku.model.guildConfig;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;

public class AntiraidCommand extends Command {

	public AntiraidCommand() {
		super("semraid", new String[]{"noraid", "antiraid"}, "Expulsa automaticamente novos membros que possuirem contas muito recentes (< 10 min).", Category.MODERACAO);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, Event event, String prefix) {
		guildConfig gc = GuildDAO.getGuildById(guild.getId());

		if (gc.isAntiRaid()) {
			gc.setAntiRaid(false);
			channel.sendMessage("Modo anti-raid está desligado").queue();
		} else {
			gc.setAntiRaid(true);
			channel.sendMessage("Modo anti-raid está ligado, expulsarei novos membros que tiverem uma conta com tempo menor que 10 minutos.").queue();
		}

		GuildDAO.updateGuildSettings(gc);
	}
}

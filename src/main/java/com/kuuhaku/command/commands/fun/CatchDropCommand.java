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

package com.kuuhaku.command.commands.fun;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.model.common.drop.Prize;
import com.kuuhaku.utils.I18n;
import com.kuuhaku.utils.ShiroInfo;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NonNls;

public class CatchDropCommand extends Command {

	public CatchDropCommand(String name, String description, Category category, boolean requiresMM) {
		super(name, description, category, requiresMM);
	}

	public CatchDropCommand(@NonNls String name, @NonNls String[] aliases, String description, Category category, boolean requiresMM) {
		super(name, aliases, description, category, requiresMM);
	}

	public CatchDropCommand(String name, String usage, String description, Category category, boolean requiresMM) {
		super(name, usage, description, category, requiresMM);
	}

	public CatchDropCommand(String name, String[] aliases, String usage, String description, Category category, boolean requiresMM) {
		super(name, aliases, usage, description, category, requiresMM);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		Prize p = ShiroInfo.getCurrentDrop().getIfPresent(guild.getId());

		if (p == null) {
			channel.sendMessage(":x: | Não há nenhum drop para coletar neste servidor.").queue();
			return;
		}

		if (p.getRequirement().getValue().apply(author)) {
			channel.sendMessage(ShiroInfo.getLocale(I18n.PT).getString("err_requirements-not-fulfilled")).queue();
			return;
		}

		ShiroInfo.getCurrentDrop().invalidate(guild.getId());
		p.award(author);

		channel.sendMessage("Você coletou o drop com sucesso!").queue();
	}
}

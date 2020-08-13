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

package com.kuuhaku.command.commands.discord.exceed;

import com.github.ygimenez.method.Pages;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.postgresql.ExceedDAO;
import com.kuuhaku.controller.sqlite.MemberDAO;
import com.kuuhaku.model.persistent.ExceedMember;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NonNls;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExceedLeaveCommand extends Command {

	public ExceedLeaveCommand(String name, String description, Category category, boolean requiresMM) {
		super(name, description, category, requiresMM);
	}

	public ExceedLeaveCommand(@NonNls String name, @NonNls String[] aliases, String description, Category category, boolean requiresMM) {
		super(name, aliases, description, category, requiresMM);
	}

	public ExceedLeaveCommand(String name, String usage, String description, Category category, boolean requiresMM) {
		super(name, usage, description, category, requiresMM);
	}

	public ExceedLeaveCommand(String name, String[] aliases, String usage, String description, Category category, boolean requiresMM) {
		super(name, aliases, usage, description, category, requiresMM);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		ExceedMember em = ExceedDAO.getExceedMember(author.getId());

		if (em == null) {
			channel.sendMessage(":x: | Você não faz parte de nenhum Exceed atualmente.").queue();
			return;
		}

		channel.sendMessage(":warning: | Sair da " + em.getExceed() + " além de reduzir seu XP pela metade fará com que você não possa escolher outro Exceed até o próximo mês. Deseja confirmar sua escolha?").queue(s ->
				Pages.buttonize(s, Map.of(Helper.ACCEPT, (mb, ms) -> {
					if (mb.getId().equals(author.getId())) {
						List<com.kuuhaku.model.persistent.Member> mbs = MemberDAO.getMemberByMid(mb.getId());
						mbs.forEach(m -> {
							m.halfXpKeepLevel();
							com.kuuhaku.controller.postgresql.MemberDAO.saveMemberToBD(m);
							MemberDAO.updateMemberConfigs(m);
						});
						em.setBlocked(true);
						em.setExceed("");
						ExceedDAO.joinExceed(em);
						ms.delete().queue(null, Helper::doNothing);
					}
				}), true, 1, TimeUnit.MINUTES, u -> u.getId().equals(author.getId()))
		);
	}
}
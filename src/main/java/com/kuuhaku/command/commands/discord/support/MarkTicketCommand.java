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

package com.kuuhaku.command.commands.discord.support;

import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.postgresql.TicketDAO;
import com.kuuhaku.model.persistent.Ticket;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.I18n;
import com.kuuhaku.utils.ShiroInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MarkTicketCommand extends Command {

	public MarkTicketCommand(String name, String description, Category category, boolean requiresMM) {
		super(name, description, category, requiresMM);
	}

	public MarkTicketCommand(String name, String[] aliases, String description, Category category, boolean requiresMM) {
		super(name, aliases, description, category, requiresMM);
	}

	public MarkTicketCommand(String name, String usage, String description, Category category, boolean requiresMM) {
		super(name, usage, description, category, requiresMM);
	}

	public MarkTicketCommand(@NonNls String name, @NonNls String[] aliases, String usage, String description, Category category, boolean requiresMM) {
		super(name, aliases, usage, description, category, requiresMM);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		if (args.length < 1) {
			channel.sendMessage(ShiroInfo.getLocale(I18n.PT).getString("err_no-ticket-id")).queue();
			return;
		} else if (!StringUtils.isNumeric(args[0])) {
			channel.sendMessage(ShiroInfo.getLocale(I18n.PT).getString("err_invalid-ticket-id")).queue();
			return;
		}

		Ticket t = TicketDAO.getTicket(Integer.parseInt(args[0]));

		if (t == null) {
			channel.sendMessage(ShiroInfo.getLocale(I18n.PT).getString("err_invalid-ticket")).queue();
			return;
		} else if (t.isSolved()) {
			channel.sendMessage(ShiroInfo.getLocale(I18n.PT).getString("err_ticket-already-solved")).queue();
			return;
		}

		EmbedBuilder eb = new EmbedBuilder();

		eb.setTitle("Resolução de ticket Nº " + args[0]);
		eb.setDescription("Assunto:```" + t.getSubject() + "```");
		if (Helper.getOr(t.getRequestedBy(), null) != null)
			eb.addField("Aberto por:", Main.getInfo().getUserByID(t.getRequestedBy()).getAsTag(), true);
		eb.addField("Resolvido por:", author.getAsTag(), true);
		eb.addField("Fechado em:", Helper.dateformat.format(LocalDateTime.now().atZone(ZoneId.of("GMT-3"))), true);
		eb.setColor(Color.green);

		Main.getInfo().getDevelopers().forEach(dev -> {
			Message msg = Main.getInfo().getUserByID(dev).openPrivateChannel()
					.flatMap(m -> m.sendMessage(eb.build()))
					.complete();
			msg.getChannel().retrieveMessageById(String.valueOf(t.getMsgIds().get(dev)))
					.flatMap(Message::delete)
					.queue();
			t.solved();
				}
		);

		TicketDAO.updateTicket(t);
		channel.sendMessage(ShiroInfo.getLocale(I18n.PT).getString("str_successfully-solved-ticket")).queue();
	}
}
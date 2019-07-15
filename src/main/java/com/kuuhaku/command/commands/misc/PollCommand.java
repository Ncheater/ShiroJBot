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

package com.kuuhaku.command.commands.misc;

import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.SQLite;
import com.kuuhaku.model.guildConfig;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PollCommand extends Command {

	public PollCommand() {
		super("enquete", new String[]{"poll"}, "<pergunta>", "Inicia uma enquete no canal atual ou no configurado pelos moderadores", Category.MISC);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, Event event, String prefix) {
		if (args.length < 1) {
			channel.sendMessage(":x: | Você precisa digitar uma pergunta para a enquete.").queue();
			return;
		} else if (String.join(" ", args).length() < 10) {
			channel.sendMessage(":x: | Pergunta muito curta, tente complementá-la mais!").queue();
			return;
		} else if (String.join(" ", args).length() > 2000) {
			channel.sendMessage(":x: | Pergunta muito longa, tente simplificá-la mais!").queue();
			return;
		}

		guildConfig gc = SQLite.getGuildById(guild.getId());

		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(":notepad_spiral: Enquete criada por " + member.getEffectiveName());
		eb.setThumbnail("https://www.kalkoken.org/apps/easypoll/resources/poll-logo.png");
		eb.setDescription(String.join(" ", args));
		eb.setFooter("Clique nas reações abaixo para votar", null);
		eb.setColor(Color.decode("#2195f2"));

		if (gc.getCanalSUG() == null || gc.getCanalSUG().isEmpty()) {
			SQLite.updateGuildCanalSUG("", gc);
			channel.sendMessage(eb.build()).queue(m -> {
				m.addReaction("\uD83D\uDC4D").queue();
				m.addReaction("\uD83D\uDC4E").queue();
				final Runnable awaitPollEnd = () -> {
					int pos = Collections.frequency(m.getReactions().stream().map(r -> r.getReactionEmote().getEmote()).collect(Collectors.toList()), "\uD83D\uDC4D") - 1;
					int neg = Collections.frequency(m.getReactions().stream().map(r -> r.getReactionEmote().getEmote()).collect(Collectors.toList()), "\uD83D\uDC4E") - 1;
					boolean NOVOTE = false;

					if (pos == -1 && neg == -1) {
						pos = 1;
						neg = 1;
						NOVOTE = true;
					}

					eb.setAuthor("A enquete feita por " + member.getEffectiveName() + " foi encerrada!");
					eb.setTitle("Enquete: ("+ (NOVOTE ? "nenhum voto" : (pos + neg) + " votos") +")");
					eb.addField("Aprovação: ", Helper.round((((float) pos * 100f) / ((float) pos + (float) neg)), 1) + "%", true);
					eb.addField("Reprovação: ", Helper.round((((float) neg * 100f) / ((float) pos + (float) neg)), 1) + "%", true);

					m.editMessage(eb.build()).submit();
					author.openPrivateChannel().queue(c -> c.sendMessage(eb.setAuthor("Sua enquete foi encerrada!").build()).submit());
					m.clearReactions().complete();
				};
				Main.getInfo().getScheduler().schedule(awaitPollEnd, gc.getPollTime(), TimeUnit.SECONDS);
			});
		} else {
			try {

				guild.getTextChannelById(gc.getCanalSUG()).sendMessage(eb.build()).queue(m -> {
					m.addReaction("\uD83D\uDC4D").queue();
					m.addReaction("\uD83D\uDC4E").queue();
					final Runnable awaitPollEnd = () -> {
						int pos = Collections.frequency(m.getReactions().stream().map(r -> r.getReactionEmote().getEmote()).collect(Collectors.toList()), "\uD83D\uDC4D") - 1;
						int neg = Collections.frequency(m.getReactions().stream().map(r -> r.getReactionEmote().getEmote()).collect(Collectors.toList()), "\uD83D\uDC4E") - 1;
						boolean NOVOTE = false;

						if (pos == -1 && neg == -1) {
							pos = 1;
							neg = 1;
							NOVOTE = true;
						}

						eb.setAuthor("A enquete feita por " + member.getEffectiveName() + " foi encerrada!");
						eb.setTitle("Enquete: ("+ (NOVOTE ? "nenhum voto" : (pos + neg) + " votos") +")");
						eb.addField("Aprovação: ", Helper.round((((float) pos * 100f) / ((float) pos + (float) neg)), 1) + "%", true);
						eb.addField("Reprovação: ", Helper.round((((float) neg * 100f) / ((float) pos + (float) neg)), 1) + "%", true);

						m.editMessage(eb.build()).submit();
						author.openPrivateChannel().queue(c -> c.sendMessage(eb.setAuthor("Sua enquete foi encerrada!").build()).submit());
						m.clearReactions().complete();
					};
					Main.getInfo().getScheduler().schedule(awaitPollEnd, gc.getPollTime(), TimeUnit.SECONDS);
				});
			} catch (Exception e) {
				SQLite.updateGuildCanalSUG("", gc);
				channel.sendMessage(eb.build()).queue(m -> {
					m.addReaction("\uD83D\uDC4D").queue();
					m.addReaction("\uD83D\uDC4E").queue();
					final Runnable awaitPollEnd = () -> {
						int pos = Collections.frequency(m.getReactions().stream().map(r -> r.getReactionEmote().getEmote()).collect(Collectors.toList()), "\uD83D\uDC4D") - 1;
						int neg = Collections.frequency(m.getReactions().stream().map(r -> r.getReactionEmote().getEmote()).collect(Collectors.toList()), "\uD83D\uDC4E") - 1;
						boolean NOVOTE = false;

						if (pos == -1 && neg == -1) {
							pos = 1;
							neg = 1;
							NOVOTE = true;
						}

						eb.setAuthor("A enquete feita por " + member.getEffectiveName() + " foi encerrada!");
						eb.setTitle("Enquete: ("+ (NOVOTE ? "nenhum voto" : (pos + neg) + " votos") +")");
						eb.addField("Aprovação: ", Helper.round((((float) pos * 100f) / ((float) pos + (float) neg)), 1) + "%", true);
						eb.addField("Reprovação: ", Helper.round((((float) neg * 100f) / ((float) pos + (float) neg)), 1) + "%", true);

						m.editMessage(eb.build()).submit();
						author.openPrivateChannel().queue(c -> c.sendMessage(eb.setAuthor("Sua enquete foi encerrada!").build()).submit());
						m.clearReactions().complete();
					};
					Main.getInfo().getScheduler().schedule(awaitPollEnd, gc.getPollTime(), TimeUnit.SECONDS);
				});
			}
		}

		channel.sendMessage("Enquete criada com sucesso, ela encerrará automaticamente em " + gc.getPollTime() + " segundos.").queue();
	}
}

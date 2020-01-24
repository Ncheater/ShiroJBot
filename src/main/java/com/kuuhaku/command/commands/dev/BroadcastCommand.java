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

package com.kuuhaku.command.commands.dev;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.mysql.TagDAO;
import com.kuuhaku.model.Tags;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BroadcastCommand extends Command {

	public BroadcastCommand(String name, String description, Category category) {
		super(name, description, category);
	}

	public BroadcastCommand(String name, String[] aliases, String description, Category category) {
		super(name, aliases, description, category);
	}

	public BroadcastCommand(String name, String usage, String description, Category category) {
		super(name, usage, description, category);
	}

	public BroadcastCommand(String name, String[] aliases, String usage, String description, Category category) {
		super(name, aliases, usage, description, category);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		if (args.length < 1) {
			channel.sendMessage(":x: | É necessário informar um tipo de broadcast (geral/parceiros).").queue();
			return;
		} else if (args.length < 2) {
			channel.sendMessage(":x: | É necessário informar uma mensagem para enviar.").queue();
			return;
		}

		String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		Map<String, Boolean> result = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		List<Page> pages = new ArrayList<>();
		EmbedBuilder eb = new EmbedBuilder();

		switch (args[0].toLowerCase()) {
			case "geral":
				List<Guild> gcs = Main.getInfo().getAPI().getGuilds();
				List<List<Guild>> gcPages = Helper.chunkify(gcs, 10);

				for (List<Guild> gs : gcPages) {
					result.clear();
					eb.clear();
					sb.setLength(0);

					for (Guild g : gs) {
						try {
							result.put(g.getName(), false);
							for (TextChannel c : g.getTextChannels()) {
								if (c.canTalk()) {
									c.sendMessage(msg).submit().get();
									result.put(g.getName(), true);
									break;
								}
							}
						} catch (Exception e) {
							result.put(g.getName(), false);
						}
					}

					showResult(result, sb, pages, eb);
				}

				channel.sendMessage((MessageEmbed) pages.get(0).getContent()).queue(s -> Pages.paginate(Main.getInfo().getAPI(), s, pages, 60, TimeUnit.SECONDS));
				break;
			case "parceiros":
				List<Tags> ps = TagDAO.getAllPartners();
				List<List<Tags>> psPages = Helper.chunkify(ps, 10);

				for (List<Tags> p : psPages) {
					result.clear();
					eb.clear();
					sb.setLength(0);

					for (Tags t : p) {
						User u = Helper.getOr(Main.getInfo().getUserByID(t.getId()), null);

						if (u == null) {
							result.put("Desconhecido (" + t.getId() + ")", false);
						} else {
							try {
								u.openPrivateChannel().complete().sendMessage(msg).complete();
								result.put(u.getAsTag(), true);
							} catch (ErrorResponseException e) {
								result.put(u.getAsTag(), false);
							}
						}
					}

					showResult(result, sb, pages, eb);
				}

				channel.sendMessage((MessageEmbed) pages.get(0).getContent()).queue(s -> Pages.paginate(Main.getInfo().getAPI(), s, pages, 60, TimeUnit.SECONDS));
				break;
			default:
				channel.sendMessage(":x: | Tipo desconhecido, os tipos válidos são **geral** ou **parceiros**").queue();
		}
	}

	private void showResult(Map<String, Boolean> result, StringBuilder sb, List<Page> pages, EmbedBuilder eb) {
		sb.append("```diff\n");
		result.forEach((key, value) -> sb.append(value ? "+ " : "- ").append(key).append("\n"));
		sb.append("```");

		eb.setTitle("__**STATUS**__ ");
		eb.setDescription(sb.toString());
		pages.add(new Page(PageType.EMBED, eb.build()));
	}
}

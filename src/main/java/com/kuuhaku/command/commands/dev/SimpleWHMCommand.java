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

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NonNls;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class SimpleWHMCommand extends Command {

	public SimpleWHMCommand(String name, String description, Category category) {
		super(name, description, category);
	}

	public SimpleWHMCommand(String name, String[] aliases, String description, Category category) {
		super(name, aliases, description, category);
	}

	public SimpleWHMCommand(@NonNls String name, String usage, String description, Category category) {
		super(name, usage, description, category);
	}

	public SimpleWHMCommand(String name, String[] aliases, String usage, String description, Category category) {
		super(name, aliases, usage, description, category);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		Webhook wh = Helper.getOrCreateWebhook((TextChannel) channel, "Webhook Test", Main.getInfo().getAPI());
		Map<String, Consumer<Void>> s = Helper.sendEmotifiedString(guild, String.join(" ", args));

		WebhookMessageBuilder wmb = new WebhookMessageBuilder();
		wmb.setContent(String.valueOf(s.keySet().toArray()[0]));
		wmb.setAvatarUrl(author.getAvatarUrl());
		wmb.setUsername(author.getName());

		assert wh != null;
		WebhookClient wc = new WebhookClientBuilder(wh.getUrl()).build();
		try {
			wc.send(wmb.build()).thenAccept(rm -> s.get(String.valueOf(s.keySet().toArray()[0])).accept(null)).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}

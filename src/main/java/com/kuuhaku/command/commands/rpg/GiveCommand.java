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

package com.kuuhaku.command.commands.rpg;

import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.handlers.games.rpg.Utils;
import com.kuuhaku.handlers.games.rpg.actors.Actor;
import com.kuuhaku.handlers.games.rpg.entities.Item;
import com.kuuhaku.handlers.games.rpg.enums.Resource;
import com.kuuhaku.handlers.games.rpg.exceptions.UnknownItemException;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;

public class GiveCommand extends Command {

	public GiveCommand(String name, String description, Category category) {
		super(name, description, category);
	}

	public GiveCommand(String name, String[] aliases, String description, Category category) {
		super(name, aliases, description, category);
	}

	public GiveCommand(String name, String usage, String description, Category category) {
		super(name, usage, description, category);
	}

	public GiveCommand(@NonNls String name, @NonNls String[] aliases, String usage, String description, Category category) {
		super(name, aliases, usage, description, category);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		if (Utils.noPlayerAlert(args, message, channel)) return;

		Actor.Player t = Main.getInfo().getGames().get(guild.getId()).getPlayers().getOrDefault(message.getMentionedUsers().get(0).getId(), null);

		if (t == null) {
			channel.sendMessage(":x: | O alvo deve ser um jogador participante da campanha atual.").queue();
			return;
		}

		if (Main.getInfo().getGames().get(guild.getId()).getMaster().equals(author.getId())) {
			if (args.length < 3 && (Helper.containsAny(args[1], ArrayUtils.addAll(Resource.MONEY.getAliases(), Resource.XP.getAliases())))) {
				channel.sendMessage(":x: | Você precisa especificar a quantidade de XP.").queue();
				return;
			}

			if (Helper.containsAny(args[1], Resource.MONEY.getAliases())) {
				t.getCharacter().getInventory().addGold(Integer.parseInt(args[2]));
				channel.sendMessage("_**" + t.getCharacter().getName() + " recebeu $" + args[2] + " moeda" + (Integer.parseInt(args[2]) != 1 ? "s" : "") + ".**_").queue();
			} else if (Helper.containsAny(args[1], Resource.XP.getAliases())) {
				t.getCharacter().getStatus().addXp(Integer.parseInt(args[2]));
				channel.sendMessage("_**" + t.getCharacter().getName() + " recebeu " + args[2] + " ponto" + (Integer.parseInt(args[2]) != 1 ? "s" : "") + " de experiência.**_").queue();
			} else {
				t.getCharacter().getInventory().addItem(Main.getInfo().getGames().get(guild.getId()).getItem(String.join(" ", Arrays.copyOfRange(args, 1, args.length))));
			}
			return;
		}

		Actor.Player p = Main.getInfo().getGames().get(guild.getId()).getPlayers().get(author.getId());
		if (Helper.containsAny(args[1], Resource.MONEY.getAliases())) {
			if (p.getCharacter().getInventory().getGold() < Integer.parseInt(args[1])) {
				channel.sendMessage(":x: | Você não possui essa quantia de ouro.").queue();
				return;
			}

			p.getCharacter().getInventory().addGold(-Integer.parseInt(args[2]));
			t.getCharacter().getInventory().addGold(Integer.parseInt(args[2]));
			channel.sendMessage("_**" + p.getCharacter().getName() + " deu $" + args[2] + " moeda" + (Integer.parseInt(args[2]) != 1 ? "s" : "") + " para " + t.getCharacter().getName() + "!**_").queue();
		} else {
			try {
				Item i = p.getCharacter().getInventory().getItem(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
				p.getCharacter().getInventory().removeItem(i);
				t.getCharacter().getInventory().addItem(i);
				channel.sendMessage("_**" + p.getCharacter().getName() + " deu o item " + i.getName() + " para " + t.getCharacter().getName() + "!**_").queue();
			} catch (UnknownItemException e) {
				channel.sendMessage(":x: | Você não possui este item.").queue();
			}
		}
	}
}

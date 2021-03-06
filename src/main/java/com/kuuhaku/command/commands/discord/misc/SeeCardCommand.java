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

package com.kuuhaku.command.commands.discord.misc;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.controller.postgresql.CardDAO;
import com.kuuhaku.controller.postgresql.KawaiponDAO;
import com.kuuhaku.controller.postgresql.RarityColorsDAO;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Champion;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Equipment;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Field;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.enums.Charm;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.interfaces.Drawable;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.annotations.Requires;
import com.kuuhaku.model.common.ColorlessEmbedBuilder;
import com.kuuhaku.model.enums.KawaiponRarity;
import com.kuuhaku.model.persistent.*;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.collections4.ListUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@Command(
		name = "carta",
		aliases = {"card", "see", "olhar"},
		usage = "req_card-type",
		category = Category.MISC
)
@Requires({Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES})
public class SeeCardCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		if (args.length < 1) {
			channel.sendMessage("❌ | Você precisa informar uma carta.").queue();
			return;
		}

		Account acc = AccountDAO.getAccount(author.getId());
		Kawaipon kp = KawaiponDAO.getKawaipon(author.getId());
		boolean shoukan = args.length > 1 && args[1].equalsIgnoreCase("S");

		if (shoukan) {
			Champion ch = CardDAO.peekChampion(args[0]);
			Equipment eq = CardDAO.getEquipment(args[0]);
			Field f = CardDAO.getField(args[0]);

			if (ch == null && eq == null && f == null) {
				channel.sendMessage("❌ | Esse campeão, equipamento ou campo não existe, você não quis dizer `" + Helper.didYouMean(args[0], ListUtils.union(ListUtils.union(CardDAO.getAllChampionNames(), CardDAO.getAllEquipmentNames()), CardDAO.getAllFieldNames()).toArray(String[]::new)) + "`?").queue();
				return;
			}

			Drawable d = ch == null ? eq == null ? f : eq : ch;
			d.setAcc(acc);

			EmbedBuilder eb = new ColorlessEmbedBuilder();

			eb.setTitle((ch == null ? ":shield:" : ":crossed_swords:") + " | " + d.getCard().getName());
			if (d instanceof Champion c) {
				eb.addField("Classe:", c.getCategory() == null ? "Nenhuma" : c.getCategory().getName(), true);
			} else if (d instanceof Equipment e && e.getCharm() != null) {
				Charm c = e.getCharm();
				eb.addField("Amuleto:", c == Charm.SPELL && e.isParasite() ? "Parasita" : c.getName(), true);
			}
			eb.setImage("attachment://kawaipon.png");

			channel.sendMessageEmbeds(eb.build()).addFile(Helper.writeAndGet(d.drawCard(false), "s_" + d.getCard().getId(), "png"), "kawaipon.png").queue();
		} else {
			Card tc = CardDAO.getCard(args[0], true);
			if (tc == null) {
				channel.sendMessage("❌ | Essa carta não existe, você não quis dizer `" + Helper.didYouMean(args[0], CardDAO.getAllCardNames().toArray(String[]::new)) + "`?").queue();
				return;
			}

			boolean foil = args.length > 1 && tc.getRarity() != KawaiponRarity.ULTIMATE && args[1].equalsIgnoreCase("C");
			KawaiponCard card = new KawaiponCard(tc, foil);

			Set<KawaiponCard> cards = kp.getCards();
			Set<AddedAnime> animes = CardDAO.getValidAnime();
			for (AddedAnime anime : animes) {
				if (CardDAO.hasCompleted(author.getId(), anime.getName(), false))
					cards.add(new KawaiponCard(CardDAO.getUltimate(anime.getName()), false));
			}

			Champion c = CardDAO.peekChampion(card.getCard());
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle((foil ? ":star2:" : ":flower_playing_cards:") + " | " + card.getName())
					.setColor(RarityColorsDAO.getColor(tc.getRarity()).getPrimary())
					.addField("Obtida:", cards.contains(card) ? "Sim" : "Não", true)
					.addField("Elegível:", c != null && !c.isFusion() ? (Helper.getOr(c.getRawEffect(), "").contains("//TODO") ? "Ainda não" : "Sim") : "Não", true)
					.addField("Raridade:", tc.getRarity().toString(), true)
					.addField("Tipo:", tc.getRarity() == KawaiponRarity.ULTIMATE ? "Única" : (card.isFoil() ? "Cromada" : "Normal"), true)
					.addField("Anime:", tc.getAnime().toString(), true)
					.setImage("attachment://kawaipon." + (cards.contains(card) ? "png" : "jpg"));

			try {
				BufferedImage bi = (ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("kawaipon/missing.jpg"))));

				if (cards.contains(card))
					channel.sendMessageEmbeds(eb.build()).addFile(Helper.writeAndGet(tc.drawCard(foil), "kp_" + tc.getId(), "png"), "kawaipon.png").queue();
				else
					channel.sendMessageEmbeds(eb.build()).addFile(Helper.writeAndGet(bi, "unknown", "jpg"), "kawaipon.jpg").queue();
			} catch (IOException e) {
				channel.sendMessage("❌ | Deu um pequeno erro aqui na hora de mostrar a carta, logo logo um dos meus desenvolvedores irá corrigi-lo!").queue();
				Helper.logger(this.getClass()).error(e + " | " + e.getStackTrace()[0]);
			}
		}
	}
}
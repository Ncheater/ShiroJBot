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

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.ThrowingBiConsumer;
import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.controller.postgresql.CardDAO;
import com.kuuhaku.controller.postgresql.KawaiponDAO;
import com.kuuhaku.controller.postgresql.MarketDAO;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Equipment;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Field;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.annotations.Requires;
import com.kuuhaku.model.common.ColorlessEmbedBuilder;
import com.kuuhaku.model.enums.CardType;
import com.kuuhaku.model.persistent.*;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Command(
		name = "anunciar",
		aliases = {"sell", "vender"},
		usage = "req_card-price",
		category = Category.MISC
)
@Requires({Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION})
public class SellCardCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		Kawaipon kp = KawaiponDAO.getKawaipon(author.getId());
		Deck dk = kp.getDeck();

		if (Main.getInfo().getConfirmationPending().get(author.getId()) != null) {
			channel.sendMessage("❌ | Você possui um comando com confirmação pendente, por favor resolva-o antes de usar este comando novamente.").queue();
			return;
		} else if (args.length < 2) {
			channel.sendMessage("❌ | Você precisa informar uma carta e o preço dela.").queue();
			return;
		} else if (!StringUtils.isNumeric(args[1])) {
			channel.sendMessage("❌ | O preço precisa ser um valor inteiro.").queue();
			return;
		}

		String name = args[0].toUpperCase(Locale.ROOT);
		EnumSet<CardType> matches = EnumSet.noneOf(CardType.class);
		kp.getCards().stream()
				.filter(kc -> kc.getCard().getId().equals(name))
				.findFirst()
				.ifPresent(kc -> matches.add(CardType.KAWAIPON));
		dk.getEquipments().stream()
				.filter(e -> e.getCard().getId().equals(name))
				.findFirst()
				.ifPresent(e -> matches.add(CardType.EVOGEAR));
		dk.getFields().stream()
				.filter(f -> f.getCard().getId().equals(name))
				.findFirst()
				.ifPresent(f -> matches.add(CardType.FIELD));

		CompletableFuture<Triple<Card, CardType, Boolean>> chosen = new CompletableFuture<>();
		if (matches.size() > 1) {
			EmbedBuilder eb = new ColorlessEmbedBuilder()
					.setTitle("Por favor escolha uma")
					.setDescription(
							(matches.contains(CardType.KAWAIPON) ? (Helper.getRegionalIndicator(10) + " -> Kawaipon\n") : "") +
							(matches.contains(CardType.EVOGEAR) ? (Helper.getRegionalIndicator(4) + " -> Evogear\n") : "") +
							(matches.contains(CardType.FIELD) ? (Helper.getRegionalIndicator(2) + " -> Campo\n") : "")
					);

			Map<String, ThrowingBiConsumer<Member, Message>> btns = new LinkedHashMap<>();
			if (matches.contains(CardType.KAWAIPON)) {
				btns.put(Helper.getRegionalIndicator(10), (mb, ms) -> {
					chooseVersion(author, channel, kp, name, chosen);
					ms.delete().queue(null, Helper::doNothing);
				});
			}
			if (matches.contains(CardType.EVOGEAR)) {
				btns.put(Helper.getRegionalIndicator(4), (mb, ms) -> {
					chosen.complete(Triple.of(CardDAO.getRawCard(name), CardType.EVOGEAR, false));
					ms.delete().queue(null, Helper::doNothing);
				});
			}
			if (matches.contains(CardType.FIELD)) {
				btns.put(Helper.getRegionalIndicator(2), (mb, ms) -> {
					chosen.complete(Triple.of(CardDAO.getRawCard(name), CardType.FIELD, false));
					ms.delete().queue(null, Helper::doNothing);
				});
			}

			Main.getInfo().getConfirmationPending().put(author.getId(), true);
			channel.sendMessageEmbeds(eb.build())
					.queue(s -> Pages.buttonize(s, btns, true,
							1, TimeUnit.MINUTES,
							u -> u.getId().equals(author.getId()),
							ms -> {
								Main.getInfo().getConfirmationPending().remove(author.getId());
								chosen.complete(null);
							}
					));
		} else if (matches.isEmpty()) {
			channel.sendMessage("❌ | Você não pode vender uma carta que não possui!").queue();
			return;
		} else {
			CardType type = matches.stream().findFirst().orElse(CardType.NONE);
			switch (type) {
				case KAWAIPON -> chooseVersion(author, channel, kp, name, chosen);
				case EVOGEAR, FIELD -> chosen.complete(Triple.of(CardDAO.getRawCard(name), type, false));
				case NONE -> chosen.complete(null);
			}
		}

		try {
			Triple<Card, CardType, Boolean> off = chosen.get();
			if (off == null) {
				channel.sendMessage("Venda cancelada.").queue();
				return;
			}

			boolean hasLoan = AccountDAO.getAccount(kp.getUid()).getLoan() > 0;
			int price = Integer.parseInt(args[1]);
			int min = switch (off.getLeft().getRarity()) {
				case EQUIPMENT -> hasLoan ? Helper.BASE_EQUIPMENT_PRICE * 2 : Helper.BASE_EQUIPMENT_PRICE / 2;
				case FIELD -> hasLoan ? Helper.BASE_FIELD_PRICE * 2 : Helper.BASE_FIELD_PRICE / 2;
				default -> off.getLeft().getRarity().getIndex() * (hasLoan ? Helper.BASE_CARD_PRICE * 2 : Helper.BASE_CARD_PRICE / 2) * (off.getRight() ? 2 : 1);
			};

			if (price < min) {
				if (hasLoan)
					channel.sendMessage("❌ | Como você possui uma dívida ativa, você não pode vender essa carta por menos que " + Helper.separate(min) + " créditos.").queue();
				else
					channel.sendMessage("❌ | Você não pode vender essa carta por menos que " + Helper.separate(min) + " créditos.").queue();
				return;
			}

			String msg = switch (off.getLeft().getRarity()) {
				case EQUIPMENT -> "Este equipamento sairá do seu deck, você ainda poderá comprá-lo novamente pelo mesmo preço. Deseja mesmo anunciá-lo?";
				case FIELD -> "Este campo sairá do seu deck, você ainda poderá comprá-lo novamente pelo mesmo preço. Deseja mesmo anunciá-lo?";
				default -> "Esta carta sairá da sua coleção, você ainda poderá comprá-la novamente pelo mesmo preço. Deseja mesmo anunciá-la?";
			};

			Main.getInfo().getConfirmationPending().put(author.getId(), true);
			channel.sendMessage(msg)
					.queue(s -> Pages.buttonize(s, Map.of(Helper.ACCEPT, (mb, ms) -> {
								Main.getInfo().getConfirmationPending().remove(author.getId());
								Kawaipon finalKp = KawaiponDAO.getKawaipon(author.getId());
								Deck fDk = finalKp.getDeck();

								Market m = switch (off.getMiddle()) {
									case EVOGEAR -> {
										Equipment e = CardDAO.getEquipment(off.getLeft());
										fDk.removeEquipment(e);
										assert e != null;
										yield new Market(author.getId(), e, price);
									}
									case FIELD -> {
										Field f = CardDAO.getField(off.getLeft());
										fDk.removeField(f);
										assert f != null;
										yield new Market(author.getId(), f, price);
									}
									default -> {
										KawaiponCard kc = new KawaiponCard(off.getLeft(), off.getRight());
										finalKp.removeCard(kc);
										yield new Market(author.getId(), kc, price);
									}
								};

								MarketDAO.saveCard(m);
								KawaiponDAO.saveKawaipon(finalKp);

								s.delete().flatMap(d -> channel.sendMessage("✅ | Carta anunciada com sucesso!")).queue();
							}), true, 1, TimeUnit.MINUTES,
							u -> u.getId().equals(author.getId()),
							ms -> Main.getInfo().getConfirmationPending().remove(author.getId())
					));
		} catch (InterruptedException | ExecutionException e) {
			Helper.logger(this.getClass()).error(e + " | " + e.getStackTrace()[0]);
		} catch (NumberFormatException e) {
			channel.sendMessage("❌ | O valor máximo é " + Helper.separate(Integer.MAX_VALUE) + " créditos!").queue();
		}
	}

	private void chooseVersion(User author, TextChannel channel, Kawaipon kp, String name, CompletableFuture<Triple<Card, CardType, Boolean>> chosen) {
		List<KawaiponCard> kcs = kp.getCards().stream()
				.filter(kc -> kc.getCard().getId().equals(name))
				.sorted(Comparator.comparing(KawaiponCard::isFoil))
				.collect(Collectors.toList());

		if (kcs.size() > 1) {
			Main.getInfo().getConfirmationPending().put(author.getId(), true);
			channel.sendMessage("Foram encontradas 2 versões dessa carta (normal e cromada). Por favor selecione **:one: para normal** ou **:two: para cromada**.")
					.queue(s -> Pages.buttonize(s, new LinkedHashMap<>() {{
								put(Helper.getNumericEmoji(1), (mb, ms) -> {
									chosen.complete(Triple.of(kcs.get(0).getCard(), CardType.KAWAIPON, false));
									ms.delete().queue(null, Helper::doNothing);
								});
								put(Helper.getNumericEmoji(2), (mb, ms) -> {
									chosen.complete(Triple.of(kcs.get(1).getCard(), CardType.KAWAIPON, true));
									ms.delete().queue(null, Helper::doNothing);
								});
							}}, true, 1, TimeUnit.MINUTES,
							u -> u.getId().equals(author.getId()),
							ms -> {
								Main.getInfo().getConfirmationPending().remove(author.getId());
								chosen.complete(null);
							}
					));
		} else {
			chosen.complete(Triple.of(kcs.get(0).getCard(), CardType.KAWAIPON, kcs.get(0).isFoil()));
		}
	}
}
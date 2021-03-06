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
import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.controller.postgresql.CardDAO;
import com.kuuhaku.controller.postgresql.KawaiponDAO;
import com.kuuhaku.events.SimpleMessageListener;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Equipment;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Field;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.annotations.Requires;
import com.kuuhaku.model.persistent.*;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.ShiroInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Command(
		name = "leilao",
		aliases = {"auction", "auct"},
		usage = "req_card-price",
		category = Category.MISC
)
@Requires({Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION})
public class AuctionCommand implements Executable {

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		if (args.length < 3) {
			channel.sendMessage("❌ | Você precisa informar a carta, o tipo dela e o valor inicial para fazer um leilão.").queue();
			return;
		} else if (!StringUtils.isNumeric(args[2])) {
			channel.sendMessage("❌ | O preço precisa ser um valor inteiro.").queue();
			return;
		} else if (Main.getInfo().getConfirmationPending().get(author.getId()) != null) {
			channel.sendMessage("❌ | Você possui um comando com confirmação pendente, por favor resolva-o antes de usar este comando novamente.").queue();
			return;
		}

		int type = switch (args[1].toUpperCase(Locale.ROOT)) {
			case "N", "C" -> 1;
			case "E" -> 2;
			case "F" -> 3;
			default -> -1;
		};

		if (type == -1) {
			channel.sendMessage("❌ | Você precisa informar o tipo da carta que deseja leiloar (`N` = normal, `C` = cromada, `E` = evogear, `F` = campo).").queue();
			return;
		}

		Kawaipon kp = KawaiponDAO.getKawaipon(author.getId());
		Deck dk = kp.getDeck();
		Object obj;
		boolean foil = args[1].equalsIgnoreCase("C");
		switch (type) {
			case 1 -> {
				Card c = CardDAO.getCard(args[0], false);

				if (c == null) {
					channel.sendMessage("❌ | Essa carta não existe, você não quis dizer `" + Helper.didYouMean(args[0], CardDAO.getAllCardNames().toArray(String[]::new)) + "`?").queue();
					return;
				}

				KawaiponCard card = kp.getCard(c, foil);

				if (card == null) {
					channel.sendMessage("❌ | Você não pode leiloar uma carta que não possui!").queue();
					return;
				}

				obj = card;
			}
			case 2 -> {
				Equipment c = CardDAO.getEquipment(args[0]);

				if (c == null) {
					channel.sendMessage("❌ | Esse equipamento não existe, você não quis dizer `" + Helper.didYouMean(args[0], CardDAO.getAllEquipmentNames().toArray(String[]::new)) + "`?").queue();
					return;
				} else if (!dk.getEquipments().contains(c)) {
					channel.sendMessage("❌ | Você não pode leiloar um equipamento que não possui!").queue();
					return;
				}

				obj = c;
			}
			default -> {
				Field f = CardDAO.getField(args[0]);

				if (f == null) {
					channel.sendMessage("❌ | Essa arena não existe, você não quis dizer `" + Helper.didYouMean(args[0], CardDAO.getAllFieldNames().toArray(String[]::new)) + "`?").queue();
					return;
				} else if (!dk.getFields().contains(f)) {
					channel.sendMessage("❌ | Você não pode leiloar uma arena que não possui!").queue();
					return;
				}

				obj = f;
			}
		}

		try {
			boolean hasLoan = AccountDAO.getAccount(kp.getUid()).getLoan() > 0;
			int price = Integer.parseInt(args[2]);
			int min = switch (type) {
				case 1 -> ((KawaiponCard) obj).getCard().getRarity().getIndex() * (hasLoan ? Helper.BASE_CARD_PRICE * 2 : Helper.BASE_CARD_PRICE / 2) * (foil ? 2 : 1);
				case 2 -> hasLoan ? Helper.BASE_EQUIPMENT_PRICE * 2 : Helper.BASE_EQUIPMENT_PRICE / 2;
				default -> hasLoan ? Helper.BASE_FIELD_PRICE * 2 : Helper.BASE_FIELD_PRICE / 2;
			};

			if (price < min) {
				if (hasLoan)
					channel.sendMessage("❌ | Como você possui uma dívida ativa, você não pode leiloar " + (type == 1 ? "essa carta" : type == 2 ? "esse equipamento" : "essa arena") + " por menos que " + min + " créditos.").queue();
				else
					channel.sendMessage("❌ | Você não pode leiloar " + (type == 1 ? "essa carta" : type == 2 ? "esse equipamento" : "essa arena") + " por menos que " + min + " créditos.").queue();
				return;
			}

			ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

			AtomicReference<Future<?>> event = new AtomicReference<>();

			AtomicInteger phase = new AtomicInteger(1);
			AtomicReference<Pair<User, Integer>> highest = new AtomicReference<>(null);

			SimpleMessageListener listener = new SimpleMessageListener() {
				@Override
				public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent evt) {
					if (!evt.getChannel().getId().equals(channel.getId()) || evt.getAuthor().isBot()) return;
					String raw = evt.getMessage().getContentRaw();
					if (StringUtils.isNumeric(raw)) {
						try {
							int offer = Integer.parseInt(raw);

							if (offer >= price && (highest.get() == null || offer > highest.get().getRight())) {
								Kawaipon offerer = KawaiponDAO.getKawaipon(evt.getAuthor().getId());
								Deck dk = offerer.getDeck();
								AtomicReference<Account> oacc = new AtomicReference<>(AccountDAO.getAccount(evt.getAuthor().getId()));

								switch (type) {
									case 1 -> {
										if (offerer.getCards().contains(obj) && !evt.getAuthor().getId().equals(author.getId())) {
											channel.sendMessage("❌ | Parece que você já possui essa carta!").queue();
											return;
										}
									}
									case 2 -> {
										if (dk.checkEquipment((Equipment) obj, channel)) return;
									}
									default -> {
										if (dk.checkField((Field) obj, channel)) return;
									}
								}

								if (oacc.get().getBalance() < offer) {
									channel.sendMessage("❌ | Você não possui créditos suficientes!").queue();
									return;
								}

								highest.set(Pair.of(evt.getAuthor(), offer));
								phase.set(1);

								channel.sendMessage(evt.getAuthor().getAsMention() + " ofereceu **" + Helper.separate(offer) + " créditos**!").queue();

								event.get().cancel(true);
								event.set(exec.scheduleWithFixedDelay(() -> {
									if (phase.get() == 4 && highest.get() != null) {
										channel.sendMessage("**" + (type == 1 ? "Carta vendida" : type == 2 ? "Equipamento vendido" : "Arena vendida") + "** para " + highest.get().getLeft().getAsMention() + " por **" + Helper.separate(highest.get().getRight()) + "** créditos!").queue();

										if (!author.getId().equals(highest.get().getLeft().getId())) {
											Kawaipon k = KawaiponDAO.getKawaipon(author.getId());
											Deck sdk = k.getDeck();

											Kawaipon buyer = KawaiponDAO.getKawaipon(highest.get().getLeft().getId());
											Deck bdk = buyer.getDeck();

											Account acc = AccountDAO.getAccount(author.getId());
											Account bacc = AccountDAO.getAccount(highest.get().getLeft().getId());

											acc.addCredit(highest.get().getRight(), AuctionCommand.class);
											bacc.removeCredit(highest.get().getRight(), AuctionCommand.class);

											switch (type) {
												case 1 -> {
													k.removeCard((KawaiponCard) obj);
													buyer.addCard((KawaiponCard) obj);
												}
												case 2 -> {
													sdk.removeEquipment((Equipment) obj);
													bdk.addEquipment((Equipment) obj);
												}
												default -> {
													sdk.removeField((Field) obj);
													bdk.addField((Field) obj);
												}
											}

											KawaiponDAO.saveKawaipon(k);
											KawaiponDAO.saveKawaipon(buyer);
											AccountDAO.saveAccount(acc);
											AccountDAO.saveAccount(bacc);
										}


										Main.getInfo().getConfirmationPending().remove(author.getId());
										close();
										event.get().cancel(true);
										exec.shutdownNow();
									} else {
										switch (phase.get()) {
											case 1 -> channel.sendMessage("Dou-lhe 1...").queue();
											case 2 -> channel.sendMessage("""
													Dou-lhe 2...
													Vamos lá pessoal, será que eu ouvi um %s?
													""".formatted(Helper.separate(highest.get().getRight() + 250))).queue();
											case 3 -> channel.sendMessage("Dou-lhe 3...").queue();
										}

										phase.getAndIncrement();
									}
								}, 5, 5, TimeUnit.SECONDS));
							}
						} catch (NumberFormatException e) {
							channel.sendMessage("❌ | O valor máximo é " + Helper.separate(Integer.MAX_VALUE) + " créditos!").queue();
						}
					}
				}
			};

			Main.getInfo().getConfirmationPending().put(author.getId(), true);
			channel.sendMessage("Esta carta será vendida para quem oferecer o maior valor. Deseja mesmo leiloá-la?")
					.queue(s -> Pages.buttonize(s, Map.of(Helper.ACCEPT, (mb, ms) -> {
								if (mb.getId().equals(author.getId())) {
									Main.getInfo().getConfirmationPending().remove(author.getId());
									event.set(channel.sendMessage("Não houve nenhuma oferta, declaro o leilão **encerrado**!").queueAfter(30, TimeUnit.SECONDS, msg -> {
												Main.getInfo().getConfirmationPending().remove(author.getId());
												listener.close();
											}
									));

									s.delete().flatMap(d -> channel.sendMessage("✅ | Leilão aberto com sucesso, se não houver ofertas maiores que " + price + " dentro de 30 segundos irei fechá-lo!")).queue();
									ShiroInfo.getShiroEvents().addHandler(guild, listener);
								}
							}), true, 1, TimeUnit.MINUTES,
							u -> u.getId().equals(author.getId()),
							ms -> Main.getInfo().getConfirmationPending().remove(author.getId())
					));
		} catch (NumberFormatException e) {
			channel.sendMessage("❌ | O valor máximo é " + Helper.separate(Integer.MAX_VALUE) + " créditos!").queue();
		}
	}
}

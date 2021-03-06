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

package com.kuuhaku.command.commands.discord.fun;

import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Executable;
import com.kuuhaku.controller.postgresql.*;
import com.kuuhaku.handlers.games.disboard.model.PoliticalState;
import com.kuuhaku.model.annotations.Command;
import com.kuuhaku.model.annotations.Requires;
import com.kuuhaku.model.common.ColorlessEmbedBuilder;
import com.kuuhaku.model.enums.ExceedEnum;
import com.kuuhaku.model.enums.I18n;
import com.kuuhaku.model.enums.Slot;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.model.persistent.Slots;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kuuhaku.model.enums.Slot.*;

@Command(
		name = "roleta",
		aliases = {"slots"},
		usage = "req_bet",
		category = Category.FUN
)
@Requires({Permission.MESSAGE_EXT_EMOJI})
public class SlotsCommand implements Executable {
	private final List<Slot> rolled = new ArrayList<>();

	@Override
	public void execute(User author, Member member, String command, String argsAsText, String[] args, Message message, TextChannel channel, Guild guild, String prefix) {
		if (Main.getInfo().getConfirmationPending().get(author.getId()) != null) {
			channel.sendMessage("❌ | Você não pode jogar slots se estiver em uma transação ou houver outro slots em progresso.").queue();
			return;
		}

		if (args.length == 0) {
			EmbedBuilder eb = new ColorlessEmbedBuilder();
			eb.setDescription(prizeTable());
			eb.setTitle("Tabela de prêmios");
			eb.setFooter("Use `" + prefix + "slots VALOR` para jogar (Valor mínimo: 1000 créditos)");

			channel.sendMessageEmbeds(eb.build()).queue();
			return;
		} else if (!StringUtils.isNumeric(args[0]) || Integer.parseInt(args[0]) < 1000) {
			channel.sendMessage(I18n.getString("err_slots-invalid-number")).queue();
			return;
		} else if (Main.getInfo().gameInProgress(author.getId())) {
			channel.sendMessage(I18n.getString("err_you-are-in-game")).queue();
			return;
		}

		Account acc = AccountDAO.getAccount(author.getId());
		AtomicInteger bet = new AtomicInteger(Integer.parseInt(args[0]));

		if (acc.getTotalBalance() < bet.get()) {
			channel.sendMessage(I18n.getString("err_insufficient-credits-user")).queue();
			return;
		}

		Slots slt = SlotsDAO.getSlots();
		acc.consumeCredit(bet.get(), this.getClass());
		AccountDAO.saveAccount(acc);
		slt.addToPot(Math.round(bet.get() * 0.5));
		SlotsDAO.saveSlots(slt);
		rollSlots();

		Runnable r = () -> {
			Slot combo = Slot.getCombo(rolled);

			if (combo != null) {
				Account facc = AccountDAO.getAccount(author.getId());
				Slots slts = SlotsDAO.getSlots();

				if (combo == JACKPOT) {
					long prize = slts.jackpot();
					SlotsDAO.saveSlots(slts);

					facc.addCredit(prize, this.getClass());
					AccountDAO.saveAccount(facc);

					channel.sendMessage(
							"""
									%s
									         
									<a:YellowArrowLeft:680461765863145503><a:YellowArrowLeft:680461765863145503><a:YellowArrowLeft:680461765863145503><a:YellowArrowLeft:680461765863145503><a:YellowArrowLeft:680461765863145503><a:YellowArrowLeft:680461765863145503><a:YellowArrowLeft:680461765863145503><a:YellowArrowLeft:680461765863145503><a:YellowArrowLeft:680461765863145503>
									Seu prêmio é de __**%s créditos**__!
									<a:YellowArrowRight:680461983342264360><a:YellowArrowRight:680461983342264360><a:YellowArrowRight:680461983342264360><a:YellowArrowRight:680461983342264360><a:YellowArrowRight:680461983342264360><a:YellowArrowRight:680461983342264360><a:YellowArrowRight:680461983342264360><a:YellowArrowRight:680461983342264360><a:YellowArrowRight:680461983342264360>
									""".formatted(combo.getMessage(), Helper.separate(prize))
					).queue();
					LeaderboardsDAO.submit(author, SlotsCommand.class, (int) prize);
				} else {
					long prize = Math.round(bet.get() * combo.getMultiplier());
					SlotsDAO.saveSlots(slts);

					facc.addCredit(prize, this.getClass());
					AccountDAO.saveAccount(facc);

					channel.sendMessage("""
							%s
							Seu prêmio é de __**%s créditos**__!
							""".formatted(combo.getMessage(), Helper.separate(prize))).queue();
					LeaderboardsDAO.submit(author, SlotsCommand.class, (int) prize);
				}

				if (ExceedDAO.hasExceed(author.getId())) {
					PoliticalState ps = PStateDAO.getPoliticalState(ExceedEnum.getByName(ExceedDAO.getExceed(author.getId())));
					ps.modifyInfluence(combo.getInfluence());
					PStateDAO.savePoliticalState(ps);
				}
			} else {
				channel.sendMessage("Poxa, parece que você não teve sorte hoje. Volte sempre!").queue();

				if (ExceedDAO.hasExceed(author.getId())) {
					PoliticalState ps = com.kuuhaku.controller.postgresql.PStateDAO.getPoliticalState(ExceedEnum.getByName(ExceedDAO.getExceed(author.getId())));
					ps.modifyInfluence(false);
					PStateDAO.savePoliticalState(ps);
				}
			}

			Main.getInfo().getConfirmationPending().remove(author.getId());
		};

		String frame = Helper.buildFrame(
				"""
						%
												
						**Prêmio acumulado: __%__**
						┌─┬─┬─┬─┬─┐
						┤%│%│%│%│%├
						└═┴═┴═┴═┴═┘
						"""
		);

		List<String> vals = new ArrayList<>() {{
			add(":white_flower: | **Aposta de " + author.getAsMention() + ": __" + Helper.separate(Integer.parseInt(args[0])) + "__**");
			add(Helper.separate(slt.getPot()));
		}};

		Main.getInfo().getConfirmationPending().put(author.getId(), true);
		channel.sendMessage(frame.formatted(ListUtils.union(vals, showSlots(-1)).toArray(Object[]::new)))
				.delay(3, TimeUnit.SECONDS)
				.flatMap(s -> s.editMessage(frame.formatted(ListUtils.union(vals, showSlots(0)).toArray(Object[]::new))))
				.delay(3, TimeUnit.SECONDS)
				.flatMap(s -> s.editMessage(frame.formatted(ListUtils.union(vals, showSlots(1)).toArray(Object[]::new))))
				.delay(3, TimeUnit.SECONDS)
				.flatMap(s -> s.editMessage(frame.formatted(ListUtils.union(vals, showSlots(2)).toArray(Object[]::new))))
				.delay(3, TimeUnit.SECONDS)
				.flatMap(s -> s.editMessage(frame.formatted(ListUtils.union(vals, showSlots(3)).toArray(Object[]::new))))
				.delay(3, TimeUnit.SECONDS)
				.flatMap(s -> s.editMessage(frame.formatted(ListUtils.union(vals, showSlots(4)).toArray(Object[]::new))))
				.queue(s -> r.run(), Helper::doNothing);
	}

	private List<Object> showSlots(int phase) {
		return switch (phase) {
			case 0 -> List.of(
					rolled.get(0),
					Slots.SLOT,
					Slots.SLOT,
					Slots.SLOT,
					Slots.SLOT
			);
			case 1 -> List.of(
					rolled.get(0),
					rolled.get(1),
					Slots.SLOT,
					Slots.SLOT,
					Slots.SLOT
			);
			case 2 -> List.of(
					rolled.get(0),
					rolled.get(1),
					rolled.get(2),
					Slots.SLOT,
					Slots.SLOT
			);
			case 3 -> List.of(
					rolled.get(0),
					rolled.get(1),
					rolled.get(2),
					rolled.get(3),
					Slots.SLOT
			);
			case 4 -> List.of(
					rolled.get(0),
					rolled.get(1),
					rolled.get(2),
					rolled.get(3),
					rolled.get(4)
			);
			default -> List.of(
					Slots.SLOT,
					Slots.SLOT,
					Slots.SLOT,
					Slots.SLOT,
					Slots.SLOT
			);
		};
	}

	private void rollSlots() {
		rolled.clear();
		rolled.addAll(Helper.getRandomN(List.of(Slots.getSlots()), 5));
	}

	private String prizeTable() {
		return """
				%s%s%s -> x%s
				%s%s%s -> x%s
				%s%s%s -> x%s
				%s%s%s -> x%s
				%s%s%s -> x%s
				%s%s%s -> x%s
				%s%s%s -> x%s
				%s%s%s -> x%s
				%s%s%s -> JACKPOT!
				"""
				.formatted(
						LEMON, LEMON, LEMON, LEMON.getMultiplier(),
						WATERMELON, WATERMELON, WATERMELON, WATERMELON.getMultiplier(),
						CHERRY, CHERRY, CHERRY, CHERRY.getMultiplier(),
						HEART, HEART, HEART, HEART.getMultiplier(),
						BELL, BELL, BELL, BELL.getMultiplier(),
						BAR, BAR, BAR, BAR.getMultiplier(),
						HORSESHOE, HORSESHOE, HORSESHOE, HORSESHOE.getMultiplier(),
						DIAMOND, DIAMOND, DIAMOND, DIAMOND.getMultiplier(),
						JACKPOT, JACKPOT, JACKPOT
				);
	}
}

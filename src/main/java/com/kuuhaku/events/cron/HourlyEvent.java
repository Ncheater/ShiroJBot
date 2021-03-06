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

package com.kuuhaku.events.cron;

import com.kuuhaku.Main;
import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.controller.postgresql.ExceedDAO;
import com.kuuhaku.controller.postgresql.MatchDAO;
import com.kuuhaku.model.persistent.Account;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.JSONObject;
import com.kuuhaku.utils.ShiroInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class HourlyEvent implements Job {
	public static JobDetail hourly;

	@Override
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void execute(JobExecutionContext context) {
		if (!Main.getInfo().isLive())
			for (JDA shard : Main.getShiroShards().getShards()) {
				shard.getPresence().setActivity(Main.getRandomActivity(shard));
			}

		Main.getInfo().setWinner(ExceedDAO.getWinner());
		Helper.logger(this.getClass()).info("Atualizado vencedor mensal.");

		if (Main.getInfo().getDblApi() != null) {
			int size = Main.getShiroShards().getGuilds().size();
			Main.getInfo().getDblApi().setStats(size);
			if (System.getenv().containsKey("DBL_TOKEN")) {
				JSONObject jo = new JSONObject();

				jo.put("guildCount", size);

				String response = Helper.post("https://discord.bots.gg/api/v1/bots/" + Main.getShiroShards().getShards().get(0).getSelfUser().getId() + "/stats", jo, System.getenv("DBL_TOKEN")).toString();
				Helper.logger(this.getClass()).debug(response);
			}
		}

		ShiroInfo.getEmoteLookup().clear();
		for (Emote emote : Main.getShiroShards().getEmotes()) {
			ShiroInfo.getEmoteLookup().put(":" + emote.getName() + ":", emote.getId());
		}

		System.runFinalization();
		System.gc();

		for (File file : Objects.requireNonNull(Main.getInfo().getCollectionsFolder().listFiles())) {
			file.delete();
		}

		List<Account> accs = AccountDAO.getVolatileAccounts();
		for (Account acc : accs) {
			acc.expireVCredit();
			AccountDAO.saveAccount(acc);
		}

		MatchDAO.cleanHistory();
	}
}

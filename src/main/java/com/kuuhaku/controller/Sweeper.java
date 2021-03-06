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

package com.kuuhaku.controller;

import com.kuuhaku.controller.postgresql.Manager;
import com.kuuhaku.utils.Helper;
import org.intellij.lang.annotations.Language;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;

public class Sweeper {
	public static void sweep(Set<String> guildIDs, Set<String> memberIDs) {
		EntityManager em = Manager.getEntityManager();

		List<List<String>> gids = Helper.chunkify(guildIDs, 1000);
		List<List<String>> mids = Helper.chunkify(memberIDs, 1000);

		if (gids.size() > 0)
			executeSweep(em, gids, "DELETE FROM GuildConfig WHERE guildId IN :ids");

		if (mids.size() > 0) {
			executeSweep(em, mids, "DELETE FROM Member WHERE id IN :ids");
		}

		em.close();
	}

	private static void executeSweep(EntityManager em, List<List<String>> chunks, @Language("JPAQL") String query) {
		em.getTransaction().begin();

		Query q = em.createQuery(query);
		for (List<String> ids : chunks) {
			if (ids.isEmpty()) break;
			q.setParameter("ids", ids);
			q.executeUpdate();
		}

		em.getTransaction().commit();
	}
}

/*
 * This file is part of Shiro J Bot.
 * Copyright (C) 2020  Yago Gimenez (KuuHaKu)
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

package com.kuuhaku.controller.postgresql;

import com.kuuhaku.model.persistent.EquipmentMarket;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class EquipmentMarketDAO {
	@SuppressWarnings("unchecked")
	public static List<EquipmentMarket> getCards() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT eqm FROM EquipmentMarket eqm WHERE buyer = ''", EquipmentMarket.class);

		try {
			return (List<EquipmentMarket>) q.getResultList();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<EquipmentMarket> getCardsByCard(String id) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT eqm FROM EquipmentMarket eqm WHERE eqm.card.card.id = UPPER(:id) AND eqm.publishDate IS NOT NULL", EquipmentMarket.class);
		q.setParameter("id", id);

		try {
			return (List<EquipmentMarket>) q.getResultList();
		} finally {
			em.close();
		}
	}

	public static EquipmentMarket getCard(int id) {
		EntityManager em = Manager.getEntityManager();

		try {
			EquipmentMarket eqm = em.find(EquipmentMarket.class, id);
			if (eqm == null || !eqm.getBuyer().isBlank()) return null;
			else return eqm;
		} finally {
			em.close();
		}
	}

	public static void saveCard(EquipmentMarket card) {
		EntityManager em = Manager.getEntityManager();

		em.getTransaction().begin();
		em.merge(card);
		em.getTransaction().commit();

		em.close();
	}
}
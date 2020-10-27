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

import com.kuuhaku.handlers.games.tabletop.games.shoukan.Champion;
import com.kuuhaku.handlers.games.tabletop.games.shoukan.Equipment;
import com.kuuhaku.model.enums.AnimeName;
import com.kuuhaku.model.enums.KawaiponRarity;
import com.kuuhaku.model.persistent.Card;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.EnumSet;
import java.util.List;

public class CardDAO {
	public static Card getCard(String name) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Card c WHERE id = UPPER(:name) AND rarity = 'EQUIPMENT'", Card.class);
		q.setParameter("name", name);

		try {
			return (Card) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public static Card getCard(String name, boolean withUltimate) {
		EntityManager em = Manager.getEntityManager();

		Query q;
		if (withUltimate)
			q = em.createQuery("SELECT c FROM Card c WHERE id = UPPER(:name) AND anime <> 'HIDDEN'", Card.class);
		else
			q = em.createQuery("SELECT c FROM Card c WHERE id = UPPER(:name) AND rarity <> 'ULTIMATE' AND anime <> 'HIDDEN'", Card.class);
		q.setParameter("name", name);

		try {
			return (Card) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public static Card getUltimate(AnimeName anime) {
		EntityManager em = Manager.getEntityManager();

		try {
			return em.find(Card.class, anime.name());
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Card> getCards() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Card c WHERE rarity <> 'ULTIMATE' AND anime IN :animes AND anime <> 'HIDDEN'", Card.class);
		q.setParameter("animes", EnumSet.allOf(AnimeName.class));
		List<Card> c = (List<Card>) q.getResultList();

		em.close();

		return c;
	}

	@SuppressWarnings("unchecked")
	public static List<Card> getAllCards() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Card c WHERE anime IN :animes AND anime <> 'HIDDEN'", Card.class);
		q.setParameter("animes", EnumSet.allOf(AnimeName.class));
		List<Card> c = (List<Card>) q.getResultList();

		em.close();

		return c;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAllCardNames() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c.id FROM Card c WHERE anime IN :animes AND anime <> 'HIDDEN'", String.class);
		q.setParameter("animes", EnumSet.allOf(AnimeName.class));
		List<String> c = (List<String>) q.getResultList();

		em.close();

		return c;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAllEquipmentNames() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT e.card.id FROM Equipment e", String.class);
		List<String> c = (List<String>) q.getResultList();

		em.close();

		return c;
	}

	@SuppressWarnings("unchecked")
	public static List<Equipment> getAllEquipments() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT e FROM Equipment e", Equipment.class);
		List<Equipment> c = (List<Equipment>) q.getResultList();

		em.close();

		return c;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAllChampionNames() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c.card.id FROM Champion c", String.class);
		List<String> c = (List<String>) q.getResultList();

		em.close();

		return c;
	}

	@SuppressWarnings("unchecked")
	public static List<Champion> getAllChampions() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Champion c", Champion.class);
		List<Champion> c = (List<Champion>) q.getResultList();

		em.close();

		return c;
	}

	@SuppressWarnings("unchecked")
	public static List<Card> getCardsByAnime(AnimeName anime) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Card c WHERE rarity <> 'ULTIMATE' AND anime = :anime AND anime <> 'HIDDEN'", Card.class);
		q.setParameter("anime", anime);

		try {
			return (List<Card>) q.getResultList();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Card> getAllCardsByAnime(AnimeName anime) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Card c WHERE anime = :anime AND anime <> 'HIDDEN'", Card.class);
		q.setParameter("anime", anime);

		try {
			return (List<Card>) q.getResultList();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Card> getCardsByRarity(KawaiponRarity rarity) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Card c WHERE rarity = :rarity AND anime IN :animes AND anime <> 'HIDDEN'", Card.class);
		q.setParameter("rarity", rarity);
		q.setParameter("animes", EnumSet.allOf(AnimeName.class));

		try {
			return (List<Card>) q.getResultList();
		} finally {
			em.close();
		}
	}

	public static long totalCards() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT COUNT(c) FROM Card c WHERE rarity <> 'ULTIMATE' AND anime IN :animes AND anime <> 'HIDDEN'", Long.class);
		q.setParameter("animes", EnumSet.allOf(AnimeName.class));

		try {
			return (long) q.getSingleResult();
		} finally {
			em.close();
		}
	}

	public static long totalCards(AnimeName anime) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT COUNT(c) FROM Card c WHERE rarity <> 'ULTIMATE' AND anime = :anime AND anime <> 'HIDDEN'", Long.class);
		q.setParameter("anime", anime);

		try {
			return (long) q.getSingleResult();
		} finally {
			em.close();
		}
	}

	public static long totalCards(KawaiponRarity rarity) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT COUNT(c) FROM Card c WHERE rarity = :rarity AND anime IN :animes AND anime <> 'HIDDEN'", Long.class);
		q.setParameter("rarity", rarity);
		q.setParameter("animes", EnumSet.allOf(AnimeName.class));

		try {
			return (long) q.getSingleResult();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Champion> getFusions() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Champion c WHERE SIZE(requiredCards) > 0", Champion.class);

		try {
			return (List<Champion>) q.getResultList();
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public static Champion getChampion(Card c) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Champion c WHERE card = :card", Champion.class);
		q.setParameter("card", c);

		try {
			return (Champion) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public static Champion getChampion(String name) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Champion c WHERE card.id = UPPER(:card)", Champion.class);
		q.setParameter("card", name);

		try {
			return (Champion) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public static Champion getRandomChampion() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT c FROM Champion c WHERE c.card.anime <> 'HIDDEN' ORDER BY RANDOM()", Champion.class);

		try {
			return (Champion) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public static Equipment getEquipment(Card c) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT e FROM Equipment e WHERE card = :card", Equipment.class);
		q.setParameter("card", c);

		try {
			return (Equipment) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public static Equipment getEquipment(String name) {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT e FROM Equipment e WHERE card.id = UPPER(:card)", Equipment.class);
		q.setParameter("card", name);

		try {
			return (Equipment) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Equipment> getEquipments() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT e FROM Equipment e", Equipment.class);

		try {
			return (List<Equipment>) q.getResultList();
		} finally {
			em.close();
		}
	}
}

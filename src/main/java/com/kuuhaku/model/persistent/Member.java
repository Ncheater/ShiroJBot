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

package com.kuuhaku.model.persistent;

import com.kuuhaku.Main;
import com.kuuhaku.controller.postgresql.AccountDAO;
import com.kuuhaku.controller.postgresql.ExceedDAO;
import com.kuuhaku.controller.sqlite.KGotchiDAO;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "member")
public class Member {
	@Id
	@Column(columnDefinition = "VARCHAR(191)")
	private String id;

	@Column(columnDefinition = "VARCHAR(191) DEFAULT ''")
	private String mid = "";

	@Column(columnDefinition = "VARCHAR(191) DEFAULT ''")
	private String sid = "";

	@Column(columnDefinition = "VARCHAR(191) DEFAULT ''")
	private String waifu = "";

	//TEXTS
	@Column(columnDefinition = "TEXT")
	private String bg = "https://pm1.narvii.com/6429/7f50ee6d5a42723882c6c23a8420f24dfff60e4f_hq.jpg";

	@Column(columnDefinition = "TEXT")
	private String bio = "";

	//NUMBERS
	@Column(columnDefinition = "INT DEFAULT 0")
	private int level = 1;

	@Column(columnDefinition = "INT DEFAULT 0")
	private int xp = 0;

	@Column(columnDefinition = "FLOAT DEFAULT 1.25")
	private float waifuMult = 1.25f;

	@Column(columnDefinition = "BIGINT DEFAULT 0")
	private long lastVoted = 0;

	//SWITCHES
	@Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean markForDelete = false;

	@Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean rulesSent = false;

	public Member() {

	}

	public boolean addXp(Guild g) {
		float mult = 1;

		if (ExceedDAO.hasExceed(this.mid) && Main.getInfo().getWinner().equals(ExceedDAO.getExceed(this.mid)))
			mult *= 2;
		if (g.getMembers().stream().map(net.dv8tion.jda.api.entities.Member::getId).collect(Collectors.toList()).contains(waifu))
			mult *= waifuMult;
		if (KGotchiDAO.getKawaigotchi(mid) != null && !Objects.requireNonNull(KGotchiDAO.getKawaigotchi(mid)).isAlive())
			mult *= 0.8;
		else if (KGotchiDAO.getKawaigotchi(mid) != null)
			mult *= Objects.requireNonNull(KGotchiDAO.getKawaigotchi(mid)).getTier().getUserXpMult();

		xp += 15 * mult;

		if (xp >= (int) Math.pow(level, 2) * 100) {
			level++;
			Account acc = AccountDAO.getAccount(mid);
			acc.addCredit(75 + (10 * level));
			AccountDAO.saveAccount(acc);
			return true;
		}
		return false;
	}

	public void resetXp() {
		level = 1;
		xp = 0;
	}

	public String getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getXp() {
		return xp;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBg() {
		return bg;
	}

	public void setBg(String bg) {
		this.bg = bg;
	}

	public boolean isMarkForDelete() {
		return markForDelete;
	}

	public void setMarkForDelete(boolean markForDelete) {
		this.markForDelete = markForDelete;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getWaifu() {
		if (waifu == null) waifu = "";
		return waifu;
	}

	public void marry(User waifu) {
		this.waifu = waifu.getId();
	}

	public void divorce() {
		this.waifu = "";
		this.waifuMult *= 0.99f;
		Helper.clamp(this.waifuMult, 1.05f, 1.25f);
	}

	public String getExceed() {
		return ExceedDAO.getExceed(this.mid);
	}

	public boolean isRulesSent() {
		return rulesSent;
	}

	public void setRulesSent(boolean rulesSent) {
		this.rulesSent = rulesSent;
	}

	public boolean canVote() {
		return (System.currentTimeMillis() / 1000) - lastVoted > 86400;
	}

	public void vote() {
		lastVoted = System.currentTimeMillis() / 1000;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public float getWaifuMult() {
		return waifuMult;
	}

	public long getLastVoted() {
		return lastVoted;
	}
}

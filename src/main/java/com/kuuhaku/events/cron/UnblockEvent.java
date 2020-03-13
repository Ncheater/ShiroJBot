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

package com.kuuhaku.events.cron;

import com.kuuhaku.Main;
import com.kuuhaku.controller.mysql.MemberDAO;
import com.kuuhaku.controller.sqlite.GuildDAO;
import com.kuuhaku.model.common.RelayBlockList;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UnblockEvent implements Job {
	public static JobDetail unblock;

	@Override
	public void execute(JobExecutionContext context) {
		if (LocalDateTime.now().getHour() % 12 == 0) {
			RelayBlockList.clearBlockedThumbs();
			RelayBlockList.refresh();

			GuildDAO.getAllGuilds().forEach(gc -> {
				if (gc.getCargoVip() != null && !gc.getCargoVip().isEmpty()) {
					Guild g = Main.getInfo().getGuildByID(gc.getGuildID());
					Role r = g.getRoleById(GuildDAO.getGuildById(g.getId()).getCargoVip());
					assert r != null;
					g.retrieveInvites().queue(i -> i.stream()
							.filter(inv -> inv.getInviter() != null)
							.map(inv -> {
								Member m = g.getMember(inv.getInviter());
								assert m != null;
								if (inv.getUses() / LocalDate.now().getDayOfMonth() > 1) return g.addRoleToMember(m, r);
								else return g.removeRoleFromMember(m, r);
							}).collect(Collectors.toList())
							.forEach(AuditableRestAction::queue));
				}
			});
		}

		MemberDAO.getMutedMembers().forEach(m -> {
			Guild g = Main.getInfo().getGuildByID(m.getGuild());
			Member mb = g.getMemberById(m.getUid());
			Role r = g.getRoleById(GuildDAO.getGuildById(g.getId()).getCargoWarn());
			assert r != null;
			assert mb != null;
			if (mb.getRoles().stream().filter(rol -> !rol.isPublicRole()).anyMatch(rol -> !rol.getId().equals(r.getId())) && m.isMuted()) {
				g.modifyMemberRoles(mb, r).complete();
			} else if (!m.isMuted()) {
				List<Role> roles = m.getRoles().toList().stream().map(rol -> g.getRoleById((String) rol)).collect(Collectors.toList());
				g.modifyMemberRoles(mb, roles).complete();
				MemberDAO.removeMutedMember(m);
			}
		});
	}
}

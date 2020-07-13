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

package com.kuuhaku.controller;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.kuuhaku.Main;
import com.kuuhaku.controller.postgresql.ExceedDAO;
import com.kuuhaku.controller.postgresql.GlobalMessageDAO;
import com.kuuhaku.controller.sqlite.GuildDAO;
import com.kuuhaku.controller.sqlite.Manager;
import com.kuuhaku.controller.sqlite.MemberDAO;
import com.kuuhaku.model.common.RelayBlockList;
import com.kuuhaku.model.persistent.GlobalMessage;
import com.kuuhaku.model.persistent.GuildConfig;
import com.kuuhaku.utils.ExceedEnums;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.Tag;
import com.kuuhaku.utils.TagIcons;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Relay {
	private final Map<String, String> relays = new HashMap<>();
	private int relaySize;

	private void checkSize() {
		if (relays.size() != relaySize) {
			relaySize = relays.size();
			Main.getJibril().getPresence().setActivity(Activity.listening("as mensagens de " + relaySize + " servidores!"));
		}
	}

	private WebhookMessage getMessage(String msg, Member m, Guild s) {
		WebhookMessageBuilder wmb = new WebhookMessageBuilder();
		String exceed = ExceedDAO.getExceed(m.getId());

		String filtered = Arrays.stream(msg.split(" ")).map(w -> w =
				(w.contains("<") && w.contains(">") && w.contains(":")) ? ":question:" : w
		).collect(Collectors.joining(" "));
		wmb.setContent(filtered);
		wmb.setAvatarUrl(RelayBlockList.checkThumb(m.getUser().getId()) ? "https://i.pinimg.com/originals/46/15/87/461587d51087bfdf8906149d356f972f.jpg" : m.getUser().getAvatarUrl());
		wmb.setUsername("(" + s.getName() + ") " + (exceed.isEmpty() ? "" : "[" + exceed + "] ") + (m.getUser().getName().length() > 15 ? m.getUser().getName().substring(0, 15) + "..." : m.getUser().getName()));
		return wmb.build();
	}

	private WebhookMessage getMessage(GlobalMessage gm) {
		WebhookMessageBuilder wmb = new WebhookMessageBuilder();

		wmb.setContent(gm.getContent());
		wmb.setAvatarUrl(RelayBlockList.checkThumb(gm.getUserId()) ? "https://i.pinimg.com/originals/46/15/87/461587d51087bfdf8906149d356f972f.jpg" :gm.getAvatar());
		wmb.setUsername(gm.getName().length() > 15 ? gm.getName().substring(0, 15) + "..." : gm.getName());
		return wmb.build();
	}

	private WebhookClient getClient(TextChannel ch, Guild s) {
		List<Webhook> wbs = ch.retrieveWebhooks().complete().stream().filter(w -> w.getOwner() == s.getSelfMember()).collect(Collectors.toList());
		if (wbs.size() != 0) {
			WebhookClientBuilder wcb = new WebhookClientBuilder(wbs.get(0).getUrl());
			return wcb.build();
		} else {
			WebhookClientBuilder wcb = new WebhookClientBuilder(Objects.requireNonNull(Helper.getOrCreateWebhook(ch, "Jibril", Main.getJibril())).getUrl());
			return wcb.build();
		}
	}

	public void relayMessage(Message source, String msg, Member m, Guild s, ByteArrayOutputStream img) {
		updateRelays();
		checkSize();

		GlobalMessage gm = new GlobalMessage();
		gm.setUserId(m.getId());
		gm.setName(m.getUser().getName());
		gm.setAvatar(m.getUser().getAvatarUrl());
		gm.setContent(msg);

		Main.getInfo().getSockets().getChat().onMessage(null, gm.toString());
		GlobalMessageDAO.saveMessage(gm);

		String exceed = ExceedDAO.getExceed(m.getId());

		EmbedBuilder eb = new EmbedBuilder();
		eb.setDescription(Helper.makeEmoteFromMention(msg.split(" ")) + "\n\n ");
		eb.setImage("attachment://image.png");
		eb.setAuthor("(" + s.getName() + ") " + (exceed.isEmpty() ? "" : "[" + exceed + "] ") + m.getUser().getName(), s.getIconUrl(), s.getIconUrl());
		eb.setThumbnail(RelayBlockList.checkThumb(m.getUser().getId()) ? "https://i.pinimg.com/originals/46/15/87/461587d51087bfdf8906149d356f972f.jpg" : m.getUser().getAvatarUrl());
		eb.setFooter(m.getUser().getId(), "http://icons.iconarchive.com/icons/killaaaron/adobe-cc-circles/1024/Adobe-Id-icon.png");
		try {
			eb.setColor(Helper.colorThief(s.getIconUrl()));
		} catch (IOException e) {
			eb.setColor(Helper.getRandomColor());
		}

		StringBuilder badges = new StringBuilder();
		Set<Tag> tags = Tag.getTags(m.getUser(), m);
		com.kuuhaku.model.persistent.Member mbr = MemberDAO.getMemberById(m.getId() + s.getId());

		if (!exceed.isEmpty()) {
			badges.append(TagIcons.getExceed(ExceedEnums.getByName(exceed)));
		}

		tags.forEach(t -> badges.append(t.getEmote(mbr) == null ? "" : t.getEmote(mbr).getTag()));

		eb.addField("Emblemas:", badges.toString(), false);

		MessageBuilder mb = new MessageBuilder();
		mb.setEmbed(eb.build());

		relays.forEach((k, r) -> {
			if (k.equals(s.getId()) && GuildDAO.getGuildById(k).isLiteMode() && m.getUser() != Main.getJibril().getSelfUser())
				return;
			try {
				TextChannel t = Objects.requireNonNull(Main.getJibril().getGuildById(k)).getTextChannelById(r);
				assert t != null;
				if (GuildDAO.getGuildById(k).isAllowImg()) {
					if (GuildDAO.getGuildById(k).isLiteMode()) {
						WebhookClient client = getClient(t, Main.getJibril().getGuildById(k));
						client.send(getMessage(msg, m, s));
						client.close();
					} else {
						if (img != null) {
							t.sendMessage(mb.build()).addFile(img.toByteArray(), "image.png").queue();
						} else {
							t.sendMessage(mb.build()).queue();
						}
					}
				} else {
					if (GuildDAO.getGuildById(k).isLiteMode()) {
						WebhookClient client = getClient(t, Main.getJibril().getGuildById(k));
						client.send(getMessage(msg, m, s));
						client.close();
					} else {
						t.sendMessage(mb.build()).queue();
					}
				}
			} catch (NullPointerException e) {
				GuildDAO.getGuildById(k).setCanalRelay(null);
			} catch (InsufficientPermissionException ex) {
				Guild g = Main.getJibril().getGuildById(k);
				assert g != null;
				try {
					Objects.requireNonNull(g.getOwner()).getUser().openPrivateChannel().queue(c -> c.sendMessage(":x: | Me faltam permissões para enviar mensagens globais no servidor " + g.getName() + ".\n\nPermissões que eu possuo:```" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_WRITE) ? "✅" : "❌") + " Ler/Enviar mensagens\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS) ? "✅" : "❌") + " Inserir links\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_ATTACH_FILES) ? "✅" : "❌") + " Anexar arquivos\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_HISTORY) ? "✅" : "❌") + " Ver histórico de mensagens\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_EXT_EMOJI) ? "✅" : "❌") + " Usar emojis externos\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) ? "✅" : "❌") + " Gerenciar mensagens\n" +
							(g.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS) ? "✅" : "❌") + " Gerenciar webhooks" +
							"```").queue());
					Helper.logger(this.getClass()).error(ex + " | Sevidor " + g.getName());
				} catch (Exception e) {
					Helper.logger(this.getClass()).error(ex + " | Dono " + Objects.requireNonNull(g.getOwner()).getUser().getAsTag());
				}
			}
		});
		try {
			if (!GuildDAO.getGuildById(s.getId()).isLiteMode()) {
				source.delete().queue();
			}
		} catch (InsufficientPermissionException ignore) {
		}
	}

	public void relayMessage(GlobalMessage gm) {
		updateRelays();
		checkSize();

		EmbedBuilder eb = new EmbedBuilder();
		eb.setDescription(gm.getContent());
		eb.setAuthor(gm.getName(), "https://www.pngkey.com/png/full/334-3346073_no-game-no-life-icon.png", "https://www.pngkey.com/png/full/334-3346073_no-game-no-life-icon.png");
		eb.setThumbnail(RelayBlockList.checkThumb(gm.getUserId()) ? "https://i.pinimg.com/originals/46/15/87/461587d51087bfdf8906149d356f972f.jpg" : gm.getAvatar());
		eb.setFooter(gm.getUserId(), "http://icons.iconarchive.com/icons/killaaaron/adobe-cc-circles/1024/Adobe-Id-icon.png");
		try {
			eb.setColor(Helper.colorThief(gm.getAvatar()));
		} catch (IOException e) {
			eb.setColor(Helper.getRandomColor());
		}

		eb.addField("Enviado via dashboard", "", false);

		MessageBuilder mb = new MessageBuilder();
		mb.setEmbed(eb.build());

		relays.forEach((k, r) -> {
			try {
				TextChannel t = Objects.requireNonNull(Main.getJibril().getGuildById(k)).getTextChannelById(r);
				assert t != null;
				if (GuildDAO.getGuildById(k).isAllowImg()) {
					if (GuildDAO.getGuildById(k).isLiteMode()) {
						WebhookClient client = getClient(t, Main.getJibril().getGuildById(k));
						client.send(getMessage(gm));
						client.close();
					} else {
						t.sendMessage(mb.build()).queue();
					}
				} else {
					if (GuildDAO.getGuildById(k).isLiteMode()) {
						WebhookClient client = getClient(t, Main.getJibril().getGuildById(k));
						client.send(getMessage(gm));
						client.close();
					} else {
						t.sendMessage(mb.build()).queue();
					}
				}
			} catch (NullPointerException e) {
				GuildDAO.getGuildById(k).setCanalRelay(null);
			} catch (InsufficientPermissionException ex) {
				Guild g = Main.getJibril().getGuildById(k);
				assert g != null;
				try {
					Objects.requireNonNull(g.getOwner()).getUser().openPrivateChannel().queue(c -> c.sendMessage(":x: | Me faltam permissões para enviar mensagens globais no servidor " + g.getName() + ".\n\nPermissões que eu possuo:```" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_WRITE) ? "✅" : "❌") + " Ler/Enviar mensagens\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS) ? "✅" : "❌") + " Inserir links\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_ATTACH_FILES) ? "✅" : "❌") + " Anexar arquivos\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_HISTORY) ? "✅" : "❌") + " Ver histórico de mensagens\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_EXT_EMOJI) ? "✅" : "❌") + " Usar emojis externos\n" +
							(g.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE) ? "✅" : "❌") + " Gerenciar mensagens\n" +
							(g.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS) ? "✅" : "❌") + " Gerenciar webhooks" +
							"```").queue());
					Helper.logger(this.getClass()).error(ex + " | Sevidor " + g.getName());
				} catch (Exception e) {
					Helper.logger(this.getClass()).error(ex + " | Dono " + Objects.requireNonNull(g.getOwner()).getUser().getAsTag());
				}
			}
		});
	}

	public MessageEmbed getRelayInfo(GuildConfig gc) {
		updateRelays();
		checkSize();
		EmbedBuilder eb = new EmbedBuilder();

		eb.setTitle(":globe_with_meridians: Dados do relay");
		eb.addField(":busts_in_silhouette: Clientes conectados: " + relays.size(), "Canal relay: " + (gc.getCanalRelay() == null ? "Não configurado" : Objects.requireNonNull(Main.getInfo().getGuildByID(gc.getGuildID()).getTextChannelById(gc.getCanalRelay())).getAsMention()), false);
		eb.addField("Modo:", gc.isLiteMode() ? "Lite" : "Normal", true);
		eb.addField("Imagens:", gc.isAllowImg() ? "Permitidas" : "Negadas", true);
		eb.setColor(Helper.getRandomColor());

		return eb.build();
	}

	public Map<String, String> getRelayMap() {
		updateRelays();
		checkSize();

		return relays;
	}

	@SuppressWarnings("unchecked")
	private void updateRelays() {
		EntityManager em = Manager.getEntityManager();

		Query q = em.createQuery("SELECT g FROM GuildConfig g WHERE canalrelay <> '' AND canalrelay IS NOT NULL", GuildConfig.class);

		List<GuildConfig> gc = q.getResultList();
		gc.removeIf(g -> Main.getJibril().getGuildById(g.getGuildID()) == null);
		gc.forEach(g -> relays.put(g.getGuildID(), g.getCanalRelay()));

		em.close();
	}
}

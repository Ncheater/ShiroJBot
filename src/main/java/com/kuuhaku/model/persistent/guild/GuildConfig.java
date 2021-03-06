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

package com.kuuhaku.model.persistent.guild;

import com.kuuhaku.Main;
import com.kuuhaku.model.persistent.guild.buttons.ButtonChannel;
import com.kuuhaku.model.records.embed.Embed;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.JSONUtils;
import com.kuuhaku.utils.ShiroInfo;
import net.dv8tion.jda.api.entities.TextChannel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "guildconfig")
public class GuildConfig {
	@Id
	@Column(columnDefinition = "VARCHAR(255) NOT NULL")
	private String guildId;

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String name = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT 's!'")
	private String prefix = ShiroInfo.getDefaultPrefix();

	//CHANNELS
	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String welcomeChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String byeChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String suggestionChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String levelChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String logChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String alertChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String kawaiponChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String dropChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String generalChannel = "";

	@Column(columnDefinition = "VARCHAR(255) NOT NULL DEFAULT ''")
	private String starboardChannel = "";

	//TEXTS
	@Column(columnDefinition = "TEXT")
	private String welcomeMessage = "Seja bem-vindo(a) ao %guild%, %user%!";

	@Column(columnDefinition = "TEXT")
	private String byeMessage = "Ahh...%user% deixou este servidor!";

	@Column(columnDefinition = "TEXT")
	private String embedTemplate = "{}";

	@Column(columnDefinition = "TEXT")
	private String generalTopic = "";

	//NUMBERS
	@Column(columnDefinition = "BIGINT NOT NULL DEFAULT 60000")
	private long pollTime = 60000;

	@Column(columnDefinition = "BIGINT NOT NULL DEFAULT 0")
	private long muteTime = 0;

	@Column(columnDefinition = "INT NOT NULL DEFAULT 5")
	private int noSpamAmount = 5;

	@Column(columnDefinition = "INT NOT NULL DEFAULT 10")
	private int antiRaidTime = 10;

	@Column(columnDefinition = "INT NOT NULL DEFAULT 3")
	private int starRequirement = 3;

	//SWITCHES
	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
	private boolean levelNotif = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean anyTell = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean hardAntispam = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean antiRaid = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean nqnMode = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean cardSpawn = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean dropSpawn = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean autoExceedRoles = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean smallCards = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean antiHoist = false;

	@Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
	private boolean makeMentionable = false;

	//COLLECTIONS
	//ROLES
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "guildconfig_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<LevelRole> levelRoles = new HashSet<>();

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "guildconfig_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<ColorRole> colorRoles = new HashSet<>();

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "guildconfig_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<PaidRole> paidRoles = new HashSet<>();

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "guildconfig_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<VoiceRole> voiceRoles = new HashSet<>();

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "guildconfig_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<ButtonChannel> buttonConfigs = new HashSet<>();

	//CHANNELS
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinColumn(name = "guildconfig_guildid")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<String> noLinkChannels = new HashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinColumn(name = "guildconfig_guildid")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<String> noSpamChannels = new HashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinColumn(name = "guildconfig_guildid")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<String> noCommandChannels = new HashSet<>();

	//CONFIGS
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinColumn(name = "guildconfig_guildid")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<String> disabledCommands = new HashSet<>();

	//LAZY
	@ElementCollection(fetch = FetchType.LAZY)
	@JoinColumn(name = "guildconfig_guildid")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<String> rules = new ArrayList<>();

	public GuildConfig(String guildId, String name) {
		this.guildId = guildId;
		this.name = name;
	}

	public GuildConfig(String guildId) {
		this.guildId = guildId;
	}

	public GuildConfig() {
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String id) {
		this.guildId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public TextChannel getWelcomeChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(welcomeChannel, "1"));
	}

	public void setWelcomeChannel(String welcomeChannel) {
		this.welcomeChannel = Helper.getOr(welcomeChannel, "");
	}

	public TextChannel getByeChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(byeChannel, "1"));
	}

	public void setByeChannel(String byeChannel) {
		this.byeChannel = Helper.getOr(byeChannel, "");
	}

	public TextChannel getSuggestionChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(suggestionChannel, "1"));
	}

	public void setSuggestionChannel(String suggestionChannel) {
		this.suggestionChannel = Helper.getOr(suggestionChannel, "");
	}

	public TextChannel getLevelChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(levelChannel, "1"));
	}

	public void setLevelChannel(String levelChannel) {
		this.levelChannel = Helper.getOr(levelChannel, "");
	}

	public TextChannel getLogChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(logChannel, "1"));
	}

	public void setLogChannel(String logChannel) {
		this.logChannel = Helper.getOr(logChannel, "");
	}

	public TextChannel getAlertChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(alertChannel, "1"));
	}

	public void setAlertChannel(String alertChannel) {
		this.alertChannel = Helper.getOr(alertChannel, "");
	}

	public TextChannel getKawaiponChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(kawaiponChannel, "1"));
	}

	public void setKawaiponChannel(String kawaiponChannel) {
		this.kawaiponChannel = Helper.getOr(kawaiponChannel, "");
	}

	public TextChannel getDropChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(dropChannel, "1"));
	}

	public void setDropChannel(String dropChannel) {
		this.dropChannel = Helper.getOr(dropChannel, "");
	}

	public TextChannel getGeneralChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(generalChannel, "1"));
	}

	public void setGeneralChannel(String generalChannel) {
		this.generalChannel = Helper.getOr(generalChannel, "");
	}

	public TextChannel getStarboardChannel() {
		return Main.getInfo().getGuildByID(guildId).getTextChannelById(Helper.getOr(starboardChannel, "1"));
	}

	public void setStarboardChannel(String starboardChannel) {
		this.starboardChannel = Helper.getOr(starboardChannel, "");
	}

	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = Helper.getOr(welcomeMessage, "Seja bem-vindo(a) ao %guild%, %user%!");
	}

	public String getByeMessage() {
		return byeMessage;
	}

	public void setByeMessage(String byeMessage) {
		this.byeMessage = Helper.getOr(byeMessage, "Ahh...%user% deixou este servidor!");
	}

	public Set<ButtonChannel> getButtonConfigs() {
		return buttonConfigs;
	}

	public void setButtonConfigs(Set<ButtonChannel> buttonConfigs) {
		this.buttonConfigs = buttonConfigs;
	}

	public Embed getEmbedTemplate() {
		return JSONUtils.fromJSON(Helper.getOr(embedTemplate, "{}"), Embed.class);
	}

	public void setEmbedTemplate(Embed template) {
		if (template == null)
			this.embedTemplate = "{}";
		else
			this.embedTemplate = JSONUtils.toJSON(template);
	}

	public String getEmbedTemplateRaw() {
		return Helper.getOr(embedTemplate, "");
	}

	public String getGeneralTopic() {
		return generalTopic;
	}

	public void setGeneralTopic(String generalTopic) {
		this.generalTopic = Helper.getOr(generalTopic, "Contagem de membros em %count% e subindo!");
	}

	public long getPollTime() {
		return pollTime;
	}

	public void setPollTime(long pollTime) {
		this.pollTime = pollTime;
	}

	public long getMuteTime() {
		return muteTime;
	}

	public void setMuteTime(long muteTime) {
		this.muteTime = muteTime < 60000 ? 0 : muteTime;
	}

	public int getNoSpamAmount() {
		return noSpamAmount;
	}

	public void setNoSpamAmount(int noSpamAmount) {
		this.noSpamAmount = noSpamAmount;
	}

	public int getAntiRaidTime() {
		return antiRaidTime;
	}

	public void setAntiRaidTime(int antiRaidTime) {
		this.antiRaidTime = antiRaidTime;
	}

	public int getStarRequirement() {
		return starRequirement;
	}

	public void setStarRequirement(int starRequirement) {
		this.starRequirement = starRequirement;
	}

	public boolean isLevelNotif() {
		return levelNotif;
	}

	public void setLevelNotif(boolean levelNotif) {
		this.levelNotif = levelNotif;
	}

	public void toggleLevelNotif() {
		this.levelNotif = !levelNotif;
	}

	public boolean isAnyTell() {
		return anyTell;
	}

	public void setAnyTell(boolean anyTell) {
		this.anyTell = anyTell;
	}

	public void toggleAnyTell() {
		this.anyTell = !anyTell;
	}

	public boolean isHardAntispam() {
		return hardAntispam;
	}

	public void setHardAntispam(boolean hardAntispam) {
		this.hardAntispam = hardAntispam;
	}

	public void toggleHardAntispam() {
		this.hardAntispam = !hardAntispam;
	}

	public boolean isAntiRaid() {
		return antiRaid;
	}

	public void setAntiRaid(boolean antiRaid) {
		this.antiRaid = antiRaid;
	}

	public void toggleAntiRaid() {
		this.antiRaid = !antiRaid;
	}

	public boolean isNQNMode() {
		return nqnMode;
	}

	public void setNQNMode(boolean nqnMode) {
		this.nqnMode = nqnMode;
	}

	public void toggleNQNMode() {
		this.nqnMode = !nqnMode;
	}

	public boolean isCardSpawn() {
		return cardSpawn;
	}

	public void setCardSpawn(boolean kawaiponEnabled) {
		this.cardSpawn = kawaiponEnabled;
	}

	public void toggleCardSpawn() {
		this.cardSpawn = !cardSpawn;
	}

	public boolean isDropSpawn() {
		return dropSpawn;
	}

	public void setDropSpawn(boolean dropEnabled) {
		this.dropSpawn = dropEnabled;
	}

	public void toggleDropSpawn() {
		this.dropSpawn = !dropSpawn;
	}

	public boolean isAutoExceedRoles() {
		return autoExceedRoles;
	}

	public void setAutoExceedRoles(boolean autoExceedRoles) {
		this.autoExceedRoles = autoExceedRoles;
	}

	public void toggleAutoExceedRoles() {
		this.autoExceedRoles = !autoExceedRoles;
	}

	public boolean isSmallCards() {
		return smallCards;
	}

	public void setSmallCards(boolean smallCards) {
		this.smallCards = smallCards;
	}

	public void toggleSmallCards() {
		this.smallCards = !smallCards;
	}

	public boolean isAntiHoist() {
		return antiHoist;
	}

	public void setAntiHoist(boolean antiHoist) {
		this.antiHoist = antiHoist;
	}

	public void toggleAntiHoist() {
		this.antiHoist = !antiHoist;
	}

	public boolean isMakeMentionable() {
		return makeMentionable;
	}

	public void setMakeMentionable(boolean makeMentionable) {
		this.makeMentionable = makeMentionable;
	}

	public void toggleMakeMentionable() {
		this.makeMentionable = !makeMentionable;
	}

	public Set<LevelRole> getLevelRoles() {
		return levelRoles;
	}

	public void addLevelRole(String id, int level) {
		LevelRole lr = new LevelRole(id, level);

		levelRoles.remove(lr);
		levelRoles.add(lr);
	}

	public void removeLevelRole(String id) {
		levelRoles.removeIf(lr -> lr.getId().equals(id));
	}

	public void setLevelRoles(Set<LevelRole> levelRoles) {
		this.levelRoles = levelRoles;
	}

	public Set<ColorRole> getColorRoles() {
		return colorRoles;
	}

	public void addColorRole(String id, Color color, String name) {
		colorRoles.add(new ColorRole(id, color, name));
	}

	public void addColorRole(String id, String color, String name) {
		ColorRole cr = new ColorRole(id, color, name);

		colorRoles.remove(cr);
		colorRoles.add(cr);
	}

	public void removeColorRole(String name) {
		colorRoles.removeIf(cr -> cr.getName().equals(name));
	}

	public void setColorRoles(Set<ColorRole> colorRoles) {
		this.colorRoles = colorRoles;
	}

	public Set<PaidRole> getPaidRoles() {
		return paidRoles;
	}

	public void addPaidRole(String id, int price, long expiration) {
		PaidRole pr = new PaidRole(id, price, expiration);

		paidRoles.remove(pr);
		paidRoles.add(pr);
	}

	public void removePaidRole(String id) {
		paidRoles.removeIf(pr -> pr.getId().equals(id));
	}

	public void setPaidRoles(Set<PaidRole> paidRoles) {
		this.paidRoles = paidRoles;
	}

	public Set<VoiceRole> getVoiceRoles() {
		return voiceRoles;
	}

	public void addVoiceRole(String id, long time) {
		VoiceRole vr = new VoiceRole(id, time);

		voiceRoles.remove(vr);
		voiceRoles.add(vr);
	}

	public void removeVoiceRole(String id) {
		voiceRoles.removeIf(vr -> vr.getId().equals(id));
	}

	public void setVoiceRoles(Set<VoiceRole> voiceRoles) {
		this.voiceRoles = voiceRoles;
	}

	public Set<String> getNoLinkChannels() {
		return noLinkChannels;
	}

	public void addNoLinkChannel(String channel) {
		noLinkChannels.add(channel);
	}

	public void removeNoLinkChannel(String channel) {
		noLinkChannels.remove(channel);
	}

	public void setNoLinkChannels(Set<String> noLinkChannels) {
		this.noLinkChannels = noLinkChannels;
	}

	public Set<String> getNoSpamChannels() {
		return noSpamChannels;
	}

	public void addNoSpamChannel(String channel) {
		noSpamChannels.add(channel);
	}

	public void removeNoSpamChannel(String channel) {
		noSpamChannels.remove(channel);
	}

	public void setNoSpamChannels(Set<String> noSpamChannels) {
		this.noSpamChannels = noSpamChannels;
	}

	public Set<String> getNoCommandChannels() {
		return noCommandChannels;
	}

	public void addNoCommandChannel(String channel) {
		noCommandChannels.add(channel);
	}

	public void removeNoCommandChannel(String channel) {
		noCommandChannels.remove(channel);
	}

	public void setNoCommandChannels(Set<String> noCommandChannels) {
		this.noCommandChannels = noCommandChannels;
	}

	public List<String> getRules() {
		return rules;
	}

	public void addRule(String rule) {
		rules.add(rule);
	}

	public void removeRule(int index) {
		rules.remove(index);
	}

	public void moveRule(int from, int to) {
		rules.add(to, rules.remove(from));
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public Set<String> getDisabledCommands() {
		return disabledCommands;
	}

	public void disableCommand(Class<?> klass) {
		disabledCommands.add(klass.getName());
	}

	public void enableCommand(Class<?> klass) {
		disabledCommands.remove(klass.getName());
	}

	public void setDisabledCommands(Set<Class<?>> classes) {
		disabledCommands = classes.stream().map(Class::getName).collect(Collectors.toSet());
	}
}

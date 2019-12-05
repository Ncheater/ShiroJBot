/*
 * This file is part of Shiro J Bot.
 *
 *     Shiro J Bot is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Shiro J Bot is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Shiro J Bot.  If not, see <https://www.gnu.org/licenses/>
 */

package com.kuuhaku.command.commands.dev;

import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.MySQL.Tag;
import com.kuuhaku.model.Tags;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;

import javax.persistence.NoResultException;

public class PartnerTagCommand extends Command {

    public PartnerTagCommand() {
        super("switchpartner", new String[]{"mudaparceiro", "tagparceiro", "éparça"}, "<@usuário>", "Define um usuário como parceiro ou não.", Category.DEVS);
    }

    @Override
    public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, Event event, String prefix) {
        if (message.getMentionedUsers().size() > 0) {
            if (message.getMentionedUsers().size() == 1) {
                try {
                    Tags t = Tag.getTagById(message.getMentionedUsers().get(0).getId());
                    if (t.isPartner()) {
                        Tag.removeTagPartner(message.getMentionedUsers().get(0).getId());
                        channel.sendMessage(message.getMentionedUsers().get(0).getAsMention() + " não é mais parceiro, foi bom enquanto durou!").queue();
                    } else {
                        Tag.giveTagPartner(message.getMentionedUsers().get(0).getId());
                        channel.sendMessage(message.getMentionedUsers().get(0).getAsMention() + " agora é um parceiro, que iniciem os negócios!").queue();
                    }
                } catch (NoResultException e) {
                    Tag.addUserTagsToDB(message.getMentionedUsers().get(0).getId());
                    Tags t = Tag.getTagById(message.getMentionedUsers().get(0).getId());
                    if (t.isPartner()) {
                        Tag.removeTagPartner(message.getMentionedUsers().get(0).getId());
                        channel.sendMessage(message.getMentionedUsers().get(0).getAsMention() + " não é mais parceiro, foi bom enquanto durou!").queue();
                    } else {
                        Tag.giveTagPartner(message.getMentionedUsers().get(0).getId());
                        channel.sendMessage(message.getMentionedUsers().get(0).getAsMention() + " agora é um parceiro, que iniciem os negócios!").queue();
                    }
                }
            } else {
                channel.sendMessage(":x: | Nii-chan, você mencionou usuários demais!").queue();
            }
        } else {
            try {
                if (Main.getInfo().getUserByID(args[0]) != null) {
                    try {
                        Tags t = Tag.getTagById(args[0]);
                        if (t.isPartner()) {
                            Tag.removeTagPartner(args[0]);
                            channel.sendMessage("<@" + args[0] + "> não é mais parceiro, foi bom enquanto durou!").queue();
                        } else {
                            Tag.giveTagPartner(args[0]);
                            channel.sendMessage("<@" + args[0] + "> agora é um parceiro, que iniciem os negócios!").queue();
                        }
                    } catch (NoResultException e) {
                        Tag.addUserTagsToDB(args[0]);
                        Tags t = Tag.getTagById(args[0]);
                        if (t.isPartner()) {
                            Tag.removeTagPartner(args[0]);
                            channel.sendMessage("<@" + args[0] + "> não é mais parceiro, foi bom enquanto durou!").queue();
                        } else {
                            Tag.giveTagPartner(args[0]);
                            channel.sendMessage("<@" + args[0] + "> agora é um parceiro, que iniciem os negócios!").queue();
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                channel.sendMessage(":x: | Nii-chan bobo, você precisa mencionar um usuário!").queue();
            }
        }
    }
}

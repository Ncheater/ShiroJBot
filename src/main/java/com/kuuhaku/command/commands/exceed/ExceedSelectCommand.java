package com.kuuhaku.command.commands.exceed;

import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.MySQL;
import com.kuuhaku.controller.SQLite;
import com.kuuhaku.model.Exceed;
import com.kuuhaku.model.Profile;
import com.kuuhaku.utils.ExceedEnums;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.LogLevel;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.kuuhaku.model.Profile.*;

public class ExceedSelectCommand extends Command {
	public ExceedSelectCommand() {
		super("exceedselect", new String[]{"exselect", "sou"}, "Escolhe seu exceed, esta escolha é permanente.", Category.EXCEED);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, Event event, String prefix) {
		channel.sendMessage("<a:Loading:598500653215645697> Analisando dados...").queue(m -> {
			com.kuuhaku.model.Member u = SQLite.getMemberByMid(author.getId());

			if (u.getExceed().isEmpty()) {
				if (args.length == 0) {
					channel.sendMessage("Os exceeds disponíveis são:" +
							"\n**" + ExceedEnums.IMANITY.getName() + "**" +
							"\n**" + ExceedEnums.SEIREN.getName() + "**" +
							"\n**" + ExceedEnums.WEREBEAST.getName() + "**" +
							"\n**" + ExceedEnums.LUMAMANA.getName() + "**" +
							"\n**" + ExceedEnums.EXMACHINA.getName() + "**" +
							"\n**" + ExceedEnums.FLUGEL.getName() + "**" +
							"\n\nEscolha usando " + prefix + "`exselect EXCEED`.\n__**ESTA ESCOLHA É PERMANENTE**__").queue();
					return;
				}
				switch (args[0].toLowerCase()) {
					case "imanity":
						u.setExceed(ExceedEnums.IMANITY.getName());
						break;
					case "seiren":
						u.setExceed(ExceedEnums.SEIREN.getName());
						break;
					case "werebeast":
						u.setExceed(ExceedEnums.WEREBEAST.getName());
						break;
					case "lumamana":
						u.setExceed(ExceedEnums.LUMAMANA.getName());
						break;
					case "ex-machina":
						u.setExceed(ExceedEnums.EXMACHINA.getName());
						break;
					case "flügel":
						u.setExceed(ExceedEnums.FLUGEL.getName());
						break;
					default:
						channel.sendMessage(":x: | Exceed inexistente.").queue();
						return;
				}
				SQLite.updateMemberSettings(u);
				channel.sendMessage("Exceed escolhido com sucesso, você agora pertence à **" + u.getExceed() + "**.").queue();
				MySQL.getExceedMembers(ExceedEnums.getByName(u.getExceed())).forEach(em ->
					Main.getInfo().getUserByID(em.getMid()).openPrivateChannel().queue(c -> {
						try {
							c.sendMessage(author.getAsTag() + " juntou-se à " + u.getExceed() + ", dê-o(a) as boas-vindas!").queue();
						} catch (Exception ignore) {
						}
					}));
				m.delete().queue();
			} else {
				m.editMessage(":x: | Você já pertence à um exceed, não é possível trocá-lo.").queue();
			}
		});
	}
}

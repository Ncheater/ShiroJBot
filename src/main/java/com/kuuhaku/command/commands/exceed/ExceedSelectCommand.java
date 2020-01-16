package com.kuuhaku.command.commands.exceed;

import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.controller.mysql.ExceedDAO;
import com.kuuhaku.controller.sqlite.MemberDAO;
import com.kuuhaku.utils.ExceedEnums;
import com.kuuhaku.utils.TagIcons;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class ExceedSelectCommand extends Command {
	public ExceedSelectCommand() {
		super("exceedselect", new String[]{"exselect", "sou"}, "Escolhe seu exceed, esta escolha é permanente.", Category.EXCEED);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, String prefix) {
		channel.sendMessage("<a:Loading:598500653215645697> Analisando dados...").queue(m -> {
			List<com.kuuhaku.model.Member> u = MemberDAO.getMemberByMid(author.getId());

			if (u.get(0).getExceed().isEmpty()) {
				if (args.length == 0) {
					channel.sendMessage("Exceed é um sistema global de clãs, onde todo mês o clã vencedor ira receber experiência em dobro por uma semana. A pontuação é dada pela soma da experiência de todos os membros do clã, **independente do servidor**.\n\n" +
							"Os exceeds disponíveis são:" +
							"\n" + TagIcons.getExceed(ExceedEnums.IMANITY) + "**" + ExceedEnums.IMANITY.getName() + "** - Os engenhosos humanos." +
							"\n" + TagIcons.getExceed(ExceedEnums.SEIREN) + "**" + ExceedEnums.SEIREN.getName() + "** - As curiosas sereias." +
							"\n" + TagIcons.getExceed(ExceedEnums.WEREBEAST) + "**" + ExceedEnums.WEREBEAST.getName() + "** - Os sábios bestiais." +
							"\n" + TagIcons.getExceed(ExceedEnums.ELF) + "**" + ExceedEnums.ELF.getName() + "** - Os místicos elfos." +
							"\n" + TagIcons.getExceed(ExceedEnums.EXMACHINA) + "**" + ExceedEnums.EXMACHINA.getName() + "** - Os poderosos androides." +
							"\n" + TagIcons.getExceed(ExceedEnums.FLUGEL) + "**" + ExceedEnums.FLUGEL.getName() + "** - Os divinos anjos." +
							"\n\nEscolha usando `" + prefix + "exselect EXCEED`.\n__**ESTA ESCOLHA É PERMANENTE**__").queue();
					m.delete().queue();
					return;
				}
				switch (args[0].toLowerCase()) {
					case "imanity":
						u.forEach(us -> us.setExceed(ExceedEnums.IMANITY.getName()));
						break;
					case "seiren":
						u.forEach(us -> us.setExceed(ExceedEnums.SEIREN.getName()));
						break;
					case "werebeast":
						u.forEach(us -> us.setExceed(ExceedEnums.WEREBEAST.getName()));
						break;
					case "elf":
						u.forEach(us -> us.setExceed(ExceedEnums.ELF.getName()));
						break;
					case "ex-machina":
						u.forEach(us -> us.setExceed(ExceedEnums.EXMACHINA.getName()));
						break;
					case "flügel":
						u.forEach(us -> us.setExceed(ExceedEnums.FLUGEL.getName()));
						break;
					default:
						channel.sendMessage(":x: | Exceed inexistente.").queue();
						return;
				}
				u.forEach(MemberDAO::updateMemberConfigs);
				channel.sendMessage("Exceed escolhido com sucesso, você agora pertence à **" + u.get(0).getExceed() + "**.").queue();
				ExceedDAO.getExceedMembers(ExceedEnums.getByName(u.get(0).getExceed())).forEach(em ->
						Main.getInfo().getUserByID(em.getMid()).openPrivateChannel().queue(c -> {
							try {
								c.sendMessage(author.getAsTag() + " juntou-se à " + u.get(0).getExceed() + ", dê-o(a) as boas-vindas!").queue();
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

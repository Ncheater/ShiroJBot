package com.kuuhaku.command.commands.fun;

import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.model.Profile;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.LogLevel;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ExpandingBrainCommand extends Command {

	public ExpandingBrainCommand() {
		super("eb", new String[]{"menteexpandida", "expandingbrain", "brain"}, "<opção 1>;<opção 2>;<opção 3>;<opção 4>", "Gera um meme no formato \"Dois botões\"", Category.FUN);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, Event event, String prefix) {

		if (args.length < 1) {
			channel.sendMessage(":x: | Você tem que escrever a mensagem que deseja que apareca no meme.").queue();
			return;
		} else if (String.join(" ", args).split(";").length < 4) {
			channel.sendMessage(":x: | Você precisa escrever quatro opções para o meme (separados por ponto-e-vírgula).").queue();
			return;
		}

		try {
			BufferedImage bi = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("Two-Buttons.jpg")));
			Graphics2D g2d = bi.createGraphics();

			g2d.setColor(Color.BLACK);
			g2d.setFont(new Font("Impact", Font.BOLD, 25));
			Profile.drawStringMultiLine(g2d, String.join(" ", args).split(";")[0], 400, 20, 20);
			Profile.drawStringMultiLine(g2d, String.join(" ", args).split(";")[1], 400, 20, 320);
			Profile.drawStringMultiLine(g2d, String.join(" ", args).split(";")[2], 400, 20, 630);
			Profile.drawStringMultiLine(g2d, String.join(" ", args).split(";")[3], 400, 20, 910);

			g2d.dispose();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", baos);

			channel.sendMessage("Aqui está seu meme " + author.getAsMention() + "!").addFile(baos.toByteArray(), "tmv.jpg").queue();
		} catch (IOException e) {
			Helper.log(this.getClass(), LogLevel.ERROR, e.toString());
		}
	}

}

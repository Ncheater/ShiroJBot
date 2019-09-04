package com.kuuhaku.command.commands.misc;

import bsh.Interpreter;
import com.kuuhaku.Main;
import com.kuuhaku.command.Category;
import com.kuuhaku.command.Command;
import com.kuuhaku.utils.BannedVars;
import com.kuuhaku.utils.Helper;
import com.kuuhaku.utils.LogLevel;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompileCommand extends Command {
	public CompileCommand() {
		super("compilar", new String[]{"compile"}, "Executa um código Java.", Category.MISC);
	}

	@Override
	public void execute(User author, Member member, String rawCmd, String[] args, Message message, MessageChannel channel, Guild guild, Event event, String prefix) {
		final Runnable compile = () -> channel.sendMessage("<a:Loading:598500653215645697> | Compilando...").queue(m -> {
			try {
				String code = String.join(" ", args);
				if (!code.contains("out")) throw new Exception("Código sem retorno.");
				else if (code.contains("```") && !code.contains("```java")) {
					throw new IllegalArgumentException("Bloco de código com começo incorreto");
				} else if (Arrays.stream(BannedVars.vars).parallel().anyMatch(code::contains))
					throw new IllegalAccessException("Código com métodos proibidos.");
				code = code.replace("```java", "").replace("```", "");
				Interpreter i = new Interpreter();
				i.set("msg", message);
				i.set("code", String.join(" ", args));
				i.eval(code);
				Object out = i.get("out");
				m.editMessage("<:Verified:591425071772467211> | Compilado com sucesso!").queue(n ->
						m.getChannel().sendMessage("<a:Loading:598500653215645697> | Executando...").queue(d ->
								d.editMessage("-> " + out.toString()).queue()));
				message.delete().queue();
			} catch (Exception e) {
				m.editMessage(":x: | Erro ao compilar: ```" + e.toString().replace("`", "´") + "```").queue();
			}
		});
		try {
			String time = Main.getInfo().getPool().submit(compile, ManagementFactory.getThreadMXBean().getThreadInfo(Thread.currentThread().getId()).getThreadName()).get(5, TimeUnit.SECONDS);
			channel.sendMessage("<:Verified:591425071772467211> | Tempo de execução: " + time + " ms").queue();
		} catch (InterruptedException | ExecutionException e) {
			Helper.log(this.getClass(), LogLevel.ERROR, e + " | " + e.getStackTrace()[0]);
		} catch (TimeoutException e) {
			channel.sendMessage(":x: | Seu código ultrapassou o tempo limite de compilação.").queue();
		}
	}

}

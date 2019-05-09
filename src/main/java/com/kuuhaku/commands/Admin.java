package com.kuuhaku.commands;

import com.kuuhaku.model.guildConfig;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Admin {
    public static void config(String[] cmd, MessageReceivedEvent message, guildConfig gc) {
        try {
            switch (cmd[1]) {
                case "canalbv":
                    try {
                        gc.setCanalbv(message.getMessage().getMentionedChannels().get(0).getId());
                        message.getChannel().sendMessage("Canal de boas-vindas trocado para " + message.getGuild().getTextChannelById(gc.getCanalbv()).getAsMention()).queue();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        message.getChannel().sendMessage("E qual canal devo usar para mensagens de boas-vindas? Núlo não é um canal válido!").queue();
                    }
                    break;
                case "canalav":
                    try {
                        gc.setCanalav(message.getMessage().getMentionedChannels().get(0).getId());
                        message.getChannel().sendMessage("Canal de avisos trocado para " + message.getGuild().getTextChannelById(gc.getCanalav()).getAsMention()).queue();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        message.getChannel().sendMessage("E qual canal devo usar para mensagens de aviso? Núlo não é um canal válido!").queue();
                    }
                    break;
                case "prefixo":
                    try {
                        gc.setPrefix(cmd[2]);
                        message.getChannel().sendMessage("Prefixo trocado para __**" + gc.getPrefix() + "**__").queue();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        message.getChannel().sendMessage("Faltou me dizer o prefixo, bobo!").queue();
                    }
                    break;
                case "msgbv":
                    try {
                        if (cmd[2].contains("\"")) {
                            gc.setMsgBoasVindas(String.join("", message.getMessage().getContentRaw().split(gc.getPrefix() + "definir msgbv")));
                            message.getChannel().sendMessage("Agora irei dizer __**" + gc.getMsgBoasVindas(null) + "**__ para usuários que entrarem no servidor!").queue();
                        } else {
                            message.getChannel().sendMessage("A mensagem deve estar entre aspas (\")").queue();
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        message.getChannel().sendMessage("Você não me disse que mensagem devo dizer quando alguém entrar!").queue();
                    }
                    break;
                case "msgadeus":
                    try {
                        if (cmd[2].contains("\"")) {
                            gc.setMsgAdeus(String.join("", message.getMessage().getContentRaw().split(gc.getPrefix() + "definir msgadeus")));
                            message.getChannel().sendMessage("Agora irei dizer __**" + gc.getMsgAdeus(null) + "**__ para membros que deixarem o servidor!").queue();
                        } else {
                            message.getChannel().sendMessage("A mensagem deve estar entre aspas (\")").queue();
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        message.getChannel().sendMessage("Você não me disse que mensagem devo dizer quando alguém sair!").queue();
                    }
                    break;
                default:
                    message.getChannel().sendMessage("Não conheço esse comando, certeza que digitou corretamente?").queue();
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            message.getChannel().sendMessage("Você precisa me dizer o quê devo definir").queue();
        }
    }
}

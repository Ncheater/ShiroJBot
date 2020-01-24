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

package com.kuuhaku.handlers.api.websocket;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.kuuhaku.Main;
import com.kuuhaku.controller.mysql.GlobalMessageDAO;
import com.kuuhaku.controller.mysql.TokenDAO;
import com.kuuhaku.controller.sqlite.MemberDAO;
import com.kuuhaku.model.GlobalMessage;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

public class WebSocketConfig {

	private final SocketIOServer socket;

	public WebSocketConfig() {
		Thread.currentThread().setName("chat-websocket");
		Configuration config = new Configuration();
		config.setHostname("localhost");
		config.setPort(8000);

		socket = new SocketIOServer(config);
		socket.addEventListener("chatevent", JSONObject.class, (client, data, ackSender) -> {
			User u = Main.getInfo().getUserByID(data.getString("userID"));

			Helper.logger(this.getClass()).info("Mensagem enviada por " + u.getName() + ": " + data.getString("content"));

			GlobalMessage gm = new GlobalMessage();

			gm.setUserId(u.getId());
			gm.setName(u.getName());
			gm.setAvatar(u.getAvatarUrl());
			gm.setContent(data.getString("content"));

			GlobalMessageDAO.saveMessage(gm);

			Main.getRelay().relayMessage(gm);

			socket.getBroadcastOperations().sendEvent("chat", gm.toString());
		});
		socket.addEventListener("requestprofile", JSONObject.class, (client, data, ackSender) -> {
			try {
				JSONObject request = new JSONObject(data);
				if (!TokenDAO.validateToken(request.getString("token"))) return;
				socket.getBroadcastOperations().sendEvent("update", MemberDAO.getMemberById(request.getString("id")));
			} catch (Exception ignore) {
			}
		});
		socket.start();
	}

	public SocketIOServer getSocket() {
		return socket;
	}
}

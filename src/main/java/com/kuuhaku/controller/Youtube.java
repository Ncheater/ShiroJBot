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

package com.kuuhaku.controller;

import com.google.api.services.youtube.YouTube;
import com.kuuhaku.Main;
import com.kuuhaku.model.common.YoutubeVideo;
import com.kuuhaku.utils.Helper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Youtube {
	public static List<YoutubeVideo> getData(String query) throws IOException {
		URL url = new URL(YouTube.DEFAULT_BASE_URL + "search?key=" + Main.getInfo().getYoutubeToken() + "&part=snippet&type=video&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString()) + "&maxResults=5");
		JSONArray ja = requestVideoData(url);
		List<YoutubeVideo> videos = new ArrayList<>();
		for (Object j : ja) {
			JSONObject jid = ((JSONObject) j).getJSONObject("id");
			JSONObject jsnippet = ((JSONObject) j).getJSONObject("snippet");

			String id = jid.getString(jid.has("videoId") ? "videoId" : "playlistId");
			String title = jsnippet.getString("title");
			String desc = jsnippet.getString("description");
			String thumb = jsnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
			String channel = jsnippet.getString("channelTitle");
			videos.add(new YoutubeVideo(id, title, desc, thumb, channel));
		}
		return videos;
	}

	private static JSONArray requestVideoData(URL url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "application/json");
		con.addRequestProperty("Accept-Charset", "UTF-8");
		con.addRequestProperty("User-Agent", "Mozilla/5.0");

		String resposta = Helper.getResponse(con);

		Helper.logger(Tradutor.class).debug(resposta);
		JSONObject json = new JSONObject(resposta);
		return json.getJSONArray("items");
	}

	public static YoutubeVideo getSingleData(String query) throws IOException {
		URL url = new URL(YouTube.DEFAULT_BASE_URL + "search?key=" + Main.getInfo().getYoutubeToken() + "&part=snippet&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString()) + "&maxResults=5");
		JSONArray ja = requestVideoData(url);
		JSONObject jid = ja.getJSONObject(0).getJSONObject("id");
		JSONObject jsnippet = ja.getJSONObject(0).getJSONObject("snippet");

		String id = jid.getString(jid.has("videoId") ? "videoId" : "playlistId");
		String title = jsnippet.getString("title");
		String desc = jsnippet.getString("description");
		String thumb = jsnippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
		String channel = jsnippet.getString("channelTitle");

		return new YoutubeVideo(id, title, desc, thumb, channel);
	}
}

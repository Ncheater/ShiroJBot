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

package com.kuuhaku.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.kuuhaku.model.enums.JsonType;

import java.util.List;

public class JSONUtils {
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapterFactory(new RecordTypeAdapterFactory())
			.registerTypeAdapter(JSONWrapper.class, new JSONAdapter())
			.create();

	public static String toJSON(Object o) {
		if (o instanceof JSONWrapper) {
			return gson.toJson(o, JSONWrapper.class);
		}

		return gson.toJson(o);
	}

	@SuppressWarnings("UnstableApiUsage")
	public static <T> T fromJSON(String json, Class<T> klass) {
		return gson.fromJson(json, new TypeToken<List<T>>() {
		}.getType());
	}

	public static JsonObject parseJSONObject(String json) {
		return JsonParser.parseString(json).getAsJsonObject();
	}

	public static JsonArray parseJSONArray(String json) {
		return JsonParser.parseString(json).getAsJsonArray();
	}

	public static JsonElement parseJSONElement(String json) {
		return JsonParser.parseString(json);
	}

	public static JsonObject parseJSONObject(Object o) {
		return parseJSONObject(toJSON(o));
	}

	public static JsonArray parseJSONArray(Object o) {
		return parseJSONArray(toJSON(o));
	}

	public static JsonElement parseJSONElement(Object o) {
		return parseJSONElement(toJSON(o));
	}

	public static JsonType getType(String json) {
		JsonElement je = JsonParser.parseString(json);
		return je.isJsonObject() ? JsonType.OBJECT : JsonType.ARRAY;
	}
}

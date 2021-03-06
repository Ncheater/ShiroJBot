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

package com.kuuhaku.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NContract<A> extends CompletableFuture<A> {
	private final int signers;
	private Function<List<A>, A> action = null;
	private final Map<Integer, A> signatures = new HashMap<>();

	public NContract(int signers, Function<List<A>, A> action) {
		this.signers = signers;
		this.action = action;
	}

	public NContract(int signers) {
		this.signers = signers;
	}

	public Function<List<A>, A> getAction() {
		return action;
	}

	public void setAction(Function<List<A>, A> action) {
		this.action = action;
	}

	public Map<Integer, A> getSignatures() {
		return signatures;
	}

	public void addSignature(int index, A signature) {
		this.signatures.put(index, signature);
		A result = checkContract();
		if (result != null) complete(result);
	}

	private A checkContract() {
		if (this.signatures.size() == signers) {
			List<A> ordered = signatures.entrySet().stream()
					.sorted(Comparator.comparingInt(Map.Entry::getKey))
					.map(Map.Entry::getValue)
					.collect(Collectors.toList());

			A result = action.apply(ordered);
			signatures.clear();
			return result;
		}

		return null;
	}
}

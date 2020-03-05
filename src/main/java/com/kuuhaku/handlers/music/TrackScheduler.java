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

package com.kuuhaku.handlers.music;

import com.kuuhaku.utils.Helper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer player;
	private final BlockingQueue<AudioTrack> queue;
	public TextChannel channel;

	/**
	 * @param player The audio player this scheduler uses
	 */
	TrackScheduler(AudioPlayer player, TextChannel channel) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
		this.channel = channel;
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(AudioTrack track) {
		if (!player.startTrack(track, true)) {
			queue.offer(track);
		}
	}

	public BlockingQueue<AudioTrack> queue() {
		return queue;
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		player.startTrack(queue.poll(), false);
	}

	/**
	 * Stop the track.
	 */
	public void pauseTrack() {
		player.setPaused(true);
	}

	public void resumeTrack() {
		player.setPaused(false);
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			nextTrack();

			EmbedBuilder eb = new EmbedBuilder();

			try {
				AudioTrackInfo ati = player.getPlayingTrack().getInfo();

				eb.setColor(Helper.getRandomColor());
				eb.setTitle("Tocando agora: " + ati.title + " (" + String.valueOf(Helper.round((ati.length / 1000f) / 60f, 2)).replace(".", ":") + ")");
				eb.setFooter("Autor: " + ati.author + "Requisitado por: " + ((User) player.getPlayingTrack().getUserData()).getAsTag());

				channel.sendMessage(eb.build()).queue(null, Helper::doNothing);
			} catch (Exception e) {
				eb.setColor(Helper.getRandomColor());
				eb.setTitle("Fila de músicas encerrada, obrigado por mais um ouvinte da Shiro FM!");
				eb.setFooter("Se gostou das minhas funções, não deixe de votar em https://top.gg/bot/572413282653306901");

				channel.sendMessage(eb.build()).queue(null, Helper::doNothing);
			}
		}
	}

	/**
	 * Clear the queue.
	 */
	public void clear() {
		queue.clear();
	}
}
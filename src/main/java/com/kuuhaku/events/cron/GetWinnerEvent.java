package com.kuuhaku.events.cron;

import com.kuuhaku.Main;
import com.kuuhaku.controller.MySQL;
import com.kuuhaku.utils.Helper;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class GetWinnerEvent implements Job {
	public static JobDetail refreshWinner;

	@Override
	public void execute(JobExecutionContext context) {
		Main.getInfo().setWinner(MySQL.getWinner());
		Helper.logger(this.getClass()).info("Atualizado vencedor mensal.");
	}
}

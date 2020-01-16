package com.kuuhaku.handlers.api.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuuhaku.controller.mysql.TokenDAO;
import com.kuuhaku.controller.sqlite.MemberDAO;
import com.kuuhaku.handlers.api.exception.InvalidTokenException;
import com.kuuhaku.model.Member;
import com.kuuhaku.utils.Helper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class MemberRequest {

	@RequestMapping(value = "/member/get", method = RequestMethod.GET)
	public Member requestProfileById(@RequestParam(value = "id") String id) {
		return MemberDAO.getMemberById(id);
	}

	@RequestMapping(value = "/member/get", method = RequestMethod.GET)
	public Member[] requestProfileByMid(@RequestParam(value = "mid") String mid) {
		return (Member[]) MemberDAO.getMemberByMid(mid).toArray();
	}

	@RequestMapping(value = "/member/get", method = RequestMethod.GET)
	public Member[] requestProfileBySid(@RequestParam(value = "sid") String sid) {
		return (Member[]) MemberDAO.getMemberBySid(sid).toArray();
	}

	@RequestMapping(value = "/member/auth", method = RequestMethod.POST)
	public Member[] authProfile(@RequestHeader(value = "login") String login, @RequestHeader(value = "password") String pass) {
		Helper.logger(this.getClass()).info(login + " - " + pass);

		return (Member[]) MemberDAO.authMember(login, pass).toArray();
	}

	@RequestMapping(value = "/member/update", method = RequestMethod.POST)
	public void updateProfile(@RequestHeader String profile, @RequestHeader String token) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			Member m = mapper.readValue(profile, Member.class);

			if (TokenDAO.validateToken(token)) {
				MemberDAO.updateMemberConfigs(m);
			} else {
				throw new InvalidTokenException();
			}
		} catch (IOException e) {
			Helper.logger(this.getClass()).error(e + " | " + e.getStackTrace()[0]);
		}
	}
}

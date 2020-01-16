package com.kuuhaku.handlers.api.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuuhaku.controller.mysql.TokenDAO;
import com.kuuhaku.controller.sqlite.MemberDAO;
import com.kuuhaku.handlers.api.exception.InvalidTokenException;
import com.kuuhaku.handlers.api.exception.UnauthorizedException;
import com.kuuhaku.model.Member;
import com.kuuhaku.utils.Helper;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class MemberRequest {

	@RequestMapping(value = "/member/get", method = RequestMethod.GET)
	public Member requestProfile(@RequestParam(value = "id") String id) {
		return MemberDAO.getMemberById(id);
	}

	@RequestMapping(value = "/member/auth", method = RequestMethod.POST)
	public Member authProfile(@RequestBody String body) {
		JSONObject json = new JSONObject(body);

		if (!json.has("login") || !json.has("password")) throw new UnauthorizedException();

		Helper.logger(this.getClass()).info(json.getString("login") + " - " + json.getString("password"));

		return MemberDAO.authMember(json.getString("login"), json.getString("password"));
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

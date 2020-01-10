package com.kuuhaku.handlers.api.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuuhaku.controller.mysql.TokenDAO;
import com.kuuhaku.controller.sqlite.MemberDAO;
import com.kuuhaku.handlers.api.exception.InvalidTokenException;
import com.kuuhaku.model.Member;
import com.kuuhaku.utils.Helper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberRequest {

	@RequestMapping(value = "/member/get", method = RequestMethod.GET)
	public Member requestProfile(@RequestParam(value = "id") String id) {
		return MemberDAO.getMemberById(id);
	}

	@RequestMapping(value = "/member/update", method = RequestMethod.POST)
	public void updateProfile(@RequestParam(value = "profile") String profile, @RequestParam(value = "token") String token) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			Member m = mapper.readValue(profile, Member.class);

			if (TokenDAO.validateToken(token)) {
				MemberDAO.updateMemberConfigs(m);
			} else {
				throw new InvalidTokenException();
			}
		} catch (JsonProcessingException e) {
			Helper.logger(this.getClass()).error(e + " | " + e.getStackTrace()[0]);
		}
	}
}

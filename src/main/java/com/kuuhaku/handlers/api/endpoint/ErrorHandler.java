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

package com.kuuhaku.handlers.api.endpoint;

import com.kuuhaku.handlers.api.exception.Exception;
import com.kuuhaku.handlers.api.exception.InvalidTokenException;
import com.kuuhaku.handlers.api.exception.NotEnoughArgsException;
import com.kuuhaku.handlers.api.exception.UnauthorizedException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.persistence.NoResultException;

@RestController
@ControllerAdvice
public class ErrorHandler implements ErrorController {
	@RequestMapping("/error")
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Exception error(Throwable t) {
		t.printStackTrace();
		return new Exception(500, "Internal server error");
	}

	@ExceptionHandler(InvalidTokenException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Exception invalidToken() {
		return new Exception(403, "Invalid token");
	}

	@ExceptionHandler(NoResultException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Exception noResult() {
		return new Exception(404, "No entity found with that ID");
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Exception unauthorized() {
		return new Exception(403, "Access not authorized");
	}

	@ExceptionHandler(NotEnoughArgsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Exception notEnoughArgs() {
		return new Exception(400, "Not enough arguments were given to this request");
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}

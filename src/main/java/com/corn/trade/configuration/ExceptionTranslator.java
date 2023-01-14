/* planner
 * Copyleft (C) 2022  Cornknight
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityNotFoundException;

@SuppressWarnings("unused")
@ControllerAdvice
@RequestMapping(produces = "application/json")
@ResponseBody
public class ExceptionTranslator {

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Error> notFoundException(final EntityNotFoundException e) {
		Error error = Error.ErrorBuilder
				.anError()
				.withMessage(e.getMessage())
				.withStatus(HttpStatus.NOT_FOUND.value())
				.build();
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Error> illegalArgumentException(final IllegalArgumentException e) {
		Error error = Error.ErrorBuilder
				.anError()
				.withMessage(e.getMessage())
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.build();
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
}

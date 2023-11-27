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
package com.corn.trade.web.configuration;

@SuppressWarnings("unused")
public class Error {
	private final int status;
	private final String message;

	private Error(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public static final class ErrorBuilder {
		private int status;
		private String message;

		private ErrorBuilder() {
		}

		public static ErrorBuilder anError() {
			return new ErrorBuilder();
		}

		public ErrorBuilder withStatus(int status) {
			this.status = status;
			return this;
		}

		public ErrorBuilder withMessage(String message) {
			this.message = message;
			return this;
		}

		public Error build() {
			return new Error(status, message);
		}
	}
}

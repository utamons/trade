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
package com.corn.trade.controller;

import com.corn.trade.service.TradeLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TradeLogController {

	private final TradeLogService service;
	private final Logger          log = LoggerFactory.getLogger(TradeLogController.class);


	public TradeLogController(TradeLogService service) {
		this.service = service;
	}

	@GetMapping("/tradelog/all")
	public String all() throws InterruptedException {
		return "service.getAll()";
	}

	/*@PostMapping("/tradelog")
	@ResponseStatus(HttpStatus.CREATED)
	public Long add(@RequestBody TradeLogDTO item) {
		return service.addItem(item).getId();
	}

	@DeleteMapping("/tradelog/{id}")
	public void delete(@PathVariable("id") Long id) {
		log.debug("REST request to delete item {}", id);
		service.deleteItem(id);
	}*/
}

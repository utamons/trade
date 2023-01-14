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

import com.corn.trade.dto.TestItemDTO;
import com.corn.trade.exception.ValidationException;
import com.corn.trade.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TestController {

	private final TestService service;
	private final Logger      log = LoggerFactory.getLogger(TestController.class);


	public TestController(TestService service) {
		this.service = service;
	}

	@GetMapping("/test/all")
	public List<TestItemDTO> all() throws InterruptedException {
		log.debug("REST request to get test items");
		return service.getAll();
	}

	@GetMapping("/test/{id}")
	public TestItemDTO get(@PathVariable("id") long id) throws InterruptedException {
		log.debug("REST request to get test items");
		return service.find(id);
	}

	@PostMapping("/test")
	@ResponseStatus(HttpStatus.CREATED)
	public Long add(@RequestBody TestItemDTO item) {
		log.debug("REST request to add test item {}", item.getText());
		return service.addItem(item).getId();
	}

	@PutMapping("/test")
	public void update(@RequestBody TestItemDTO item) throws ValidationException {
		log.debug("REST request to update test item");
		if (item == null || item.getId() == null) {
			throw new ValidationException("Item should be present and have an id");
		}
		TestItemDTO result = service.updateItem(item);
	}

	@DeleteMapping("/test/{id}")
	public void delete(@PathVariable("id") Long id) {
		log.debug("REST request to delete test item {}", id);
		service.deleteItem(id);
	}
}

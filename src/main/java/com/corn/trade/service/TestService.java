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
package com.corn.trade.service;

import com.corn.trade.dto.TestItemDTO;
import com.corn.trade.entity.TestItem;
import com.corn.trade.repository.TestRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestService {

	private final TestRepository repo;

	public TestService(TestRepository repo) {
		this.repo = repo;
	}

	public List<TestItemDTO> getAll() {
		return TestItemDTO.toDtoList(repo.findAll());
	}

	public TestItem addItem(TestItemDTO dto) {
		List<TestItem> all  = repo.findAll();
		TestItem       item = TestItemDTO.toEntity(dto);
		return repo.save(item);
	}

	public TestItemDTO updateItem(TestItemDTO dto) {
		TestItem item = repo.getReferenceById(dto.getId());
		item.setText(dto.getText());
		return TestItemDTO.toDto(repo.save(item));
	}

	public void deleteItem(Long id) {
		repo.deleteById(id);
	}

	public TestItemDTO find(long id) {
		TestItem old = repo.getReferenceById(id);

		return TestItemDTO.toDto(old);
	}
}

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
package com.corn.trade.dto;

import com.corn.trade.entity.TestItem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TestItemDTO {
	final private Long id;
	final private String text;

	@JsonCreator
	public TestItemDTO(
			@JsonProperty("id")
			Long id,
			@JsonProperty("text")
			String text) {
		this.id = id;
		this.text = text;
	}

	public Long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public static TestItem toEntity(TestItemDTO dto) {
		TestItem item = new TestItem();
		item.setId(dto.getId());
		item.setText(dto.getText());
		return item;
	}

	public static TestItemDTO toDto(TestItem item) {
		if (item == null)
			return null;
		return new TestItemDTO(item.getId(), item.getText());
	}

	public static List<TestItemDTO> toDtoList(List<TestItem> items) {
		List<TestItemDTO> result = new ArrayList<>();
		items.forEach((item -> result.add(toDto(item))));
		return result;
	}
}

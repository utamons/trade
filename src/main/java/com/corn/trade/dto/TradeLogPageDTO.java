package com.corn.trade.dto;

import java.io.Serializable;
import java.util.List;

public class TradeLogPageDTO implements Serializable {

	private static final long serialVersionUID = 3382840619524533088L;

	private final int total;

	private final int pageSize;

	private final int skip;

	private final List<TradeLogDTO> page;

	public TradeLogPageDTO(int total, int pageSize, int skip, List<TradeLogDTO> page) {
		this.total = total;
		this.pageSize = pageSize;
		this.skip = skip;
		this.page = page;
	}

	public int getTotal() {
		return total;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getSkip() {
		return skip;
	}

	public List<TradeLogDTO> getPage() {
		return page;
	}
}

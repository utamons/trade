/*
	Trade
    Copyright (C) 2024  Cornknight

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.ui.component;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ResizeListener extends MouseAdapter {
	private final JFrame frame;
	private int prevX, prevY;
	private boolean dragging = false;
	private ResizeDirection resizeDir = ResizeDirection.NONE;

	private final int edge; // Distance from the edge to consider as a resize area

	private enum ResizeDirection {
		NONE, NORTH, SOUTH, EAST, WEST, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST
	}

	public ResizeListener(JFrame frame, int edge) {
		this.edge = edge;
		this.frame = frame;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		prevX = e.getXOnScreen();
		prevY = e.getYOnScreen();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dragging = false;
		frame.setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		updateResizeDirection(e);
		setCursor();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (dragging && resizeDir != ResizeDirection.NONE) {
			int dx = e.getXOnScreen() - prevX;
			int dy = e.getYOnScreen() - prevY;

			Dimension dim = frame.getSize();
			Point loc = frame.getLocation();

			switch (resizeDir) {
				case SOUTH_EAST:
					dim.width += dx;
					dim.height += dy;
					break;
				case SOUTH:
					dim.height += dy;
					break;
				case EAST:
					dim.width += dx;
					break;
				case NORTH_WEST:
					dim.width -= dx;
					dim.height -= dy;
					loc.x += dx;
					loc.y += dy;
					break;
				case NORTH:
					dim.height -= dy;
					loc.y += dy;
					break;
				case WEST:
					dim.width -= dx;
					loc.x += dx;
					break;
				case NORTH_EAST:
					dim.width += dx;
					dim.height -= dy;
					loc.y += dy;
					break;
				case SOUTH_WEST:
					dim.width -= dx;
					dim.height += dy;
					loc.x += dx;
					break;
				default:
					break;
			}

			frame.setSize(dim);
			frame.setLocation(loc);

			prevX = e.getXOnScreen();
			prevY = e.getYOnScreen();
		} else {
			dragging = true;
			updateResizeDirection(e);
		}
	}

	private void updateResizeDirection(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int width = frame.getWidth();
		int height = frame.getHeight();

		boolean north = y < edge;
		boolean south = y > height - edge;
		boolean east = x > width - edge;
		boolean west = x < edge;

		if (north && east) resizeDir = ResizeDirection.NORTH_EAST;
		else if (north && west) resizeDir = ResizeDirection.NORTH_WEST;
		else if (south && east) resizeDir = ResizeDirection.SOUTH_EAST;
		else if (south && west) resizeDir = ResizeDirection.SOUTH_WEST;
		else if (north) resizeDir = ResizeDirection.NORTH;
		else if (south) resizeDir = ResizeDirection.SOUTH;
		else if (east) resizeDir = ResizeDirection.EAST;
		else if (west) resizeDir = ResizeDirection.WEST;
		else resizeDir = ResizeDirection.NONE;
	}

	private void setCursor() {
		Cursor cursor = switch (resizeDir) {
			case NORTH, SOUTH -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
			case EAST, WEST -> Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
			case NORTH_EAST, SOUTH_WEST -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
			case NORTH_WEST, SOUTH_EAST -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
			default -> Cursor.getDefaultCursor();
		};
		frame.setCursor(cursor);
	}
}

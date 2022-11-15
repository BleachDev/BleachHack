/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class OperationList {

	public List<Operation> operations;
	private int index = 0;

	public OperationList(Operation... operations) {
		this.operations = new ArrayList<>(Arrays.asList(operations));
	}

	public OperationList(List<Operation> operations) {
		this.operations = new ArrayList<>();
		this.operations.addAll(operations);
	}

	public List<Operation> getRemainingOps() {
		return operations.subList(index, operations.size());
	}

	public static OperationList create(List<OperationBlueprint> blueprints, BlockPos pos, Direction dir) {
		List<Operation> op = new ArrayList<>();

		for (OperationBlueprint o: blueprints) {
			op.add(o.create(pos, dir));
		}

		return new OperationList(op);
	}

	public boolean executeNext() {
		if (!isDone() && operations.get(index).canExecute() && operations.get(index).execute()) {
			index++;
			return true;
		}

		return false;
	}

	public Operation getNext() {
		return operations.get(index);
	}

	public Box getBox() {
		Box box = null;

		for (Operation o: operations) {
			if (box == null) {
				box = new Box(o.pos);
			} else {
				box = new Box(Math.min(box.minX, o.pos.getX()), Math.min(box.minY, o.pos.getY()), Math.min(box.minZ, o.pos.getZ()),
						Math.max(box.maxX, o.pos.getX() + 1), Math.max(box.maxY, o.pos.getY() + 1), Math.max(box.maxZ, o.pos.getZ() + 1));
			}
		}

		return box;
	}

	public boolean isDone() {
		return index >= operations.size();
	}
}

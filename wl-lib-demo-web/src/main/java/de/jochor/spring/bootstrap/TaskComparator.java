package de.jochor.spring.bootstrap;

import java.util.Comparator;

import lombok.RequiredArgsConstructor;
import de.jochor.lib.wunderlist.model.Positions;
import de.jochor.lib.wunderlist.model.Task;

@RequiredArgsConstructor
public class TaskComparator implements Comparator<Task> {

	private final Positions taskPositions;

	@Override
	public int compare(Task task1, Task task2) {
		int id1 = task1.getId();
		int id2 = task2.getId();
		int[] values = taskPositions.getValues();

		for (int id : values) {
			if (id == id1) {
				return -1;
			}
			if (id == id2) {
				return 1;
			}
		}

		return 0;
	}

}
package de.jochor.spring.bootstrap;

import java.util.Comparator;

import lombok.RequiredArgsConstructor;
import de.jochor.lib.wunderlist.model.List;
import de.jochor.lib.wunderlist.model.Positions;

@RequiredArgsConstructor
public class ListComparator implements Comparator<List> {

	private final Positions taskPositions;

	@Override
	public int compare(List list1, List list2) {
		int id1 = list1.getId();
		int id2 = list2.getId();
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
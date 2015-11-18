package de.jochor.spring.bootstrap;

import java.util.HashMap;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.jochor.lib.wunderlist.api.ListService;
import de.jochor.lib.wunderlist.api.PositionsService;
import de.jochor.lib.wunderlist.api.TaskService;
import de.jochor.lib.wunderlist.model.Authorization;
import de.jochor.lib.wunderlist.model.List;
import de.jochor.lib.wunderlist.model.Positions;
import de.jochor.lib.wunderlist.model.Task;

/**
 *
 * <p>
 * <b>Started:</b> 2015-10-25
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoAppApplication.class)
@WebAppConfiguration
public class OnlineTest {

	@Inject
	private Environment env;

	@Inject
	private ListService listService;

	@Inject
	private PositionsService positionsService;

	@Inject
	private TaskService taskService;

	private Authorization authorization;

	private String title = "Testing";

	private int listID = 182730956;

	@Before
	public void setUp() {
		authorization = new Authorization();
		authorization.setClientId(env.getProperty("wunderlist.client.id"));
		authorization.setUserToken(env.getProperty("wunderlist.my.accesstoken"));
	}

	@Ignore
	@Test
	public void testRetrieveAllLists() {
		List[] allLists = listService.retrieveAll(authorization);

		System.out.println("There are " + allLists.length + " lists.");
		System.out.println("Looking for the list '" + title + "'...");

		List testList = null;
		for (List item : allLists) {
			if (title.equals(item.getTitle())) {
				testList = item;
				break;
			}
		}

		System.out.println("Found it! The list ID is '" + testList.getId() + "'.");
	}

	@Ignore
	@Test
	public void testRetrieveListContent() {
		Task[] tasks = taskService.retrieveAll(listID, authorization);
		Positions[] positionsArray = positionsService.retrieveAllTaskPositions(listID, authorization);
		if (positionsArray == null || positionsArray.length != 1) {
			throw new IllegalStateException();
		}

		Positions positions = positionsArray[0];

		HashMap<Integer, Task> idToTaskMap = toMap(tasks);
		int[] tasksIDs = positions.getValues();

		System.out.println("Tasks of list '" + title + "' in order:");
		for (int id : tasksIDs) {
			Task task = idToTaskMap.get(id);
			System.out.println(id + " - " + task.getTitle());
		}
	}

	@Ignore
	@Test
	public void testShuffelListContent() {
		Positions[] positionsArray = positionsService.retrieveAllTaskPositions(listID, authorization);
		if (positionsArray == null || positionsArray.length != 1) {
			throw new IllegalStateException();
		}
		Positions positions = positionsArray[0];

		int[] tasksIDs = positions.getValues();

		for (int i = 0; i < tasksIDs.length; i++) {
			int id = tasksIDs[i++];
			if (i == tasksIDs.length) {
				break;
			}

			tasksIDs[i - 1] = tasksIDs[i];
			tasksIDs[i] = id;
		}

		positionsService.updateTaskPositions(positions.getId(), tasksIDs, positions.getRevision(), authorization);
	}

	private HashMap<Integer, Task> toMap(Task[] tasks) {
		HashMap<Integer, Task> idToTaskMap = new HashMap<Integer, Task>();

		for (Task task : tasks) {
			idToTaskMap.put(task.getId(), task);
		}

		return idToTaskMap;
	}

}

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

import de.jochor.lib.wunderlist.model.Authorization;
import de.jochor.lib.wunderlist.model.RetrieveListPositionsResponse;
import de.jochor.lib.wunderlist.model.RetrieveListResponse;
import de.jochor.lib.wunderlist.model.Task;
import de.jochor.lib.wunderlist.service.ListService;
import de.jochor.lib.wunderlist.service.PositionsService;
import de.jochor.lib.wunderlist.service.TaskService;

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
		RetrieveListResponse[] allLists = listService.retrieveAll(authorization);

		System.out.println("There are " + allLists.length + " lists.");
		System.out.println("Looking for the list '" + title + "'...");

		RetrieveListResponse testList = null;
		for (RetrieveListResponse item : allLists) {
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
		RetrieveListPositionsResponse listPositions = positionsService.retrieve(listID, authorization);

		HashMap<Integer, Task> idToTaskMap = toMap(tasks);
		int[] tasksIDs = listPositions.getValues();

		System.out.println("Tasks of list '" + title + "' in order:");
		for (int id : tasksIDs) {
			Task task = idToTaskMap.get(Integer.valueOf(id));
			System.out.println(id + " - " + task.getTitle());
		}
	}

	private HashMap<Integer, Task> toMap(Task[] tasks) {
		HashMap<Integer, Task> idToTaskMap = new HashMap<Integer, Task>();

		for (Task task : tasks) {
			Integer id = Integer.valueOf(task.getId());
			idToTaskMap.put(id, task);
		}

		return idToTaskMap;
	}

}

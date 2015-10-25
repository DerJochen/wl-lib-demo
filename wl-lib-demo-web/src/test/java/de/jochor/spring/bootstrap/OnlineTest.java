package de.jochor.spring.bootstrap;

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
import de.jochor.lib.wunderlist.model.RetrieveListResponse;
import de.jochor.lib.wunderlist.service.ListService;

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

	private Authorization authorization;

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

		String title = "Testing";

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
		int listID = 182730956;
	}

}

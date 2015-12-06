package de.jochor.spring.bootstrap;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import de.jochor.lib.wunderlist.api.ListService;
import de.jochor.lib.wunderlist.api.PositionsService;
import de.jochor.lib.wunderlist.api.TaskService;
import de.jochor.lib.wunderlist.model.Authorization;
import de.jochor.lib.wunderlist.model.List;
import de.jochor.lib.wunderlist.model.Positions;
import de.jochor.lib.wunderlist.model.Task;
import de.jochor.lib.wunderlist.service.ListComparator;
import de.jochor.lib.wunderlist.service.TaskComparator;

@WebServlet("demo")
public class DemoServlet extends HttpServlet {

	private static final long serialVersionUID = 3641115279664025510L;

	private static final Logger logger = LoggerFactory.getLogger(DemoServlet.class);

	@Inject
	private transient Environment env;

	@Inject
	private transient ListService listService;

	@Inject
	private transient TaskService taskService;

	@Inject
	private transient PositionsService positionsService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setCharacterEncoding(StandardCharsets.UTF_8.name());
		res.setContentType("text/html");

		Optional<Authorization> authorizationOpt = getAuthorization(req);
		if (!authorizationOpt.isPresent()) {
			// TODO redirect to login page
		}
		Authorization authorization = authorizationOpt.get();

		String lString = req.getParameter("l");
		String tString = req.getParameter("t");

		try (Writer writer = res.getWriter()) {
			int selectedListId = lString == null ? 0 : Integer.parseInt(lString);
			int selectedTaskId = tString == null ? 0 : Integer.parseInt(tString);

			String pageHTML = renderHTML(selectedListId, selectedTaskId, authorization);
			writer.append(pageHTML);
		} catch (Exception e) {
			logger.error("Exception durin request", e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private Optional<Authorization> getAuthorization(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return Optional.empty();
		}

		String clientID = env.getProperty("wunderlist.client.id");
		String accessToken = env.getProperty("wunderlist.my.accesstoken");

		Authorization authorization = new Authorization();

		authorization.setClientId(clientID);
		authorization.setUserToken(accessToken);

		return Optional.of(authorization);
	}

	private String renderHTML(int selectedListId, int selectedTaskId, Authorization authorization) {
		String listsHTML = renderListsHTML(selectedListId, authorization);
		String tasksHTML = renderTasksHTML(selectedTaskId, selectedListId, authorization);

		String pageHTML = String.format(DemoServlet.pageHTML, listsHTML, tasksHTML);

		return pageHTML;
	}

	private String renderListsHTML(int selectedListId, Authorization authorization) {
		List[] allLists = listService.retrieveAll(authorization);
		Positions[] allListPositions = positionsService.retrieveAllListPositions(authorization);
		Positions listPositions = allListPositions[0];

		Arrays.sort(allLists, new ListComparator(listPositions));

		StringBuilder sb = new StringBuilder();

		for (List list : allLists) {
			String listHTML = String.format(DemoServlet.listHTML, list.getId(), list.getTitle());
			sb.append(listHTML);
		}

		String listsHTML = String.format(DemoServlet.listsHTML, sb.toString());
		return listsHTML;
	}

	private String renderTasksHTML(int selectedTaskId, int selectedListId, Authorization authorization) {
		StringBuilder sb = new StringBuilder();

		if (selectedListId != 0) {
			Task[] allTasks = taskService.retrieveAll(selectedListId, authorization);
			Positions[] allTaskPositions = positionsService.retrieveAllTaskPositions(selectedListId, authorization);
			Positions taskPositions = allTaskPositions[0];

			Arrays.sort(allTasks, new TaskComparator(taskPositions));

			for (Task task : allTasks) {
				String listHTML = String.format(DemoServlet.taskHTML, selectedListId, task.getId(), task.getTitle());
				sb.append(listHTML);
			}
		}

		String listsHTML = String.format(DemoServlet.tasksHTML, sb.toString());
		return listsHTML;
	}

	private static final String pageHTML = "<html><head><title>Demo App</title></head><body><h2>Lists</h2>%s<h2>Tasks</h2>%s</body></html>";

	private static final String listsHTML = "<div>%s</div>";

	private static final String listHTML = "<div><a href=\"?l=%d\">%s</a></div>";

	private static final String tasksHTML = "<div>%s</div>";

	private static final String taskHTML = "<div><a href=\"?l=%d&t=%d\">%s</a></div>";

}

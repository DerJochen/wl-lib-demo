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

@WebServlet({ "demo", "login" })
public class DemoServlet extends HttpServlet {

	private static final long serialVersionUID = 3641115279664025510L;

	private static final Logger logger = LoggerFactory.getLogger(DemoServlet.class);

	private static final String LOGIN_HTML = "<div>" //
			+ "<form method=\"POST\">" //
			+ "<span><label for=\"username\">Username: </span><input type=\"text\" id=\"username\" name=\"username\" />" //
			+ "<span><label for=\"password\">Password: </span><input type=\"password\" id=\"password\" name=\"password\" />" //
			+ "</form>" //
			+ "</div>";

	private static final String pageHTML = "<html><head><title>Demo App</title></head><body><h2>Lists</h2>%s<h2>Tasks</h2>%s</body></html>";

	private static final String listsHTML = "<div>%s</div>";

	private static final String listHTML = "<div><a href=\"?l=%d\">%s</a></div>";

	private static final String tasksHTML = "<div>%s</div>";

	private static final String taskHTML = "<div><a href=\"?l=%d&t=%d\">%s</a></div>";

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
			renderLoginHTML(res);
			return;
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
			logger.error("Exception during request", e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession session = req.getSession();
		if (!session.isNew()) {
			session.invalidate();
			session = req.getSession();
		}

		String username = req.getParameter("username");
		String password = req.getParameter("password");

		String realPassword = env.getProperty("app.user." + username + ".password");
		if (realPassword == null || !realPassword.equals(password)) {
			renderLoginHTML(res);
			return;
		}

		String clientID = env.getProperty("wunderlist.client.id");
		String accessToken = env.getProperty("app.user." + username + ".wunderlist.accesstoken");

		Authorization authorization = new Authorization();

		authorization.setClientId(clientID);
		authorization.setUserToken(accessToken);

		session.setAttribute("authorization", authorization);

		res.sendRedirect(req.getServletPath());
	}

	private Optional<Authorization> getAuthorization(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return Optional.empty();
		}

		Object object = session.getAttribute("authorization");
		if (object == null || !(object instanceof Authorization)) {
			return Optional.empty();
		}

		Authorization authorization = (Authorization) object;

		return Optional.of(authorization);
	}

	private void renderLoginHTML(HttpServletResponse res) throws IOException {
		try (Writer writer = res.getWriter()) {
			writer.write(LOGIN_HTML);
		} catch (IOException e) {
			logger.error("Exception during request", e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
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

}

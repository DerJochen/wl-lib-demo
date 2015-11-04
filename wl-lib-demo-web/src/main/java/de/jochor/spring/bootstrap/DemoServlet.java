package de.jochor.spring.bootstrap;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;

import de.jochor.lib.wunderlist.api.ListService;
import de.jochor.lib.wunderlist.model.Authorization;
import de.jochor.lib.wunderlist.model.List;
import de.jochor.lib.wunderlist.model.Task;

@WebServlet("demo")
public class DemoServlet extends HttpServlet {

	private static final long serialVersionUID = 3641115279664025510L;

	@Inject
	private Environment env;

	@Inject
	private ListService listService;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setCharacterEncoding(StandardCharsets.UTF_8.name());
		res.setContentType("text/html");

		String clientID = env.getProperty("wunderlist.client.id");
		String accessToken = env.getProperty("wunderlist.my.accesstoken");
		Authorization authorization = new Authorization();
		authorization.setClientId(clientID);
		authorization.setUserToken(accessToken);

		List[] lists = listService.retrieveAll(authorization);
		ArrayList<List> allLists = new ArrayList<>(Arrays.asList(lists));
		int selectedListId = 0;
		ArrayList<Task> allTasks = null;
		int selectedTaskId = 0;

		try (OutputStream out = res.getOutputStream()) {
			String pageHTML = renderHTML(allLists, selectedListId, allTasks, selectedTaskId);
			out.write(pageHTML.getBytes(StandardCharsets.UTF_8));
		}
	}

	private String renderHTML(ArrayList<List> allLists, int selectedListId, ArrayList<Task> allTasks, int selectedTaskId) {
		// TODO Auto-generated method stub

		String listsHTML = renderListsHTML(allLists, selectedListId);

		String pageHTML = String.format(DemoServlet.pageHTML, listsHTML);

		return pageHTML;
	}

	private String renderListsHTML(ArrayList<List> allLists, int selectedListId) {
		StringBuilder sb = new StringBuilder();

		for (List list : allLists) {
			String listHTML = String.format(DemoServlet.listHTML, list.getId(), list.getTitle());
			sb.append(listHTML);
		}

		String listsHTML = String.format(DemoServlet.listsHTML, sb.toString());
		return listsHTML;
	}

	private static final String pageHTML = "<html><head><title>Demo App</title></head><body>%s</body></html>";

	private static final String listsHTML = "<div>%s</div>";

	private static final String listHTML = "<div><a href=\"list/%d\">%s</a></div>";

	private static final String tasksHTML = "<div>%s</div>";

	private static final String taskHTML = "<div><a href=\"task/%d\">%s</a></div>";

}

package de.jochor.spring.bootstrap;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Principal;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;

import de.jochor.lib.wunderlist.service.AuthorizationService;

/**
 *
 * <p>
 * <b>Started:</b> 2015-09-05
 * </p>
 *
 * @author Jochen Hormes
 */
@WebServlet("/api/authorization")
public class AuthorizationServlet extends HttpServlet {

	private static final long serialVersionUID = -2023592494497037249L;

	@Inject
	private Environment env;

	@Inject
	private AuthorizationService authorizationService;

	@RequestMapping("/wunderlist-authorization")
	public void authorization(HttpServletRequest req, Principal user) {
		HttpSession session = req.getSession(false);

		// TODO check session != null
		// TODO check 'state' from req and from session are equal

		String code = req.getParameter("code");
		String accessToken = getAccessToken(code);

		try {
			storeAccessToken(user, accessToken);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

		// TODO redirect to /home.html
	}

	protected String getAccessToken(String code) {
		String clientId = env.getProperty("wunderlist.client.id");
		String clientSecrete = env.getProperty("wunderlist.client.secrete");

		String accessToken = authorizationService.retrieveAccessToken(clientId, clientSecrete, code);

		return accessToken;
	}

	// Temporary solution for library testing
	protected void storeAccessToken(Principal user, String accessToken) throws IOException {
		String homeDir = System.getProperty("user.home");
		Path storagePath = Paths.get(homeDir, ".wl-lib-demo", "storage.properties");

		Properties properties = new Properties();

		if (Files.exists(storagePath)) {
			try (Reader storageReader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
				properties.load(storageReader);
			}
		}

		properties.setProperty("user." + user.getName() + ".accessToken", accessToken);

		Files.createDirectories(storagePath.getParent());
		try (Writer storageWriter = Files.newBufferedWriter(storagePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			properties.store(storageWriter, "Dummy storage");
		}
	}

}

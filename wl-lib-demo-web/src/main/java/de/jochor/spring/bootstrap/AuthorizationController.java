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
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.jochor.lib.wunderlist.service.AuthorizationService;

/**
 *
 * <p>
 * <b>Started:</b> 2015-10-18
 * </p>
 *
 * @author Jochen Hormes
 */
@RestController
public class AuthorizationController {

	@Inject
	private Environment env;

	@Inject
	private AuthorizationService authorizationService;

	@RequestMapping(value = "/api/request-authorization", method = RequestMethod.GET)
	public void requestAuthorization(HttpServletRequest req, HttpServletResponse res, Principal user) throws IOException {
		String state = UUID.randomUUID().toString();
		HttpSession session = req.getSession();
		session.setAttribute("state", state);

		String clientId = env.getProperty("wunderlist.client.id");
		String callbackURL = env.getProperty("url.base") + env.getProperty("url.auth.wl.callback");
		String authorisationRequestURL = authorizationService.buildAuthorisationRequestURL(clientId, callbackURL, state);
		res.sendRedirect(authorisationRequestURL);
	}

	@RequestMapping(value = "/api/receive-authorization", method = RequestMethod.GET)
	public void receiveAuthorization(HttpServletRequest req, HttpServletResponse res, Principal user) throws IOException {
		HttpSession session = req.getSession(false);

		String state = req.getParameter("state");
		if (session == null || state == null || !state.equals(session.getAttribute("state"))) {
			res.setStatus(HttpStatus.FORBIDDEN.value());
			return;
		}

		String code = req.getParameter("code");
		String accessToken = getAccessToken(code);

		try {
			storeAccessToken(user, accessToken);
		} catch (IOException e) {
			res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return;
		}

		String successRedirectAddress = env.getProperty("url.home");
		res.sendRedirect(successRedirectAddress);
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

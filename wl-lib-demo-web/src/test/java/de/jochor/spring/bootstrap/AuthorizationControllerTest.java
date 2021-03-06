package de.jochor.spring.bootstrap;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.jochor.lib.http4j.StaticHTTPClientBinder;
import de.jochor.lib.http4j.junit.HTTPClientJUnit;
import de.jochor.lib.servicefactory.ServiceFactory;
import de.jochor.lib.wunderlist.service.DefaultURIProvider;
import de.jochor.lib.wunderlist.service.URIProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoAppApplication.class)
@WebAppConfiguration
public class AuthorizationControllerTest {

	private static Principal USER;

	static {
		// Explicitly selecting the jUnit implementation of the HTTPClient.
		System.setProperty(ServiceFactory.PROPERTIES_BASE + StaticHTTPClientBinder.class.getName(), HTTPClientJUnit.class.getName());
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		USER = new Principal() {

			@Override
			public String getName() {
				return "TestUser";
			}
		};
	}

	@Inject
	private Environment env;

	@Inject
	private AuthorizationController authorizationController;

	private Path testHomeDir;

	private Path storagePath;

	@Before
	public void setUp() throws Exception {
		testHomeDir = Paths.get("target", "test-home");
		storagePath = testHomeDir.resolve(".wl-lib-demo").resolve("storage.properties");
		Files.deleteIfExists(storagePath);
	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(storagePath);
	}

	@Test
	public void testRequestAuthorization() throws IOException {
		HttpSession session = Mockito.mock(HttpSession.class);

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getSession()).thenReturn(session);

		HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

		authorizationController.requestAuthorization(req, res, USER);

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(session).setAttribute(Matchers.eq("state"), argument.capture());

		URIProvider uriProvider = new DefaultURIProvider();
		String clientID = env.getProperty("wunderlist.client.id");
		String callback = env.getProperty("url.base") + env.getProperty("url.auth.callback");
		String state = argument.getValue();
		URI wunderlistRedirectURI = uriProvider.getAuthorizationRequestURI(clientID, callback, state);

		Mockito.verify(res).sendRedirect(wunderlistRedirectURI.toString());
	}

	@Test
	public void testReceiveAuthorization() throws IOException {
		String code = "code retrieved via OAuth2";
		String state = "unguessable random string";

		HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute("state")).thenReturn(state);

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getSession(false)).thenReturn(session);
		Mockito.when(req.getParameter("state")).thenReturn(state);
		Mockito.when(req.getParameter("code")).thenReturn(code);

		HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

		String accessToken = "testGetAccessToken";
		setUpGetAccessToken(code, accessToken);

		String homeDir = System.getProperty("user.home");
		System.setProperty("user.home", testHomeDir.toString());
		try {
			authorizationController.receiveAuthorization(req, res, USER);
		} finally {
			System.setProperty("user.home", homeDir);
		}

		String successRedirectAddress = env.getProperty("url.home");
		Assert.assertNotNull(successRedirectAddress);
		Assert.assertFalse(successRedirectAddress.isEmpty());

		Mockito.verify(res).sendRedirect(successRedirectAddress);
		verifyAccessTokenIsStored(USER, accessToken);
	}

	@Test
	public void testGetAccessToken() {
		String code = "code retrieved via OAuth2";
		String accessToken = "testGetAccessToken";
		setUpGetAccessToken(code, accessToken);

		String retrievedAccessToken = authorizationController.getAccessToken(code);

		Assert.assertEquals(accessToken, retrievedAccessToken);
	}

	@Test
	public void testStoreAccessToken() throws IOException {
		String accessToken = "test accessToken";

		String homeDir = System.getProperty("user.home");
		System.setProperty("user.home", testHomeDir.toString());
		try {
			authorizationController.storeAccessToken(USER, accessToken);
		} finally {
			System.setProperty("user.home", homeDir);
		}

		verifyAccessTokenIsStored(USER, accessToken);
	}

	@Test
	public void testStoreAccessToken_update() throws IOException {
		String accessToken = "test accessToken";
		String accessToken2 = "test accessToken 2";

		String homeDir = System.getProperty("user.home");
		System.setProperty("user.home", testHomeDir.toString());
		try {
			authorizationController.storeAccessToken(USER, accessToken);
			authorizationController.storeAccessToken(USER, accessToken2);
		} finally {
			System.setProperty("user.home", homeDir);
		}

		verifyAccessTokenIsStored(USER, accessToken2);
	}

	@Test
	public void testStoreAccessToken_secondUser() throws IOException {
		Principal user2 = new Principal() {

			@Override
			public String getName() {
				return "TestUser2";
			}

		};
		String accessToken = "test accessToken";
		String accessToken2 = "test accessToken 2";

		String homeDir = System.getProperty("user.home");
		System.setProperty("user.home", testHomeDir.toString());
		try {
			authorizationController.storeAccessToken(USER, accessToken);
			authorizationController.storeAccessToken(user2, accessToken2);
		} finally {
			System.setProperty("user.home", homeDir);
		}

		verifyAccessTokenIsStored(USER, accessToken);
		verifyAccessTokenIsStored(user2, accessToken2);
	}

	private void setUpGetAccessToken(String code, String accessToken) {
		HTTPClientJUnit.addResponse("{\"access_token\":\"" + accessToken + "\"}");
	}

	private void verifyAccessTokenIsStored(Principal user, String accessToken) throws IOException {
		Properties properties = new Properties();
		try (Reader storageReader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
			properties.load(storageReader);
		}

		Assert.assertEquals(accessToken, properties.get("user." + user.getName() + ".accessToken"));
	}

}

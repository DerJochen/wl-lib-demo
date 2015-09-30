package de.jochor.spring.bootstrap;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AuthorizationServletTest {

	private static Principal USER;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		USER = new Principal() {

			@Override
			public String getName() {
				return "TestUser";
			}
		};
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private AuthorizationServlet authorizationServlet;

	private Path testHomeDir;

	private Path storagePath;

	@Before
	public void setUp() throws Exception {
		authorizationServlet = new AuthorizationServlet();

		testHomeDir = Paths.get("target", "test-home");
		storagePath = testHomeDir.resolve(".wl-lib-demo").resolve("storage.properties");
		Files.deleteIfExists(storagePath);
	}

	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(storagePath);
	}

	@Test
	public void testAuthorization() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAccessToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testStoreAccessToken() throws IOException {
		String accessToken = "test accessToken";

		String homeDir = System.getProperty("user.home");
		System.setProperty("user.home", testHomeDir.toString());
		try {
			authorizationServlet.storeAccessToken(USER, accessToken);
		} finally {
			System.setProperty("user.home", homeDir);
		}

		Properties properties = new Properties();
		try (Reader storageReader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
			properties.load(storageReader);
		}

		Assert.assertEquals(accessToken, properties.get("user.TestUser.accessToken"));
	}

	@Test
	public void testStoreAccessToken_update() throws IOException {
		String accessToken = "test accessToken";
		String accessToken2 = "test accessToken 2";

		String homeDir = System.getProperty("user.home");
		System.setProperty("user.home", testHomeDir.toString());
		try {
			authorizationServlet.storeAccessToken(USER, accessToken);
			authorizationServlet.storeAccessToken(USER, accessToken2);
		} finally {
			System.setProperty("user.home", homeDir);
		}

		Properties properties = new Properties();
		try (Reader storageReader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
			properties.load(storageReader);
		}

		Assert.assertEquals(accessToken2, properties.get("user.TestUser.accessToken"));
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
			authorizationServlet.storeAccessToken(USER, accessToken);
			authorizationServlet.storeAccessToken(user2, accessToken2);
		} finally {
			System.setProperty("user.home", homeDir);
		}

		Properties properties = new Properties();
		try (Reader storageReader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
			properties.load(storageReader);
		}

		Assert.assertEquals(accessToken, properties.get("user.TestUser.accessToken"));
		Assert.assertEquals(accessToken2, properties.get("user.TestUser2.accessToken"));
	}

}

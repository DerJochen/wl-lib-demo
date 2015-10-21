package de.jochor.spring.bootstrap;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.springframework.core.env.Environment;

import de.jochor.lib.wunderlist.service.URIProvider;

/**
 *
 * <p>
 * <b>Started:</b> 2015-10-20
 * </p>
 *
 * @author Jochen Hormes
 */
public class DemoURIProvider implements URIProvider {

	@Inject
	private Environment env;

	// TODO think: URIs can be moved to the lib project. just the callback depends on the application

	@Override
	public URI getRequestAuthorizationURI(String clientID, String callback, String state) {
		String requestAuthorizationTpl = env.getProperty("url.auth.wl.redirect.tpl");
		try {
			String utf8 = StandardCharsets.UTF_8.name();
			String clientIDEnc = URLEncoder.encode(clientID, utf8);
			String callbackEnc = URLEncoder.encode(callback, utf8);
			String stateEnc = URLEncoder.encode(state, utf8);

			String requestAuthorization = String.format(requestAuthorizationTpl, clientIDEnc, callbackEnc, stateEnc);
			URI requestAuthorizationURI = URI.create(requestAuthorization);

			return requestAuthorizationURI;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public URI getAccessTokenURI() {
		String accessToken = env.getProperty("url.auth.wl.accesstoken");
		URI accessTokenURI = URI.create(accessToken);
		return accessTokenURI;
	}

	@Override
	public URI getWunderlistCallBackURI() {
		String callBack = env.getProperty("url.base") + env.getProperty("url.auth.wl.callback");
		URI callBackURI = URI.create(callBack);
		return callBackURI;
	}

}

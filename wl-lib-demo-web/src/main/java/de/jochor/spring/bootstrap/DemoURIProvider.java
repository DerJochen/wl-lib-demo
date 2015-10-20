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

	@Override
	public URI getRequestAuthorizationURI(String clientID, String callBack, String state) {
		String requestAuthorizationTpl = env.getProperty("url.auth.wl.redirect.tpl");
		String callBackEnc;
		try {
			callBackEnc = URLEncoder.encode(callBack, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		String requestAuthorization = String.format(requestAuthorizationTpl, clientID, callBackEnc, state);
		URI requestAuthorizationURI = URI.create(requestAuthorization);
		return requestAuthorizationURI;
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

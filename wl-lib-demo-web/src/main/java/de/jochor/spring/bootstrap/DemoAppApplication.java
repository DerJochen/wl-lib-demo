package de.jochor.spring.bootstrap;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.jochor.lib.wunderlist.service.AuthorizationService;
import de.jochor.lib.wunderlist.service.AuthorizationServiceImpl;

/**
 *
 * <p>
 * <b>Started:</b> 2015-09-28
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@SpringBootApplication
@ComponentScan({ "de.jochor.spring.bootstrap" })
@RestController
public class DemoAppApplication {

	@Configuration
	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.httpBasic().and() //
					.authorizeRequests().antMatchers("/index.html", "/home.html", "/login.html", "/").permitAll() //
					.anyRequest().authenticated().and() //
					.addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class) //
					.csrf().csrfTokenRepository(csrfTokenRepository());
		}

		private CsrfTokenRepository csrfTokenRepository() {
			HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
			repository.setHeaderName("X-XSRF-TOKEN");
			return repository;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoAppApplication.class, args);
	}

	@Bean
	public AuthorizationController authorizationController() {
		return new AuthorizationController();
	}

	@Bean
	public AuthorizationService authorizationService() {
		return new AuthorizationServiceImpl();
	}

	@RequestMapping("/user")
	public Principal user(Principal user) {
		return user;
	}

	@RequestMapping("/resource")
	public Map<String, Object> home() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("id", UUID.randomUUID().toString());
		model.put("content", "Hello World");
		return model;
	}

}

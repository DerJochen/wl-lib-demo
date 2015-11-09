package de.jochor.spring.bootstrap;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.Servlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	private static final Object[] sources = { DemoAppApplication.class, WunderLibConfig.class };

	public static void main(String[] args) {
		SpringApplication.run(sources, args);
	}

	@Bean(name = "demo")
	public Servlet demoServlet() {
		return new DemoServlet();
	}

	@Bean
	public AuthorizationController authorizationController() {
		return new AuthorizationController();
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

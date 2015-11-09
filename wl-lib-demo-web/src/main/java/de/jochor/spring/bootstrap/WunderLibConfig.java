package de.jochor.spring.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.jochor.lib.wunderlist.api.AuthorizationService;
import de.jochor.lib.wunderlist.api.ListService;
import de.jochor.lib.wunderlist.api.PositionsService;
import de.jochor.lib.wunderlist.api.TaskService;
import de.jochor.lib.wunderlist.api.WebhookService;
import de.jochor.lib.wunderlist.service.AuthorizationServiceImpl;
import de.jochor.lib.wunderlist.service.ListServiceImpl;
import de.jochor.lib.wunderlist.service.PositionsServiceImpl;
import de.jochor.lib.wunderlist.service.TaskServiceImpl;
import de.jochor.lib.wunderlist.service.WebhookServiceImpl;

/**
 *
 * <p>
 * <b>Started:</b> 2015-11-06
 * </p>
 *
 * @author Jochen Hormes
 *
 */
@Configuration
public class WunderLibConfig {

	@Bean
	public AuthorizationService authorizationService() {
		return new AuthorizationServiceImpl();
	}

	@Bean
	public ListService listService() {
		return new ListServiceImpl();
	}

	@Bean
	public PositionsService positionsService() {
		return new PositionsServiceImpl();
	}

	@Bean
	public TaskService taskService() {
		return new TaskServiceImpl();
	}

	@Bean
	public WebhookService webhookService() {
		return new WebhookServiceImpl();
	}

}

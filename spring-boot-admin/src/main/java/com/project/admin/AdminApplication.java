package com.project.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

@EnableAdminServer
@SpringBootApplication
public class AdminApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}

}

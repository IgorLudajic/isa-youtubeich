package com.team44.isa_youtubeich;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IsaYoutubeichApplication {

	public static void main(String[] args) {
		SpringApplication.run(IsaYoutubeichApplication.class, args);
	}

}

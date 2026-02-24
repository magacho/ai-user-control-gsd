package com.bemobi.aiusercontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI User Control Application
 * Sistema de gestão e controle de uso de ferramentas de IA
 *
 * @author Bemobi Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class AiUserControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiUserControlApplication.class, args);
    }
}

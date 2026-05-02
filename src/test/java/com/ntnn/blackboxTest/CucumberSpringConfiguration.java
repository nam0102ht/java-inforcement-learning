package com.ntnn.blackboxTest;

import com.ntnn.Main;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = Main.class)
public class CucumberSpringConfiguration {
}

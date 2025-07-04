/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author Prof.Jong Min Lee
 */
@Component
@Slf4j
public class PropertyReader {
	private Properties props = new Properties();
	
	public PropertyReader() {
	    this("/system.properties");
	}

	public PropertyReader(String propertyFile) {
		try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(this.getClass().getResourceAsStream(propertyFile)), StandardCharsets.UTF_8)) {
			props.load(reader);
			log.debug("props = {}", props);
		} catch (IOException e) {
			log.error("PropertyReader: 예외 발생 = {}", e.getMessage());
		}
	}

	
	public String getProperty(String propertyName) {
		return props.getProperty(propertyName);
	}
}
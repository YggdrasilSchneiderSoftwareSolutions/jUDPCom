package de.network.udp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	
	private Properties properties;
	
	public Config() {
		properties = new Properties();
		readPropertiesFromClasspath();
	}
	
	private void readPropertiesFromClasspath() {
		try (InputStream input = Config.class.getClassLoader().getResourceAsStream("application.properties")) {

            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }

            properties.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	public String getConfigFor(String key) {
		return properties.getProperty(key);
	}
}

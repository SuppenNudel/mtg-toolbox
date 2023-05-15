package suppennudel.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import suppennudel.Launcher;

public class UserConfig {

	private static final File DEFAULT_CONFIG_FILE = new File(System.getenv("APPDATA")+"/"+Launcher.getName(), "config.properties");

	private static UserConfig userConfig;
	private PropertiesConfiguration config;

	private UserConfig(File file) {
		Parameters params = new Parameters();
		if(file == null) {
			file = DEFAULT_CONFIG_FILE;
		}

		initFile(file);

		FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
				.configure(params.fileBased().setListDelimiterHandler(new DefaultListDelimiterHandler(',')).setFile(file));
		builder.setAutoSave(true);
		try {
			config = builder.getConfiguration();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	private static void initFile(File file) {
		if(!file.exists()) {
			File parentFile = file.getParentFile();
			if(parentFile != null) {
				parentFile.mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static UserConfig getConfig() {
		return getConfig(null);
	}

	public static UserConfig getConfig(File file) {
		if(userConfig == null) {
			userConfig = new UserConfig(file);
		}
		return userConfig;
	}

	public <T> List<T> getList(UserConfigListKey<T> userConfigKey) {
		return config.getList(userConfigKey.getType(), userConfigKey.getKey(), Arrays.asList(userConfigKey.getDefaultValues()));
	}

	public <T> void setList(UserConfigListKey<T> userConfigKey, List<? extends T> list) {
		List<String> collect = list.stream().map(value -> userConfigKey.convertValue(value)).collect(Collectors.toList());
		config.setProperty(userConfigKey.getKey(), collect);
	}

	public <T> void set(UserConfigKey<T> userConfigKey, T value) {
		config.setProperty(userConfigKey.getKey(), userConfigKey.convertValue(value));
	}

	public <T> T get(UserConfigKey<T> userConfigKey) {
		return config.get(userConfigKey.getType(), userConfigKey.getKey(), userConfigKey.getDefaultValue());
	}

}

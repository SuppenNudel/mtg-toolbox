package suppennudel.config;

import java.util.function.Function;

public class UserConfigKey<T> {

	public static final UserConfigKey<String> EXAMPLE_CONFIG = new UserConfigKey<>("example-config", String.class, "default value");

	private String key;
	private Class<T> type;
	private T defaultValue;
	private Function<T, String> customConversion;

	private UserConfigKey(String key, Class<T> type, T defaultValue) {
		this.key = key;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	private UserConfigKey(String key, Class<T> type, T defaultValue, Function<T, String> customConversion) {
		this(key, type, defaultValue);
		this.customConversion = customConversion;
	}

	public String convertValue(T value) {
		String converted;
		if(customConversion == null) {
			converted = value.toString();
		} else {
			converted = customConversion.apply(value);
		}
		return converted;
	}

	public String getKey() {
		return key;
	}

	public Class<T> getType() {
		return type;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

}

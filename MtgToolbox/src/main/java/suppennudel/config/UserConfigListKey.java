package suppennudel.config;

import java.util.function.Function;

public class UserConfigListKey<T> {

	public static final UserConfigListKey<String> EXAMPLE_LIST_CONFIG = new UserConfigListKey<>("example-list-config",
			String.class, new String[] { "default value 1", "default value 2" });

	private String key;
	private Class<T> type;
	private T[] defaultValues;
	private Function<T, String> customConversion;

	private UserConfigListKey(String key, Class<T> type, T[] defaultValues) {
		this.key = key;
		this.type = type;
		this.defaultValues = defaultValues;
	}

	private UserConfigListKey(String key, Class<T> type, T[] defaultValues, Function<T, String> customConversion) {
		this(key, type, defaultValues);
		this.customConversion = customConversion;
	}

	public String convertValue(T value) {
		String converted;
		if (customConversion == null) {
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

	public T[] getDefaultValues() {
		return defaultValues;
	}

}

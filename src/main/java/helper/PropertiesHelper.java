package helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

public class PropertiesHelper {
    /*
     * Properties mode
     */
    private static Properties _default, _props;

    private static Properties _propsForName() {
        InputStream inputStream = null;
        try {
            System.out.println("Loading properties: " + "default.properties");
            inputStream = helper.PropertiesHelper.class.getClassLoader().getResourceAsStream("default.properties");

            if (inputStream != null) {
                Properties prop = new Properties();
                prop.load(inputStream);
                return prop;
            } else {
                // throw new FileNotFoundException("property file '" +
                // propFileName + "' not found in the classpath" );
                System.out.println("default.properties" + " not found !");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
        return null;
    }

    private static void _initProps() {
        if (_default == null) {
            _default = _propsForName();

            String props = System.getProperty("properties");
            if (props != null) {
                _props = new Properties();
                try {
                    _props.load(new StringReader(props));
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
        }
    }

    public static String getPropValue(String key) {
        return getPropValue(key, null);
    }

    public static String getPropValue(String key, String defaultValue) {
        _initProps();

        if (System.getProperty(key) != null)
            return System.getProperty(key);

        if (_props != null && _props.containsKey(key))
            return _props.getProperty(key);

        if (_default != null && _default.containsKey(key))
            return _default.getProperty(key);

        return defaultValue;
    }
}

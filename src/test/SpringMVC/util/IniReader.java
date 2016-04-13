package test.SpringMVC.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by shenpeng on 2016/4/12.
 */
public class IniReader {
    protected HashMap<String, Properties> sections = new HashMap<>();
    private transient String currentSection;
    private transient Properties current;

    public IniReader(String filename){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            read(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.matches("\\[.*\\]")) {
                currentSection = line.replaceFirst("\\[(.*)\\]", "$1");
                current = new Properties();
                sections.put(currentSection, current);
            } else if (line.matches(".*=.*")) {
                if (current != null) {
                    int i = line.indexOf('=');
                    String name = line.substring(0, i).trim();
                    String value = line.substring(i + 1).trim();
                    current.setProperty(name, value);
                }
            }
        }
    }

    public String getValue(String section, String name) {
        Properties p = sections.get(section);
        if (p == null) {
            return null;
        }
        return p.getProperty(name);
    }

}

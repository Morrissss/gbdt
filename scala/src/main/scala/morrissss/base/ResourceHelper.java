package morrissss.base;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * DO NOT change the path of this class
 */
public class ResourceHelper {

    public static ResourceHelper instance() {
        if (INSTANCE == null) {
            synchronized (ResourceHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ResourceHelper();
                }
            }
        }
        return INSTANCE;
    }
    private static ResourceHelper INSTANCE;
    private ResourceHelper() {}

    /**
     * @param path relative path to com/red/
     * @return
     * @throws IOException
     */
    public String load(String path) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(path), "utf-8");
    }
}

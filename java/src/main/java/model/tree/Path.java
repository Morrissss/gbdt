package model.tree;

import java.util.ArrayList;
import java.util.List;

public class Path {

    public static int init() {
        return 1;
    }

    public static int toLeft(int cur) {
        return (cur << 2) + 0;
    }

    public static int toRight(int cur) {
        return (cur << 2) + 1;
    }

    public static int toNan(int cur) {
        return (cur << 2) + 2;
    }

    public static int toNormal(int cur) {
        return (cur << 2) + 3;
    }

    public static String printPath(int path) {
        List<String> result = new ArrayList<String>();
        while (path != 0) {
            switch (path & 3) {
                case 0: result.add("left"); break;
                case 1: result.add("right"); break;
                case 2: result.add("NaN"); break;
                case 3: result.add("normal"); break;
            }
            path >>>= 2;
        }
        // default 1 on the head
        StringBuilder sb = new StringBuilder(8 * result.size());
        for (int i = result.size()-1; i >= 1; i--) {
            sb.append(result.get(i)).append("==>");
        }
        sb.delete(result.size()-3, result.size());
        return sb.toString();
    }
}

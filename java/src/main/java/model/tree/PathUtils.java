package model.tree;

import java.util.ArrayList;
import java.util.List;

public class PathUtils {

    public static int init() {
        return 1;
    }

    public static int toLeft(int cur) {
        return (cur << 1) + 0;
    }

    public static int toRight(int cur) {
        return (cur << 1) + 1;
    }

    public static String printPath(int path) {
        List<String> result = new ArrayList<String>();
        while (path != 0) {
            switch (path & 1) {
                case 0: result.add("l"); break;
                case 1: result.add("r"); break;
            }
            path >>>= 1;
        }
        // default 1 on the head
        StringBuilder sb = new StringBuilder(4 * result.size());
        for (int i = result.size()-1; i >= 1; i--) {
            sb.append(result.get(i)).append("==>");
        }
        sb.delete(result.size()-3, result.size());
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(printPath(10));
    }
}

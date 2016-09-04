import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by tianren.xiong on 28/8/16.
 */
public class MainGui {
    public static void main(String[] args){

        String command = null;
        String playerid = null;
        Process extprocess = null;
        Process extprocess1 = null;
        PrintStream useraction = null;

        ProcessBuilder pb = new ProcessBuilder("java Game 127.0.0.1 1099 ahaha".split(" "));
        ProcessBuilder pb1 = new ProcessBuilder("java Game 127.0.0.1 1099 ohno".split(" "));
        pb.redirectErrorStream(true);
        String filename = "CS5223_StressTest12123"
                + System.getProperty("file.separator") + "ahaha" + "_12123";
        String filename1 = "CS5223_StressTest12123"
                + System.getProperty("file.separator") + "ohno" + "_12123";
        pb.redirectOutput(new File(filename));
        pb1.redirectOutput(new File(filename1));
        try {
            extprocess = pb.start();
            extprocess1 = pb1.start();
            extprocess = pb.start();
            useraction = new PrintStream(extprocess.getOutputStream(), true);
            extprocess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected error. Please contact TA");
            System.exit(0);
        }

    }
//        String[] players = {"1","23","3"};
//        int N = 12;
//        int K = 12;
//        String[][] GameState ={
//            {"*"," ","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"*"," ","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "},
//            {"ab","*","*"," ","*"," ","*"," ","*"," ","*"," "}};
//        testGUI gui = new testGUI("test", players, GameState, N, K);
//        gui.setSize(500,500);
//
//        players[0] = "adf";
//        GameState[1][1] = "asjhalkj";
//        //gui.refreshContent(players,GameState);
//    }
}

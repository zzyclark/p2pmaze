import com.sun.org.apache.bcel.internal.generic.LMUL;

import javax.swing.*;
import javax.swing.text.StringContent;
import java.applet.Applet;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JLabel;

/**
 * Created by tianren.xiong on 28/8/16.
 */
public class testGUI extends JFrame {
    private JPanel panel1;
    private JTable table;
    private JList playerList;
    public DefaultListModel<String> players;
    //private JTable table1;

    public testGUI(String title, Hashtable<String, Integer> playerScores, String[][] gs, int N, int K){
        super(title);
        setContentPane(panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);


        this.players = new DefaultListModel<String>();
        for(String key : playerScores.keySet()){
            this.players.addElement(key + ": " + playerScores.get(key));
        }

        playerList = new JList(this.players);
        DefaultListCellRenderer renderer =  (DefaultListCellRenderer)playerList.getCellRenderer();
        renderer.setHorizontalAlignment(JLabel.LEFT);
        panel1.add(playerList);


        String[] columns = new String[N];
        for(int i = 1;i<=N;i++){
            columns[i-1] = Integer.toString(i);
        }
        table = new JTable(gs, columns){
            public boolean isCellEditable(int row, int column) {
                return false;
            };
        };
        table.setTableHeader(null);
        JScrollPane scrollPane = new JScrollPane(table);
        table.enableInputMethods(false);
        table.setCellSelectionEnabled(false);
        panel1.add(scrollPane, BorderLayout.EAST);
    }

    public void update(){
        table.repaint();
        playerList.repaint();
    }

    public void update(String newTitle){
        this.setTitle(newTitle);
        table.repaint();
        playerList.repaint();
    }

    public void updateState(String[][] gameState) {
//        DefaultListModel lModel = (DefaultListModel)playerList.getModel();
//        lModel.removeAllElements();

        for (int i = 0; i < gameState.length; ++i) {
            String[] row = gameState[i];
            for (int j = 0; j < row.length; ++j) {
                if (null == row[j] || "O".equals(row[j])) {
                    table.getModel().setValueAt("O", i, j);
                } else if (!row[j].equals("x")) {
                    table.getModel().setValueAt(row[j].substring(0,2), i, j);
//                    lModel.addElement(row[j].substring(0,2));
                }
                else {
                    table.getModel().setValueAt(row[j], i, j);
                }
            }
        }
    }

//    private DefaultListModel getUserList (String[][] gameState) {
//        DefaultListModel lModel = new DefaultListModel();
//        for (int i = 0; i < gameState.length; ++i) {
//            String[] row = gameState[i];
//            for (int j = 0; j < row.length; ++j) {
//                if (null != row[j] && !row[j].equals("x")) {
//                    lModel.addElement(row[j].substring(0, 2));
//                }
//            }
//        }
//        return lModel;
//    }
}


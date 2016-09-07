import javax.swing.*;
import javax.swing.text.StringContent;
import java.applet.Applet;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.JLabel;

/**
 * Created by tianren.xiong on 28/8/16.
 */
public class testGUI extends JFrame {
    private JPanel panel1;
    private JTable table;
    private JList playerList;
    //private JTable table1;

    public testGUI(String title, String[] players, String[][] gs, int N, int K){
        super(title);
        setContentPane(panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        playerList = new JList(players);
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

//    public void refreshContent(ArrayList<String> players, String[][] gs){
//
//        playerList = new JList(players.toArray());
//        DefaultListCellRenderer renderer =  (DefaultListCellRenderer)playerList.getCellRenderer();
//        renderer.setHorizontalAlignment(JLabel.LEFT);
//
//        String[] columns = new String[gs.length];
//        for(int i = 1;i<=gs.length;i++){
//            columns[i-1] = Integer.toString(i);
//        }
//
//        table = new JTable(gs,columns){
//            public boolean isCellEditable(int row, int column) {
//                return false;
//            };
//        };
//        table.setTableHeader(null);
//        JScrollPane scrollPane = new JScrollPane(table);
//        table.enableInputMethods(false);
//        table.setCellSelectionEnabled(false);
//        panel1.repaint();
//    }

    public void update(){
        table.repaint();
        playerList.repaint();
    }

    public void updateState(String[][] gameState) {
        for (int i = 0; i < gameState.length; ++i) {
            String[] row = gameState[i];
            for (int j = 0; j < row.length; ++j) {
                if (null == row[j]) {
                    table.getModel().setValueAt("O", i, j);
                } else {
                    table.getModel().setValueAt(row[j], i, j);
                }
            }
        }
    }
}


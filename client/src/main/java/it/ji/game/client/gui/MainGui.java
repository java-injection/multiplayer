/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package it.ji.game.client.gui;

import it.ji.game.client.manager.ClientGameManager;
import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.logic.PlayerType;
import it.ji.game.utils.settings.Settings;

import java.awt.event.KeyEvent;
import java.util.Optional;

/**
 *
 * @author sommovir
 */
public class MainGui extends javax.swing.JFrame implements ClientListener {

   // private SingleCellPanel[][] board = new SingleCellPanel[20][20];
    public MainGui() {
        initComponents();
        ClientGameManager.getInstance().addClientListener(this);
        this.setTitle(ClientGameManager.getInstance().getSelfPlayer().getUsername());
        this.setBounds(0, 0, 800, 800);
        ClientGameManager.getInstance().setLocalBoard(initBoard());
        this.setLocationRelativeTo(null);
    }
    public SingleCellPanel[][] initBoard(){
        SingleCellPanel[][] board = new SingleCellPanel[Settings.getInstance().getHeight()][Settings.getInstance().getWitdh()];
        for(int i=0; i<Settings.getInstance().getHeight(); i++){
            for(int j=0; j<Settings.getInstance().getWitdh(); j++){
                SingleCellPanel singleCellPanel = new SingleCellPanel(j + ", " + i);
                this.jPanel_container.add(singleCellPanel);
                board[j][i] = singleCellPanel;
            }
        }
        return board;
    }
    //For testing purposes
   /* MainGui(boolean b) {
        initComponents();
        playerPositions.put(PlayerType.SELF, null);
        playerPositions.put(PlayerType.ENEMY, null);
        this.setBounds(0, 0, 800, 800);
        for(int i=0; i<20; i++){
            for(int j=0; j<20; j++){
                SingleCellPanel singleCellPanel = new SingleCellPanel(j + ", " + i);
                this.jPanel_container.add(singleCellPanel);
                board[j][i] = singleCellPanel;
            }
        }
        this.setLocationRelativeTo(null);
    }*/

    @Override
    public void userAccepted(String serverId, String username) {

    }

    @Override
    public void userRejected(String serverId, String username) {

    }

    @Override
    public void gameStarted(String serverId) {

    }

    @Override
    public void gameEnded(String serverId) {
        this.setVisible(false);
        ClientGameManager.getInstance().reset();
        new WatingGui().setVisible(true);
        this.dispose();

    }


    @Override
    public void positionChanged(String username, Coordinates coordinates) {
        System.out.println("[DEBUG][EVENT] Position changed: "+username+" to "+coordinates);
        Player selfPlayer = ClientGameManager.getInstance().getPlayerFromType(PlayerType.SELF);
        if (username.equals(selfPlayer.getUsername())){
            movePlayerToDirection(PlayerType.SELF, coordinates);
        }else {
            movePlayerToDirection(PlayerType.ENEMY, coordinates);
        }
    }

    @Override
    public void turretPlaced(Player player, Coordinates xy) {

    }

    @Override
    public void bulletMoved(Coordinates xy) {

    }

    @Override
    public void serverIsAlive(boolean isAlive, Optional<String> serverID) {
        System.out.println("[DEBUG][EVENT] Server is alive: "+isAlive);
        System.out.println("[WARNING] server alive status not yet managed on Main Gui");
    }

    @Override
    public void healthChanged(PlayerType playerType) {
        if (playerType == PlayerType.SELF) {
            jpanel_healtBarPlayer1.setHP(ClientGameManager.getInstance().getSelfPlayer().getHP());
        }
        if (playerType == PlayerType.ENEMY) {
            jpanel_healtBarPlayer2.setHP(ClientGameManager.getInstance().getEnemyPlayer().getHP());
        }
    }

    public void movePlayerToDirection(PlayerType playerType, Coordinates direction){
           System.out.println("[DEBUG][EVENT] Moving player to direction: "+direction);
            Player player = ClientGameManager.getInstance().getPlayerFromType(playerType);
            ClientGameManager.getInstance().updateLocalBoardByUsername(direction, player);
    }
    public void moveUp(PlayerType playerType) throws ArrayIndexOutOfBoundsException{
        System.out.println("[DEBUG][EVENT] Moving up");
        Player player = ClientGameManager.getInstance().getPlayerFromType(playerType);
        Coordinates coordinates = ClientGameManager.getInstance().getPlayerPositions().get(player);
        ClientGameManager.getInstance().requestToUpdatePosition(new Coordinates(coordinates.x(), coordinates.y() - 1), player);
    }

    public void moveDown(PlayerType playerType) throws ArrayIndexOutOfBoundsException{
        Player player = ClientGameManager.getInstance().getPlayerFromType(playerType);
        Coordinates coordinates = ClientGameManager.getInstance().getPlayerPositions().get(player);
        ClientGameManager.getInstance().requestToUpdatePosition(new Coordinates(coordinates.x(), coordinates.y() + 1), player);
    }

    public void moveLeft(PlayerType playerType) throws ArrayIndexOutOfBoundsException{
        Player player = ClientGameManager.getInstance().getPlayerFromType(playerType);
        Coordinates coordinates = ClientGameManager.getInstance().getPlayerPositions().get(player);
        ClientGameManager.getInstance().requestToUpdatePosition(new Coordinates(coordinates.x()-1, coordinates.y()), player);
    }

    public void moveRight(PlayerType playerType) throws ArrayIndexOutOfBoundsException{
        Player player = ClientGameManager.getInstance().getPlayerFromType(playerType);
        Coordinates coordinates = ClientGameManager.getInstance().getPlayerPositions().get(player);
        ClientGameManager.getInstance().requestToUpdatePosition(new Coordinates(coordinates.x()+1, coordinates.y()), player);
    }
    public void placeTurret(PlayerType playerType){
        Coordinates lastCoordinates = ClientGameManager.getInstance().getLastCoordinates();
        Coordinates delta = new Coordinates(ClientGameManager.getInstance().getPlayerPositions().get(ClientGameManager.getInstance().getPlayerFromType(playerType)).x()-lastCoordinates.x(),
                ClientGameManager.getInstance().getPlayerPositions().get(ClientGameManager.getInstance().getPlayerFromType(playerType)).y()-lastCoordinates.y());
        Player player = ClientGameManager.getInstance().getPlayerFromType(playerType);
        ClientGameManager.getInstance().requestToPlaceTurret(delta);
    }

    private Direction getDirectionFromDelta(Coordinates delta) {
        if (delta.x() == 0 && delta.y() == 0){
            System.out.println("NONE");
            return Direction.NONE;
        }
        if (delta.x() == 0 && delta.y() == -1){
            System.out.println("UP");
            return Direction.UP;
        }
        if (delta.x() == 0 && delta.y() == 1){
            System.out.println("DOWN");
            return Direction.DOWN;
        }
        if (delta.x() == 1 && delta.y() == 0){
            System.out.println("RIGHT");
            return Direction.RIGHT;
        }
        if (delta.x() == -1 && delta.y() == 0){
            System.out.println("LEFT");
            return Direction.LEFT;
        }
        throw new IllegalArgumentException("Invalid delta");
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:

    }
    private void formKeyPressed(java.awt.event.KeyEvent evt) {                                    
        // TODO add your handling code here: REFACTORARE COME HA DETTO LUCA FACENDO SPARIRE I NUMERI E METTENDO COSTANTI TIPO ENUM
        System.out.println("Key pressed: "+evt.getKeyCode());
        int keyCode = evt.getKeyCode();
        System.out.println("Key code: "+keyCode);
        if (keyCode != KeyEvent.VK_W && keyCode != KeyEvent.VK_S && keyCode != KeyEvent.VK_A && keyCode != KeyEvent.VK_D && keyCode != KeyEvent.VK_E){
            return;
        }
        if (keyCode == KeyEvent.VK_W) {
            moveUp(PlayerType.SELF);
        }
        if (keyCode == KeyEvent.VK_S) {
            moveDown(PlayerType.SELF);
        }
        if (keyCode == KeyEvent.VK_A) {
            moveLeft(PlayerType.SELF);
        }
        if (keyCode == KeyEvent.VK_D  ) {
            moveRight(PlayerType.SELF);
        }
        if(keyCode == KeyEvent.VK_E){
            placeTurret(PlayerType.SELF);
        }
    }
    /**
         * This method is called from within the constructor to initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is always
         * regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jpanel_healtBarPlayer1 = new HPPanel(PlayerType.SELF);
        jpanel_healtBarPlayer2 = new HPPanel(PlayerType.ENEMY);
        jPanel_container = new javax.swing.JPanel();
        jLabelPlayer1_Health = new javax.swing.JLabel();
        jLabelPlayer2_Health = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
/*
                formKeyTyped(evt);
*/
            }
        });

        javax.swing.GroupLayout jpanel_healtBarPlayer1Layout = new javax.swing.GroupLayout(jpanel_healtBarPlayer1);
        jpanel_healtBarPlayer1.setLayout(jpanel_healtBarPlayer1Layout);
        jpanel_healtBarPlayer1Layout.setHorizontalGroup(
            jpanel_healtBarPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jpanel_healtBarPlayer1Layout.setVerticalGroup(
            jpanel_healtBarPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 43, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jpanel_healtBarPlayer2Layout = new javax.swing.GroupLayout(jpanel_healtBarPlayer2);
        jpanel_healtBarPlayer2.setLayout(jpanel_healtBarPlayer2Layout);
        jpanel_healtBarPlayer2Layout.setHorizontalGroup(
            jpanel_healtBarPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 656, Short.MAX_VALUE)
        );
        jpanel_healtBarPlayer2Layout.setVerticalGroup(
            jpanel_healtBarPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel_container.setBackground(new java.awt.Color(102, 102, 102));
        jPanel_container.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
/*
                jPanel_containerKeyPressed(evt);
*/
            }
        });
        jPanel_container.setLayout(new java.awt.GridLayout(20, 20));

        jLabelPlayer1_Health.setText("jLabel1");

        jLabelPlayer2_Health.setText("jLabel2");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jpanel_healtBarPlayer2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jpanel_healtBarPlayer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelPlayer2_Health, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelPlayer1_Health, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel_container, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpanel_healtBarPlayer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPlayer1_Health, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jpanel_healtBarPlayer2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelPlayer2_Health, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_container, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


                                        

                                      

    /**
     * @param args the command line arguments
     */
    /*public static void main(String args[]) {
        *//* Set the Nimbus look and feel *//*
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        *//* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         *//*
        ClientGameManager.getInstance().setSelfPlayer(new Player("Test"));
        ClientGameManager.getInstance().setServerId("TEST");
        RedisManager.getInstance().publish("game.init", "TEST:Test:10,10");
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.start();
        //sincronizza il thread con il flusso principale
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        *//* Create and display the form *//*
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainGui(true).setVisible(true);
            }
        });
    }*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelPlayer1_Health;
    private javax.swing.JLabel jLabelPlayer2_Health;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_container;
    private HPPanel jpanel_healtBarPlayer1 ;
    private HPPanel jpanel_healtBarPlayer2;
    // End of variables declaration//GEN-END:variables
}

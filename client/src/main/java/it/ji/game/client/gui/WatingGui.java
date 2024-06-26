/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package it.ji.game.client.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import it.ji.game.client.Main;
import it.ji.game.client.exceptions.NameAlreadyInUse;
import it.ji.game.client.manager.ClientGameManager;
import it.ji.game.utils.logic.Coordinates;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.logic.PlayerType;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/**
 * @author sommovir
 */
public class WatingGui extends javax.swing.JFrame implements ClientListener {


    private boolean isServerIdSet = false;
    private boolean isNameSet = false;


    /**
     * Creates new form WatingGui
     */
    public WatingGui() {
        double v = Math.random() * 20;
        this.setTitle(v+"");
        initComponents();
        this.setLocationRelativeTo(null);
        //dont start the gui with a focused textfield
        ClientGameManager.getInstance().addClientListener(this);
        this.setAlwaysOnTop(true);
        jButton1.setBackground(new java.awt.Color(0, 59, 43));
        this.jLabel_message.setText("Insert your username and server id");
        //set focus on login button
        jButton1.requestFocusInWindow();
        //set the status label with no text and red icon from it.ji.game.client.images / red.png
        ImageIcon originalIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/it/ji/game/client/images/red.png")));
        Image originalImage = originalIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(12, 12, java.awt.Image.SCALE_SMOOTH);
        jLabel_serverStatus.setIcon(new ImageIcon(resizedImage));
        jLabel_serverStatus.setText("offline");
        this.jLabel_Error.setVisible(false);

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    ClientGameManager.getInstance().checkServer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    public WatingGui(String message) {
        double v = Math.random() * 20;
        this.setTitle(v+"");
        initComponents();
        this.setLocationRelativeTo(null);
        //dont start the gui with a focused textfield
        ClientGameManager.getInstance().addClientListener(this);
        this.setAlwaysOnTop(true);
        jButton1.setBackground(new java.awt.Color(0, 59, 43));
        this.jLabel_message.setText("Insert your username and server id");
        this.jLabel_Error.setText(message);
        //set focus on login button
        jButton1.requestFocusInWindow();
        //set the status label with no text and red icon from it.ji.game.client.images / red.png
        ImageIcon originalIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/it/ji/game/client/images/red.png")));
        Image originalImage = originalIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(12, 12, java.awt.Image.SCALE_SMOOTH);
        jLabel_serverStatus.setIcon(new ImageIcon(resizedImage));
        jLabel_serverStatus.setText("offline");
        this.jLabel_Error.setVisible(false);

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    ClientGameManager.getInstance().checkServer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void tryLogin() {
        this.jLabel_Error.setText("");
        ClientGameManager.getInstance().setServerId(ServerIDField.getText());
        ClientGameManager.getInstance().setSelfPlayer(new Player(nameField.getText(), PlayerType.SELF));
        try {
            ClientGameManager.getInstance().requestToStartClient();
        } catch (NameAlreadyInUse e) {
            this.jLabel_Error.setText("Name Already in use");
        }
    }

    @Override
    public void userAccepted(String serverId, String username) {
        System.out.println("[DEBUG][EVENT] User accepted: " + username);
        jLabel_message.setText(serverId + " : " + username + " accepted");
        this.repaint();
    }

    @Override
    public void userRejected(String serverId, String username) {
        if (ClientGameManager.getInstance().isClientAccpted()){
            return;
        }
        showError(serverId + " : " + username + " rejected");
    }

    @Override
    public void gameStarted(String serverId) {
        this.setVisible(false);
        Main.startGui();
        this.dispose();
    }

    @Override
    public void gameEnded(String serverId) {

    }

    @Override
    public void positionChanged(String username, Coordinates direction) {

    }

    @Override
    public void turretPlaced(Player player, Coordinates xy) {

    }

    @Override
    public void bulletMoved(Coordinates xy) {

    }

    @Override
    public void serverIsAlive(boolean isAlive, Optional<String> serverId) {
        if (isAlive) {
//            System.out.println("[DEBUG][GUI] Server is alive");
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/it/ji/game/client/images/green.png"));
            Image originalImage = originalIcon.getImage();
            Image resizedImage = originalImage.getScaledInstance(12, 12, java.awt.Image.SCALE_SMOOTH);
            jLabel_serverStatus.setIcon(new ImageIcon(resizedImage));
            jLabel_serverStatus.setText("online"+":"+serverId.get());
            ServerIDField.setText(serverId.get());
        } else {
//            System.out.println("[ERROR][GUI] Server is offline");
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/it/ji/game/client/images/red.png"));
            Image originalImage = originalIcon.getImage();
            Image resizedImage = originalImage.getScaledInstance(12, 12, java.awt.Image.SCALE_SMOOTH);
            jLabel_serverStatus.setIcon(new ImageIcon(resizedImage));
            jLabel_serverStatus.setText("offline");
        }
        //REPAINT
        this.repaint();
    }

    @Override
    public void healthChanged(PlayerType playerType) {

    }

    private void showError(String errorMessage) {
        this.jLabel_Error.setText(errorMessage);
        this.jLabel_Error.setVisible(true);
    }

    public void setMessage(String text) {
        this.jLabel_message.setText(text);
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
        jLabel_message = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        nameField = new javax.swing.JTextField();
        jLabel_Error = new javax.swing.JLabel();
        ServerIDField = new javax.swing.JTextField();
        jLabel_serverStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 204, 255));

        jLabel_message.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel_message.setText("nothing");

        jButton1.setBackground(new java.awt.Color(153, 153, 153));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton1.setText("Login");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        nameField.setForeground(new java.awt.Color(153, 153, 153));
        nameField.setText("Inserisci il tuo username");
        nameField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                nameFieldMouseClicked(evt);
            }
        });
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        jLabel_Error.setForeground(new java.awt.Color(255, 51, 51));
        jLabel_Error.setText("nothing");

        ServerIDField.setForeground(new java.awt.Color(153, 153, 153));
        ServerIDField.setText("ServerID");
        ServerIDField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ServerIDFieldMouseClicked(evt);
            }
        });
        ServerIDField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ServerIDFieldActionPerformed(evt);
            }
        });

        jLabel_serverStatus.setText("jLabel1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel_message, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel_serverStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(121, 121, 121)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                                        .addComponent(ServerIDField)
                                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel_Error, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel_message, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel_serverStatus))
                                .addGap(57, 57, 57)
                                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ServerIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel_Error, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        tryLogin();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        System.out.println("nameFieldActionPerformed");

        //remove hint from textfield, change font color to default
        if (!isNameSet) {
            nameField.setText("");
            nameField.setForeground(new java.awt.Color(148, 145, 145));
            isNameSet = true;
        }

    }//GEN-LAST:event_nameFieldActionPerformed

    private void ServerIDFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ServerIDFieldActionPerformed

    }//GEN-LAST:event_ServerIDFieldActionPerformed

    private void nameFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nameFieldMouseClicked
        //remove hint from textfield, change font color to default
        if (!isNameSet) {
            nameField.setText("");
            //set font bold
            nameField.setFont(new java.awt.Font("Segoe UI", 1, 14));
            nameField.setForeground(new java.awt.Color(0, 150, 202));
            isNameSet = true;
        }
    }//GEN-LAST:event_nameFieldMouseClicked

    private void ServerIDFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ServerIDFieldMouseClicked


        if (!isServerIdSet) {
            ServerIDField.setText("");
            ServerIDField.setFont(new java.awt.Font("Segoe UI", 0, 14));
            ServerIDField.setForeground(new java.awt.Color(202, 202, 202));
            isServerIdSet = true;
        }
    }//GEN-LAST:event_ServerIDFieldMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(WatingGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WatingGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WatingGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WatingGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                } catch (Exception ex) {
                    System.out.println("[ERROR] FlatDarkLaf non trovato");
                }
                new WatingGui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ServerIDField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel_Error;
    private javax.swing.JLabel jLabel_message;
    private javax.swing.JLabel jLabel_serverStatus;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField nameField;
    // End of variables declaration//GEN-END:variables
}

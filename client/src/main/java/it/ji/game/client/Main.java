package it.ji.game.client;

import it.ji.game.client.exceptions.ServerNotFoundException;
import it.ji.game.client.gui.MainGui;
import it.ji.game.client.gui.WatingGui;
import it.ji.game.client.manager.ClientGameManager;
import it.ji.game.utils.logic.Player;
import it.ji.game.utils.logic.PlayerType;

import javax.swing.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        startWaitingGui();
       /* Scanner scanner = new Scanner(System.in);
        System.out.println("Starting client...");
        System.out.println("insert server id: ");
        String serverId = scanner.nextLine();
        ClientGameManager.getInstance().setServerId(serverId);
        System.out.println("insert username: ");
        String username = scanner.nextLine();
        ClientGameManager.getInstance().addPlayer(new Player(username, PlayerType.SELF));*/

    }
     public  static void startGui() {
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

         /* Create and display the form */
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new MainGui().setVisible(true);
                }
            });
     }
     public static void startWaitingGui(){
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
                 new WatingGui().setVisible(true);
                 /*try {
                     ClientGameManager.getInstance().startClient();
                 } catch (ServerNotFoundException e) {
                     throw new RuntimeException(e);
                 }*/
             }
         });
     }
}

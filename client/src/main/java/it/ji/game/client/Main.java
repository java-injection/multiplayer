package it.ji.game.client;

import it.ji.game.client.exceptions.ServerNotFoundException;
import it.ji.game.client.gui.MainGui;
import it.ji.game.client.manager.ClientGameManager;
import it.ji.game.utils.settings.Settings;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
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

//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Starting client...");
//        System.out.println("insert server id: ");
//        String serverId = scanner.nextLine();
//        System.out.println("insert username: ");
//        String username = scanner.nextLine();
//        try {
//            ClientGameManager.getInstance().startClient(serverId, username);
//        } catch (ServerNotFoundException e) {
//            e.printStackTrace();
//        }
    }

}

package it.ji.game.utils.logic.objects;

public abstract class Items {

     private String name;
     private String serverID;

     public Items(String name, String serverID) {
          this.name = name;
          this.serverID = serverID;
     }

     public String getName() {
          return name;
     }

     public void setName(String name) {
          this.name = name;
     }

     public String getServerID() {
          return serverID;
     }

     public void setServerID(String serverID) {
          this.serverID = serverID;
     }

     abstract void use() throws Exception;
}

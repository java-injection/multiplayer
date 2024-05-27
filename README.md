# Multiplayer Game Project

## Descrizione del Progetto

Questo è il progetto della mia tesina delle superiori, un gioco multiplayer dove due avversari si sfidano su una mappa comune. Ogni giocatore può muoversi e piazzare delle torrette automatiche. Queste torrette lanciano proiettili in tutte e quattro le direzioni e, se colpiscono l'avversario, gli diminuiscono la vita.

Il progetto si basa su un'architettura MVC (Model-View-Controller) e utilizza un database Redis per la comunicazione tra i client, sfruttando il modulo pub/sub per permettere un'interazione in tempo reale.

## Struttura del Progetto

Il progetto è organizzato come un multi-modulo Maven con due componenti principali:
- **Server**: gestisce la logica del gioco e la comunicazione tra i client.
- **Client**: interfaccia utente con cui i giocatori interagiscono.

## Prerequisiti

Prima di iniziare, assicurati di avere installato sul tuo sistema:
- [Java JDK 11 o superiore](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Maven 3.6.0 o superiore](https://maven.apache.org/download.cgi)
- [Redis](https://redis.io/download)

## Installazione

1. Clona il repository GitHub:
    ```sh
    git clone https://github.com/daaddo/Multiplayer.git
    cd Multiplayer
    ```

2. Costruisci il progetto usando Maven:
    ```sh
    mvn clean install
    ```

## Esecuzione del Gioco

### Avvio del Server

1. Assicurati che Redis sia in esecuzione. Puoi avviare Redis con il comando:
    ```sh
    redis-server
    ```

2. Avvia il server del gioco:
    ```sh
    cd server
    mvn exec:java -Dexec.mainClass="com.yourpackage.ServerMain"
    ```

### Avvio del Client

1. Apri un nuovo terminale.
2. Avvia il client del gioco:
    ```sh
    cd client
    mvn exec:java -Dexec.mainClass="com.yourpackage.ClientMain"
    ```

Ripeti il passaggio per avviare un secondo client in un nuovo terminale per il secondo giocatore.

## Uso del Gioco

1. Entrambi i giocatori devono connettersi al server.
2. Una volta connessi, i giocatori possono muoversi sulla mappa e piazzare torrette.
3. Le torrette lanceranno proiettili in tutte e quattro le direzioni.
4. L'obiettivo è colpire l'avversario con i proiettili delle torrette per diminuire la sua vita.

## Contribuire

Se desideri contribuire a questo progetto, per favore segui questi passaggi:

1. Fai un fork del progetto.
2. Crea un branch per la tua feature (`git checkout -b feature/AmazingFeature`).
3. Effettua il commit delle tue modifiche (`git commit -m 'Add some AmazingFeature'`).
4. Fai il push del branch (`git push origin feature/AmazingFeature`).
5. Apri una Pull Request.

## Licenza

Questo progetto è concesso in licenza sotto i termini della [MIT License](LICENSE).

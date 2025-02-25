# Distributed Systems Project

## Requirements
- JavaSDK
- ApacheMaven

## Usage
Inside your repository:
- Clean and Compile:
    ```bash
    mvn clean compile
    ```

- After compiling, start server using the default maximum number of clients:
    1. With Maven:
        ```bash
        mvn exec:java -Pserver
        ```
    2. With Java directly:
        ```bash
        java -cp target/classes server.Server
        ```

- After compiling, start server specifying the maximum number of clients:
    1. With Maven:
        ```bash
        mvn exec:java -Dexec.mainClass="server.Server" -Dexec.args="<max_clients>"
        ```

    2. With Java directly:
        ```bash
        java -cp target/classes server.Server <max_clients>
        ```
    Replace `<max_clients>` with the desired maximum number of concurrent clients, e.g., 10.

- After compiling, start client:
    ```bash
    mvn exec:java -Pclient
    ```

- Just clean:
    ```bash
    mvn clean
    ```
- Just compile:
    ```bash
    mvn compile
    ```

- Run tests:
    ```bash
    mvn test
    ```

## Installation

```bash
git clone https://github.com/your-username/SD-Project.git
```

## Group Members
- [Edgar Ferreira](https://www.github.com/Edegare)
- [Fernando Pires](https://github.com/ferjpires)
- [Pedro Teixeira](https://github.com/PedroTe010)
- [Pedro Gomes](https://github.com/FkGomesKr)

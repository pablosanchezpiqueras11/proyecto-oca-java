-- 1. TABLAS DE REFERENCIA (Para tus IdEstado e IdTipo)
CREATE TABLE Estados (
    IdEstado INT PRIMARY KEY,
    Descripcion VARCHAR(50) NOT NULL -- Ej: 1='No iniciada', 2='En juego', 3='Finalizada'
);

CREATE TABLE TipoCasilla (
    IdTipo INT PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL -- Ej: 1='Normal', 2='Oca', 3='Puente', 4='Muerte'
);

-- 2. TABLA JUGADORES (Tu esquema: IdJugador, Nombre, Password)
CREATE TABLE Jugadores (
    IdJugador INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL -- Hash
);

-- 3. TABLA PARTIDAS (Tu esquema: IdPartida, Nombre, IdEstado)
CREATE TABLE Partidas (
    IdPartida INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(100), -- Nombre de la sala, ej: "Sala de Juan"
    IdEstado INT DEFAULT 1,
    FOREIGN KEY (IdEstado) REFERENCES Estados(IdEstado)
);

-- 4. TABLA DETALLES_PARTIDA (La clave de todo: vincula jugador y partida)
CREATE TABLE DetallesPartida (
    IdPartida INT,
    IdJugador INT,
    Casilla INT DEFAULT 1,    -- En qué casilla está
    Turno INT,                -- Orden de tiro (1º, 2º, 3º...)
    Bloqueos INT DEFAULT 0,   -- Turnos sin tirar (por Pozo, Posada...)
    PRIMARY KEY (IdPartida, IdJugador),
    FOREIGN KEY (IdPartida) REFERENCES Partidas(IdPartida),
    FOREIGN KEY (IdJugador) REFERENCES Jugadores(IdJugador)
);

-- 5. TABLA TABLERO (Define qué es cada casilla del 1 al 63)
CREATE TABLE Tablero (
    NumeroCasilla INT PRIMARY KEY, -- Del 1 al 63
    IdTipo INT,
    FOREIGN KEY (IdTipo) REFERENCES TipoCasilla(IdTipo)
);

-- DATOS INICIALES NECESARIOS (Ejecutar esto una vez)
INSERT INTO Estados VALUES (1, 'No Iniciada'), (2, 'En Juego'), (3, 'Finalizada');
INSERT INTO TipoCasilla VALUES (1, 'Normal'), (2, 'Oca'), (3, 'Puente'), (4, 'Posada'), (5, 'Pozo'), (6, 'Laberinto'), (7, 'Carcel'), (8, 'Calavera');
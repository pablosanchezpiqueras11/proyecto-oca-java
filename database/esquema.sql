-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 02-02-2026 a las 18:19:32
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `proyecto_oca`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `jugadores`
--

DROP TABLE IF EXISTS `jugadores`;
CREATE TABLE `jugadores` (
  `id` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `partidas_ganadas` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `jugadores`
--

INSERT INTO `jugadores` (`id`, `nombre`, `password`, `partidas_ganadas`) VALUES
(1, 'Campeon', '1234', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `partidas`
--

DROP TABLE IF EXISTS `partidas`;
CREATE TABLE `partidas` (
  `id` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `estado` varchar(20) DEFAULT 'ESPERANDO',
  `id_turno_actual` int(11) DEFAULT NULL,
  `id_ganador` int(11) DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `partidas`
--

INSERT INTO `partidas` (`id`, `nombre`, `estado`, `id_turno_actual`, `id_ganador`, `fecha_creacion`) VALUES
(1, 'PARTIDA_PRUEBA', 'ESPERANDO', 1, NULL, '2026-02-01 20:22:27');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `partidas_jugadores`
--

DROP TABLE IF EXISTS `partidas_jugadores`;
CREATE TABLE `partidas_jugadores` (
  `id_partida` int(11) NOT NULL,
  `id_jugador` int(11) NOT NULL,
  `casilla` int(11) DEFAULT 1,
  `orden` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `partidas_jugadores`
--

INSERT INTO `partidas_jugadores` (`id_partida`, `id_jugador`, `casilla`, `orden`) VALUES
(1, 1, 1, 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `jugadores`
--
ALTER TABLE `jugadores`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `partidas`
--
ALTER TABLE `partidas`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `partidas_jugadores`
--
ALTER TABLE `partidas_jugadores`
  ADD PRIMARY KEY (`id_partida`,`id_jugador`),
  ADD KEY `id_jugador` (`id_jugador`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `jugadores`
--
ALTER TABLE `jugadores`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `partidas`
--
ALTER TABLE `partidas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `partidas_jugadores`
--
ALTER TABLE `partidas_jugadores`
  ADD CONSTRAINT `partidas_jugadores_ibfk_1` FOREIGN KEY (`id_partida`) REFERENCES `partidas` (`id`),
  ADD CONSTRAINT `partidas_jugadores_ibfk_2` FOREIGN KEY (`id_jugador`) REFERENCES `jugadores` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

package com.oca.logica;

import java.util.Arrays;
import java.util.List;

public class ReglasOca {

    // Definición de las casillas de Oca estándar
    private static final List<Integer> CASILLAS_OCA = Arrays.asList(
        5, 9, 14, 18, 23, 27, 32, 36, 41, 45, 50, 54, 59
    );

    // Constantes para casillas especiales clave
    public static final int META = 63;
    public static final int PUENTE_1 = 6;
    public static final int PUENTE_2 = 12;
    public static final int POSADA = 19;
    public static final int DADOS_1 = 26;
    public static final int POZO = 31;
    public static final int LABERINTO = 42;
    public static final int CARCEL = 52;
    public static final int DADOS_2 = 53;
    public static final int MUERTE = 58;

    /**
     * Genera un número aleatorio entre 1 y 6.
     * @return valor del dado.
     */
    public int lanzarDado() {
        return (int) (Math.random() * 6) + 1;
    }

    /**
     * Calcula la posición final tras una tirada normal, gestionando el rebote
     * si se pasa de la meta (63).
     * @param casillaActual Casilla de origen.
     * @param tirada Valor del dado.
     * @return La casilla preliminar (antes de comprobar especiales).
     */
    public int calcularPosicionRebote(int casillaActual, int tirada) {
        int destino = casillaActual + tirada;
        
        if (destino > META) {
            int sobrante = destino - META;
            destino = META - sobrante;
        }
        
        return destino;
    }

    /**
     * Comprueba si la casilla actual tiene algún efecto de movimiento (salto).
     * @param casilla La casilla donde ha caído el jugador.
     * @return La nueva casilla destino si hay salto, o la misma casilla si no.
     */
    public int comprobarCasillaEspecial(int casilla) {
        // 1. REGLA DE LA OCA: De Oca a Oca...
        if (CASILLAS_OCA.contains(casilla)) {
            // Caso especial: Si es la última Oca (59), salta a la Meta (63)
            if (casilla == 59) {
                return META;
            }
            
            // Buscar la siguiente Oca en la lista
            int indice = CASILLAS_OCA.indexOf(casilla);
            if (indice != -1 && indice < CASILLAS_OCA.size() - 1) {
                return CASILLAS_OCA.get(indice + 1);
            }
        }

        // 2. REGLA DEL PUENTE: De puente a puente...
        if (casilla == PUENTE_1) return PUENTE_2;
        if (casilla == PUENTE_2) return PUENTE_1;

        // 3. REGLA DE LOS DADOS: De dado a dado...
        if (casilla == DADOS_1) return DADOS_2;
        if (casilla == DADOS_2) return DADOS_1;

        // 4. REGLA DEL LABERINTO: Del laberinto al 30
        if (casilla == LABERINTO) return 30;

        // 5. REGLA DE LA MUERTE: A la casilla 1
        if (casilla == MUERTE) return 1;

        // Si no es ninguna especial de movimiento, se queda donde está
        // (Nota: Posada, Pozo y Cárcel no mueven al jugador, solo le quitan turnos)
        return casilla;
    }

    /**
     * Indica si el jugador tiene derecho a tirar otra vez.
     * Esto ocurre en Ocas, Puentes y Dados.
     */
    public boolean turnoExtra(int casilla) {
        return CASILLAS_OCA.contains(casilla) || 
               casilla == PUENTE_1 || casilla == PUENTE_2 ||
               casilla == DADOS_1 || casilla == DADOS_2;
    }

    /**
     * Devuelve el número de turnos que se pierden si se cae en una casilla de castigo.
     * @param casilla Casilla actual.
     * @return Número de turnos a esperar (0 si no hay castigo).
     */
    public static int getTurnosCastigo(int casilla) {
        switch (casilla) {
            case POSADA: return 1;  // Pierde 1 turno
            case POZO:   return -1;  // Pierde 2 turnos (simulado)
            case CARCEL: return 3;  // Pierde 3 turnos
            default:     return 0;
        }
    }
}
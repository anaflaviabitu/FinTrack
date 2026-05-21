package utils;

import java.text.NumberFormat;
import java.util.Locale;

public class Formatador {

    private static final Locale BR =
            new Locale("pt", "BR");

    public static String formatarValor(double valor) {

        NumberFormat formatador =
                NumberFormat.getCurrencyInstance(BR);

        return formatador.format(valor);
    }
}
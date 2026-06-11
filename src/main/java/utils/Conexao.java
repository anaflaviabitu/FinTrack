package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static String url =
            "jdbc:h2:./fintrack_db";

    private static String user = "sa";
    private static String pass = "";

    private static Connection instancia;

    public static void configurar(
            String novaUrl,
            String novoUser,
            String novoPass
    ) {

        url = novaUrl;
        user = novoUser;
        pass = novoPass;
    }

    public static void configurarConexao(
            Connection conn
    ) {

        instancia = conn;
    }

    public static Connection getConexao()
            throws SQLException {

        if (instancia != null) {

            return instancia;
        }

        instancia =
                DriverManager.getConnection(
                        url,
                        user,
                        pass
                );

        return instancia;
    }

    public static void fechar() {

        try {

            if (
                    instancia != null
                            && !instancia.isClosed()
            ) {

                instancia.close();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao fechar conexão: "
                            + e.getMessage()
            );
        }
    }
}
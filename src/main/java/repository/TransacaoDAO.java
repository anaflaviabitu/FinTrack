package repository;

import model.Transacao;
import utils.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class TransacaoDAO {

    public void inicializarBanco() {

        String sql =
                "CREATE TABLE IF NOT EXISTS transacoes (" +
                        " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                        " descricao VARCHAR(100) NOT NULL," +
                        " valor DECIMAL(10,2) NOT NULL," +
                        " tipo VARCHAR(10) NOT NULL," +
                        " data DATE NOT NULL" +
                        ")";

        try (
                Connection conn = Conexao.getConexao();
                Statement stmt = conn.createStatement()
        ) {

            stmt.execute(sql);

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao criar tabela: "
                            + e.getMessage()
            );
        }
    }

    public void salvar(Transacao t) {

        String sql =
                "INSERT INTO transacoes " +
                        "(descricao, valor, tipo, data) " +
                        "VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {

            conn = Conexao.getConexao();
            conn.setAutoCommit(false);

            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(
                                    sql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                stmt.setString(
                        1,
                        t.getDescricao()
                );

                stmt.setDouble(
                        2,
                        t.getValor()
                );

                stmt.setString(
                        3,
                        t.getTipo()
                );

                stmt.setDate(
                        4,
                        Date.valueOf(t.getData())
                );

                stmt.executeUpdate();

                try (
                        ResultSet rs =
                                stmt.getGeneratedKeys()
                ) {

                    if (rs.next()) {

                        t.setId(
                                rs.getInt(1)
                        );
                    }
                }
            }

            conn.commit();

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao salvar: "
                            + e.getMessage()
            );

            try {

                if (conn != null) {

                    conn.rollback();
                }

            } catch (SQLException ex) {

                System.err.println(
                        "Rollback falhou: "
                                + ex.getMessage()
                );
            }

        } finally {

            try {

                if (conn != null) {

                    conn.setAutoCommit(true);
                }

            } catch (SQLException ignored) {
            }
        }
    }

    public List<Transacao> listarTodos() {

        List<Transacao> lista =
                new ArrayList<>();

        String sql =
                "SELECT id, descricao, valor, tipo, data " +
                        "FROM transacoes " +
                        "ORDER BY data DESC, id DESC";

        try (

                Connection conn =
                        Conexao.getConexao();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()

        ) {

            while (rs.next()) {

                Transacao t =
                        new Transacao(

                                rs.getString(
                                        "descricao"
                                ),

                                rs.getDouble(
                                        "valor"
                                ),

                                rs.getString(
                                        "tipo"
                                ),

                                rs.getDate(
                                        "data"
                                ).toLocalDate()
                        );

                t.setId(
                        rs.getInt("id")
                );

                lista.add(t);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao listar: "
                            + e.getMessage()
            );
        }

        return lista;
    }

    public List<Transacao> listarPorMes(
            int mes
    ) {

        List<Transacao> lista =
                new ArrayList<>();

        String sql =
                "SELECT id, descricao, valor, tipo, data " +
                        "FROM transacoes " +
                        "WHERE MONTH(data) = ? " +
                        "ORDER BY data DESC, id DESC";

        try (

                Connection conn =
                        Conexao.getConexao();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)

        ) {

            stmt.setInt(1, mes);

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    Transacao t =
                            new Transacao(

                                    rs.getString(
                                            "descricao"
                                    ),

                                    rs.getDouble(
                                            "valor"
                                    ),

                                    rs.getString(
                                            "tipo"
                                    ),

                                    rs.getDate(
                                            "data"
                                    ).toLocalDate()
                            );

                    t.setId(
                            rs.getInt("id")
                    );

                    lista.add(t);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao listar por mês: "
                            + e.getMessage()
            );
        }

        return lista;
    }

    public void atualizar(
            Transacao t
    ) {

        String sql =
                "UPDATE transacoes " +
                        "SET descricao = ?, " +
                        "valor = ?, " +
                        "tipo = ?, " +
                        "data = ? " +
                        "WHERE id = ?";

        Connection conn = null;

        try {

            conn = Conexao.getConexao();
            conn.setAutoCommit(false);

            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(sql)
            ) {

                stmt.setString(
                        1,
                        t.getDescricao()
                );

                stmt.setDouble(
                        2,
                        t.getValor()
                );

                stmt.setString(
                        3,
                        t.getTipo()
                );

                stmt.setDate(
                        4,
                        Date.valueOf(t.getData())
                );

                stmt.setInt(
                        5,
                        t.getId()
                );

                stmt.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao atualizar: "
                            + e.getMessage()
            );

            try {

                if (conn != null) {

                    conn.rollback();
                }

            } catch (SQLException ex) {

                System.err.println(
                        "Rollback falhou: "
                                + ex.getMessage()
                );
            }

        } finally {

            try {

                if (conn != null) {

                    conn.setAutoCommit(true);
                }

            } catch (SQLException ignored) {
            }
        }
    }

    public void remover(
            int id
    ) {

        String sql =
                "DELETE FROM transacoes " +
                        "WHERE id = ?";

        Connection conn = null;

        try {

            conn = Conexao.getConexao();
            conn.setAutoCommit(false);

            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(sql)
            ) {

                stmt.setInt(1, id);

                stmt.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao remover: "
                            + e.getMessage()
            );

            try {

                if (conn != null) {

                    conn.rollback();
                }

            } catch (SQLException ex) {

                System.err.println(
                        "Rollback falhou: "
                                + ex.getMessage()
                );
            }

        } finally {

            try {

                if (conn != null) {

                    conn.setAutoCommit(true);
                }

            } catch (SQLException ignored) {
            }
        }
    }

    public double calcularSaldo() {

        double saldo = 0;

        String sql =
                "SELECT " +
                        "SUM(CASE " +
                        "WHEN tipo = 'Receita' THEN valor " +
                        "ELSE -valor END) AS saldo " +
                        "FROM transacoes";

        try (

                Connection conn =
                        Conexao.getConexao();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()

        ) {

            if (rs.next()) {

                saldo =
                        rs.getDouble("saldo");
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao calcular saldo: "
                            + e.getMessage()
            );
        }

        return saldo;
    }

    public double calcularReceitas() {

        double receitas = 0;

        String sql =
                "SELECT SUM(valor) AS total " +
                        "FROM transacoes " +
                        "WHERE tipo = 'Receita'";

        try (

                Connection conn =
                        Conexao.getConexao();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()

        ) {

            if (rs.next()) {

                receitas =
                        rs.getDouble("total");
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao calcular receitas: "
                            + e.getMessage()
            );
        }

        return receitas;
    }

    public double calcularDespesas() {

        double despesas = 0;

        String sql =
                "SELECT SUM(valor) AS total " +
                        "FROM transacoes " +
                        "WHERE tipo = 'Despesa'";

        try (

                Connection conn =
                        Conexao.getConexao();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()

        ) {

            if (rs.next()) {

                despesas =
                        rs.getDouble("total");
            }

        } catch (SQLException e) {

            System.err.println(
                    "Erro ao calcular despesas: "
                            + e.getMessage()
            );
        }

        return despesas;
    }
}
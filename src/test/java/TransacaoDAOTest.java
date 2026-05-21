import model.Transacao;
import org.junit.jupiter.api.*;
import repository.TransacaoDAO;
import utils.Conexao;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransacaoDAOTest {

    private TransacaoDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        Conexao.configurar("jdbc:sqlite::memory:",
                "",
                ""
        );
        dao = new TransacaoDAO();
        dao.inicializarBanco();
    }

    @AfterEach
    void tearDown() throws Exception {
        try (var conn = Conexao.getConexao();
             var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS transacoes");
        }
        Conexao.fechar();
    }

    @Test
    void deveSalvarEListarTransacao() {
        dao.salvar(new Transacao("Salário", 3000.0, "Receita", LocalDate.of(2025, 5, 1)));

        List<Transacao> lista = dao.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("Salário", lista.get(0).getDescricao());
        assertEquals(3000.0,    lista.get(0).getValor(), 0.001);
    }

    @Test
    void deveSalvarMultiplasTransacoes() {
        dao.salvar(new Transacao("Salário", 3000.0, "Receita", LocalDate.now()));
        dao.salvar(new Transacao("Aluguel", 1200.0, "Despesa", LocalDate.now()));
        dao.salvar(new Transacao("Mercado",  400.0, "Despesa", LocalDate.now()));

        assertEquals(3, dao.listarTodos().size());
    }

    @Test
    void deveRemoverTransacaoPorId() {
        Transacao t = new Transacao("Bônus", 500.0, "Receita", LocalDate.now());
        dao.salvar(t);
        assertTrue(t.getId() > 0, "ID deve ser gerado após salvar");

        dao.remover(t.getId());
        assertTrue(dao.listarTodos().isEmpty());
    }

    @Test
    void removerIdInexistenteNaoDeveLancarExcecao() {
        assertDoesNotThrow(() -> dao.remover(9999));
    }

    @Test
    void calculoSaldoDeveEstarCorreto() {
        dao.salvar(new Transacao("Salário", 5000.0, "Receita", LocalDate.now()));
        dao.salvar(new Transacao("Aluguel", 1500.0, "Despesa", LocalDate.now()));
        dao.salvar(new Transacao("Mercado",  300.0, "Despesa", LocalDate.now()));

        List<Transacao> lista = dao.listarTodos();

        double receitas = lista.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Receita"))
                .mapToDouble(Transacao::getValor).sum();

        double despesas = lista.stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Despesa"))
                .mapToDouble(Transacao::getValor).sum();

        assertEquals(5000.0, receitas, 0.001);
        assertEquals(1800.0, despesas, 0.001);
        assertEquals(3200.0, receitas - despesas, 0.001);
    }

    @Test
    void listarTodosRetornaVazioSemDados() {
        assertNotNull(dao.listarTodos());
        assertTrue(dao.listarTodos().isEmpty());
    }

    @Test
    void deveAtualizarTransacao() {

        Transacao t = new Transacao(
                "Salário",
                3000,
                "Receita",
                LocalDate.now()
        );

        dao.salvar(t);

        t.setDescricao("Salário Atualizado");
        t.setValor(5000);

        dao.atualizar(t);

        Transacao atualizada =
                dao.listarTodos().get(0);

        assertEquals(
                "Salário Atualizado",
                atualizada.getDescricao()
        );

        assertEquals(
                5000,
                atualizada.getValor(),
                0.001
        );
    }
}
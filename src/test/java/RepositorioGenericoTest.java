import model.RepositorioGenerico;
import model.Transacao;
import model.TransacaoMensal;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioGenericoTest {

    private RepositorioGenerico<Transacao> repo;
    private Transacao t1;
    private Transacao t2;

    @BeforeEach
    void setUp() {
        repo = new RepositorioGenerico<>();
        t1 = new Transacao("Salário", 3000.0, "Receita", LocalDate.now());
        t2 = new Transacao("Mercado",  500.0, "Despesa", LocalDate.now());
    }

    @AfterEach
    void tearDown() { repo = null; }

    @Test
    void deveAdicionarEListarItem() {
        repo.adicionar(t1);
        assertEquals(1, repo.listarTodos().size());
        assertEquals(t1, repo.listarTodos().get(0));
    }

    @Test
    void deveRemoverItem() {
        repo.adicionar(t1);
        repo.adicionar(t2);
        repo.remover(t1);
        assertEquals(1, repo.listarTodos().size());
        assertFalse(repo.listarTodos().contains(t1));
    }

    @Test
    void listarTodosRetornaCopiaDefensiva() {
        repo.adicionar(t1);
        assertNotSame(repo.listarTodos(), repo.listarTodos());
    }


    @Test
    void adicionarTodosDeveAceitarSubtipos() {
        List<TransacaoMensal> mensais = new ArrayList<>();
        mensais.add(new TransacaoMensal("Streaming", 50.0,   "Despesa", LocalDate.now()));
        mensais.add(new TransacaoMensal("Salário",  3000.0, "Receita", LocalDate.now()));

        repo.adicionarTodos(mensais);
        assertEquals(2, repo.tamanho());
    }


    @Test
    void copiarParaDevePopularListaDestino() {
        repo.adicionar(t1);
        repo.adicionar(t2);

        List<Object> destino = new ArrayList<>();
        repo.copiarPara(destino);
        assertEquals(2, destino.size());
    }

    @Test
    void imprimirListaDeveAceitarQualquerTipo() {
        assertDoesNotThrow(() -> repo.imprimirLista(List.of("x", 1, true)));
    }

    @Test
    void tamanhoDeveRefletirInsercoes() {
        assertEquals(0, repo.tamanho());
        repo.adicionar(t1);
        assertEquals(1, repo.tamanho());
    }
}
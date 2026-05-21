import model.Transacao;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransacaoTest {

    private Transacao receita;
    private Transacao despesa;

    @BeforeEach
    void setUp() {
        receita = new Transacao("Salário", 3000.0, "Receita", LocalDate.of(2025, 5, 1));
        despesa = new Transacao("Aluguel", 1200.0, "Despesa", LocalDate.of(2025, 5, 5));
    }

    @AfterEach
    void tearDown() {
        receita = null;
        despesa = null;
    }

    @Test
    void deveCriarReceitaComSucesso() {
        assertEquals("Salário", receita.getDescricao());
        assertEquals(3000.0, receita.getValor(), 0.001);
        assertEquals("Receita", receita.getTipo());
        assertEquals(LocalDate.of(2025, 5, 1), receita.getData());
    }

    @Test
    void deveCriarDespesaComSucesso() {
        assertEquals("Aluguel", despesa.getDescricao());
        assertEquals(1200.0, despesa.getValor(), 0.001);
        assertEquals("Despesa", despesa.getTipo());
    }

    @Test
    void deveLancarExcecaoParaValorZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transacao("Teste", 0, "Receita", LocalDate.now())
        );
    }

    @Test
    void deveLancarExcecaoParaValorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transacao("Teste", -50.0, "Despesa", LocalDate.now())
        );
    }

    @Test
    void deveLancarExcecaoParaTipoInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transacao("Teste", 100.0, "Investimento", LocalDate.now())
        );
    }

    @Test
    void deveAceitarTipoCaseInsensitive() {
        assertDoesNotThrow(() -> new Transacao("Teste", 100.0, "receita", LocalDate.now()));
        assertDoesNotThrow(() -> new Transacao("Teste", 100.0, "DESPESA", LocalDate.now()));
    }

    @Test
    void deveAtualizarId() {
        receita.setId(42);
        assertEquals(42, receita.getId());
    }

    @Test
    void toStringDeveConterDescricaoEValor() {
        String resultado = receita.toString();
        assertTrue(resultado.contains("Salário"));
        assertTrue(resultado.contains("3000"));
    }


}


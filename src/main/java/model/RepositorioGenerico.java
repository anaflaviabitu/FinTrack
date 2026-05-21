package model;

import java.util.ArrayList;
import java.util.List;

public class RepositorioGenerico<T> {

    private List<T> lista;

    public RepositorioGenerico() {
        this.lista = new ArrayList<>();
    }

    public void adicionar(T item) {
        lista.add(item);
    }


    public void adicionarTodos(List<? extends T> novosItens) {
        lista.addAll(novosItens);
    }


    public void copiarPara(List<? super T> destino) {
        destino.addAll(lista);
    }

    public void imprimirLista(List<?> itens) {
        for (Object item : itens) {
            System.out.println(item);
        }
    }

    public List<T> listarTodos() {
        return new ArrayList<>(lista);
    }

    public void remover(T item) {
        lista.remove(item);
    }

    public int tamanho() {
        return lista.size();
    }
}
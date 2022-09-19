import java.util.function.DoubleToLongFunction;

public class Pair implements Comparable<Pair>{
    // Uma classe que implementa a funcionalidade de um pair de Double e Integer
    Double fs;
    Integer sc;
    public Pair(Double a, Integer b) {
        fs = a;
        sc = b;
    }
    public Double first() { return fs;}
    public Integer second() { return sc;}


    // Definição de um comparador para o objeto Pair que criamos para usar diretamente na PriorityQueue
    @Override
    public int compareTo(Pair o) {
        if(o.first() > this.fs)
            return -1;
        else if(this.first() > o.first())
            return 1;
        return 0;
    }
}

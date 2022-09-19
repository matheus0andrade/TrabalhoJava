import java.sql.*;
import java.util.*;

public class Main {


    public static Integer AirportQuery(ArrayList<Airport> airport_list) { // Função que ajuda o usuario a escolher o aeroporto que ele quer sair ou entrar
        Scanner reader = new Scanner(System.in);
        System.out.print("\nDe qual estado deseja sair?\n");
        // Criamos um HashSet para não repetir estados
        HashSet<String> hs = new HashSet<>();
        ArrayList<String> list = new ArrayList<>(), list2 = new ArrayList<>();
        for(int ind = 0, cnt = 1; ind < airport_list.size(); ind++) {
            String tmp = airport_list.get(ind).getEstado();
            if(hs.contains(tmp))
                continue;
            // Se o hashset ainda não contém o estado, adicionamos e printamos como uma opção
            hs.add(tmp);
            list.add(tmp);
            System.out.println(" " + (cnt++) + " - " + tmp);
        }
        System.out.print("\nOpção escolhida: ");
        int input = reader.nextInt();

        // Caso o usuario entre com algo invalido
        if(input <= 0 | input > list.size()) {
            System.out.println("Opção inválida! Encerrando a consulta.");
            return -1;
        }
        String estadoSelecionado = list.get(input - 1);
        if(!hs.contains(estadoSelecionado))
            return -1;

        // Mesma coisa, com a cidade agora
        System.out.print("\nDe qual cidade deseja sair?\n");
        for(int ind = 0, cnt2 = 1; ind < airport_list.size(); ind++) {
            String tmp2 = airport_list.get(ind).getCidade();
            if(!airport_list.get(ind).getEstado().equals(estadoSelecionado))
                continue;
            list2.add(tmp2);
            System.out.println(" " + (cnt2++) + " - " + tmp2);
        }
        System.out.print("\nOpção escolhida: ");
        input = reader.nextInt();
        if(input <= 0 | input > list2.size()) {
            System.out.println("Opção inválida! Encerrando a consulta.");
            return -1;
        }
        for(int ind = 0; ind < airport_list.size(); ind++) {
            if(airport_list.get(ind).getCidade().equals(list2.get(input - 1)))
                return ind;
        }
        return -1;
    }

    // Implementação simples do algoritmo de Dijkstra
    public static ArrayList<Integer> Dijkstra(ArrayList<Airport> graph, int start, int end) {

        // Array para salvar distâncias do menor percurso de start até o indice
        ArrayList<Double> ans = new ArrayList<>(Collections.nCopies(graph.size(), 100.));

        // Para salvar quem ja visitamos
        ArrayList<Boolean> vis = new ArrayList<>(Collections.nCopies(graph.size(), false));

        // Para conseguir recuperar o caminho, basta definir uma array de previous, e depois para recuperar basta iterar do final até o começo
        ArrayList<Integer> prev = new ArrayList<>(Collections.nCopies(graph.size(), 0));

        // Implementação do algoritmo de maneira normal
        PriorityQueue<Pair> pq = new PriorityQueue<>();
        pq.add(new Pair(0., start));
        while(!pq.isEmpty()) {
            Pair e = pq.remove();
            if(vis.get(e.second()))
                continue;
            vis.set(e.second(), true);
            ans.set(e.second(), e.first());
            ArrayList<Airport.Connections> connections = graph.get(e.second()).getConnections();
            for(Airport.Connections connection : connections) {
                Integer index = connection.getAirport().getIndex();
                if(vis.get(index))
                    continue;

                // Se ainda não visitamos o nó, adicionamos ele na priority queue e atualizamos o pai imediato se gerar um resultado menor
                Double dist  = e.first() + connection.getDistance();
                if(ans.get(index) > dist) {
                    ans.set(index, dist);
                    prev.set(index, e.second());
                }
                pq.add(new Pair(dist, index));
            }
        }

        // Aqui recuperamos o caminho
        ArrayList<Integer> path = new ArrayList<>();
        path.add(end);
        while(end != start) {
            path.add(prev.get(end));
            end = prev.get(end);
        }
        return path;
    }

    public static void main(String[] args) {

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/airport_data", "matheus", "matheus");

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from aeroportos");
            ArrayList<Airport> airport_list = new ArrayList<>();
            int index = 0;

            // Se conseguimos realizar a query, agora passamos os dados obtidos para uma ArrayList, que será a "lista de aeroportos"
            while (rs.next())
                airport_list.add(new Airport(rs.getString("nome"), rs.getString("iata"), rs.getString("cidade"), rs.getString("estado"), rs.getDouble("latitude"), rs.getDouble("longitude"), index++));
            for(Airport var : airport_list)
                for(Airport var2 : airport_list) {
                    var.addConnection(var.distanceTo(var2), var2);
                }
            Scanner reader = new Scanner(System.in);
            while(true) {
                System.out.println("O que deseja fazer? \n\n 1 - Realizar uma consulta\n 0 - Sair\n");
                System.out.print("Opção escolhida: ");
                int input = reader.nextInt();
                if(input == 0) {
                    System.out.println("Fechando o programa...");
                    break;
                }
                Integer origin = AirportQuery(airport_list);
                if(origin == -1)
                    continue;
                Integer to =  AirportQuery(airport_list);
                if(to == -1 | to.equals(origin)) {
                    if(to.equals(origin))
                        System.out.println("Você está saindo desse aeroporto! Consulta encerrada.");
                    continue;
                }
                // Se e escolha dos aeroportos está correta e deu tudo certo, tiramos a conexão entre eles, realizamos o Dijkstra e adicionamos a conexão novamente
                // Isso é só para que o nosso percurso total tenha pelo menos uma escala
                Airport a1 = airport_list.get(origin), a2 = airport_list.get(to);
                a1.removeConnection(a2);

                // Dijkstra(args...) devolve o caminho percorrido de menor "custo" da origem até o destino
                ArrayList<Integer> path = Dijkstra(airport_list, origin, to);
                String result = "";
                for(int i = path.size() - 1; i >= 0; i--)
                    result += (airport_list.get(path.get(i)).getShortName() + (i > 0 ? " -> " : ""));
                a1.addConnection(a1.distanceTo(a2), a2);
                System.out.print("\nMelhor rota não trivial encontrada: " + result + "\n");

                // Prepara o statement a ser executado para salvar o resultado no banco de dados
                String sqlInsertion = "insert into consultas (origem, destino, percurso)" + " values (?, ?, ?)";
                PreparedStatement preparedStmt = connection.prepareStatement(sqlInsertion);
                preparedStmt.setString(1, a1.getShortName());
                preparedStmt.setString(2, a2.getShortName());
                preparedStmt.setString(3, result);
                preparedStmt.execute();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

class Ciudad {
    private final int id;
    private final String nombre;

    public Ciudad(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre + " (C" + id + ")";
    }
}

class Tramo {
    private final int destinoId;
    private final int km;

    public Tramo(int destinoId, int km) {
        this.destinoId = destinoId;
        this.km = km;
    }

    public int getDestinoId() {
        return destinoId;
    }

    public int getKm() {
        return km;
    }
}

class ResultadoRuta {
    private final List<Ciudad> secuencia;
    private final List<Integer> tramosKm;
    private final int totalKm;

    public ResultadoRuta(List<Ciudad> secuencia, List<Integer> tramosKm, int totalKm) {
        this.secuencia = secuencia;
        this.tramosKm = tramosKm;
        this.totalKm = totalKm;
    }

    public List<Ciudad> getSecuencia() {
        return secuencia;
    }

    public List<Integer> getTramosKm() {
        return tramosKm;
    }

    public int getTotalKm() {
        return totalKm;
    }
}

class RedVial {
    private final Map<Integer, Ciudad> ciudades = new HashMap<>();
    private final Map<Integer, List<Tramo>> conexiones = new HashMap<>();

    private record EstadoBusqueda(int idCiudad, int distanciaAcum) {
    }

    public void registrarCiudad(int id, String nombre) {
        ciudades.put(id, new Ciudad(id, nombre));
        conexiones.putIfAbsent(id, new ArrayList<>());
    }

    public void registrarTramo(int origenId, int destinoId, int km) {
        conexiones.get(origenId).add(new Tramo(destinoId, km));
        conexiones.get(destinoId).add(new Tramo(origenId, km));
    }

    public Ciudad obtenerCiudad(int id) {
        return ciudades.get(id);
    }

    public ResultadoRuta calcularRutaMinima(int origenId, int destinoId) {
        Map<Integer, Integer> distMinima = new HashMap<>();
        Map<Integer, Integer> origenDe = new HashMap<>();
        PriorityQueue<EstadoBusqueda> pendientes =
                new PriorityQueue<>(Comparator.comparingInt(EstadoBusqueda::distanciaAcum));

        for (int id : ciudades.keySet()) {
            distMinima.put(id, Integer.MAX_VALUE);
        }
        distMinima.put(origenId, 0);
        pendientes.add(new EstadoBusqueda(origenId, 0));

        while (!pendientes.isEmpty()) {
            EstadoBusqueda actual = pendientes.poll();
            int u = actual.idCiudad();

            if (u == destinoId) {
                break;
            }
            if (actual.distanciaAcum() > distMinima.get(u)) {
                continue;
            }

            for (Tramo tramo : conexiones.get(u)) {
                int v = tramo.getDestinoId();
                int nuevaDist = distMinima.get(u) + tramo.getKm();
                if (nuevaDist < distMinima.get(v)) {
                    distMinima.put(v, nuevaDist);
                    origenDe.put(v, u);
                    pendientes.add(new EstadoBusqueda(v, nuevaDist));
                }
            }
        }

        if (distMinima.get(destinoId) == Integer.MAX_VALUE) {
            return new ResultadoRuta(Collections.emptyList(), Collections.emptyList(), -1);
        }

        List<Ciudad> secuencia = new ArrayList<>();
        Integer paso = destinoId;
        while (paso != null) {
            secuencia.add(ciudades.get(paso));
            paso = origenDe.get(paso);
        }
        Collections.reverse(secuencia);

        List<Integer> tramosKm = new ArrayList<>();
        for (int i = 0; i < secuencia.size() - 1; i++) {
            int actualId = secuencia.get(i).getId();
            int siguienteId = secuencia.get(i + 1).getId();
            for (Tramo t : conexiones.get(actualId)) {
                if (t.getDestinoId() == siguienteId) {
                    tramosKm.add(t.getKm());
                    break;
                }
            }
        }

        return new ResultadoRuta(secuencia, tramosKm, distMinima.get(destinoId));
    }
}

class NodoLista<T> {
    T valor;
    NodoLista<T> siguiente;

    NodoLista(T valor) {
        this.valor = valor;
    }
}

class Pila<T> {
    private NodoLista<T> tope;
    private int tamanio;

    public void apilar(T valor) {
        NodoLista<T> nodo = new NodoLista<>(valor);
        nodo.siguiente = tope;
        tope = nodo;
        tamanio++;
    }

    public T desapilar() {
        if (estaVacia()) {
            throw new IllegalStateException("La pila esta vacia");
        }
        T valor = tope.valor;
        tope = tope.siguiente;
        tamanio--;
        return valor;
    }

    public T verTope() {
        if (estaVacia()) {
            throw new IllegalStateException("La pila esta vacia");
        }
        return tope.valor;
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int obtenerTamanio() {
        return tamanio;
    }
}

class Cola<T> {
    private NodoLista<T> frente;
    private NodoLista<T> final_;
    private int tamanio;

    public void encolar(T valor) {
        NodoLista<T> nodo = new NodoLista<>(valor);
        if (estaVacia()) {
            frente = nodo;
        } else {
            final_.siguiente = nodo;
        }
        final_ = nodo;
        tamanio++;
    }

    public T desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola esta vacia");
        }
        T valor = frente.valor;
        frente = frente.siguiente;
        if (frente == null) {
            final_ = null;
        }
        tamanio--;
        return valor;
    }

    public T verFrente() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola esta vacia");
        }
        return frente.valor;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int obtenerTamanio() {
        return tamanio;
    }
}

class ValidadorIntegridad {

    public static boolean parentesisBalanceados(String texto) {
        Pila<Character> pila = new Pila<>();
        Map<Character, Character> pares = new HashMap<>();
        pares.put(')', '(');
        pares.put(']', '[');
        pares.put('}', '{');

        for (char c : texto.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                pila.apilar(c);
            } else if (c == ')' || c == ']' || c == '}') {
                if (pila.estaVacia() || pila.desapilar() != pares.get(c)) {
                    return false;
                }
            }
        }
        return pila.estaVacia();
    }
}

enum EstadoPedido {
    PENDIENTE,
    ATENDIDO
}

class Pedido {
    private final String codigo;
    private final String cliente;
    private EstadoPedido estado;

    public Pedido(String codigo, String cliente) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.estado = EstadoPedido.PENDIENTE;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCliente() {
        return cliente;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void marcarAtendido() {
        this.estado = EstadoPedido.ATENDIDO;
    }
}

class CentroAtencion {
    private final Cola<Pedido> colaPedidos = new Cola<>();
    private final List<Pedido> historialAtendidos = new ArrayList<>();

    public void recibirPedido(Pedido pedido) {
        colaPedidos.encolar(pedido);
    }

    public Pedido atenderSiguiente() {
        Pedido pedido = colaPedidos.desencolar();
        pedido.marcarAtendido();
        historialAtendidos.add(pedido);
        return pedido;
    }

    public boolean hayPedidosPendientes() {
        return !colaPedidos.estaVacia();
    }

    public List<Pedido> getHistorialAtendidos() {
        return historialAtendidos;
    }
}

public class SistemaLogistico {

    public static void main(String[] args) {
        RedVial red = new RedVial();
        red.registrarCiudad(0, "Quito");
        red.registrarCiudad(1, "Esmeraldas");
        red.registrarCiudad(2, "Riobamba");
        red.registrarCiudad(3, "Loja");
        red.registrarCiudad(4, "Machala");

        red.registrarTramo(0, 2, 190);
        red.registrarTramo(0, 1, 318);
        red.registrarTramo(2, 3, 260);
        red.registrarTramo(1, 3, 410);
        red.registrarTramo(2, 4, 300);
        red.registrarTramo(3, 4, 120);

        int origenId = 0;
        int destinoId = 4;
        ResultadoRuta ruta = red.calcularRutaMinima(origenId, destinoId);

        imprimirEncabezado();
        imprimirRuta(red.obtenerCiudad(origenId), red.obtenerCiudad(destinoId), ruta);
        simularCargaDescarga(ruta);
        simularCentroAtencion();
    }

    private static void imprimirEncabezado() {
        System.out.println("======================================================");
        System.out.println("ANDESCARGA EC - MODULO DE PILAS Y COLAS (FASE 2)");
        System.out.println("======================================================");
    }

    private static void imprimirRuta(Ciudad origen, Ciudad destino, ResultadoRuta ruta) {
        System.out.println("[Ruta base - Fase 1] " + origen.getNombre().toUpperCase()
                + " -> " + destino.getNombre().toUpperCase());

        StringBuilder grafica = new StringBuilder();
        List<Ciudad> secuencia = ruta.getSecuencia();
        List<Integer> tramos = ruta.getTramosKm();
        for (int i = 0; i < secuencia.size(); i++) {
            grafica.append(secuencia.get(i).getNombre().toUpperCase());
            if (i < tramos.size()) {
                grafica.append(" --(").append(tramos.get(i)).append("km)--> ");
            }
        }
        System.out.println("Secuencia optima: " + grafica);
        System.out.println("Distancia total: " + ruta.getTotalKm() + " km");
        System.out.println();
    }

    private static void simularCargaDescarga(ResultadoRuta ruta) {
        System.out.println("[Modulo PILA - carga y descarga LIFO]");
        Pila<String> pilaCarga = new Pila<>();
        List<Ciudad> paradas = ruta.getSecuencia();

        for (Ciudad ciudad : paradas) {
            String paquete = "Paquete-" + ciudad.getNombre();
            pilaCarga.apilar(paquete);
            System.out.println("push -> " + paquete + " (tamanio pila: " + pilaCarga.obtenerTamanio() + ")");
        }

        System.out.println("Orden de descarga (debe ser el inverso de la carga):");
        while (!pilaCarga.estaVacia()) {
            System.out.println("pop  -> " + pilaCarga.desapilar());
        }
        System.out.println();
    }

    private static void simularCentroAtencion() {
        System.out.println("[Modulo COLA - flujo de atencion FIFO + validacion de integridad]");
        CentroAtencion centro = new CentroAtencion();

        centro.recibirPedido(new Pedido("[PED-01(RUTA-A)]", "Cliente Torres"));
        centro.recibirPedido(new Pedido("{PED-02[RUTA-B]}", "Cliente Vera"));
        centro.recibirPedido(new Pedido("(PED-03(RUTA-C)", "Cliente Salas"));

        while (centro.hayPedidosPendientes()) {
            Pedido atendido = centro.atenderSiguiente();
            boolean valido = ValidadorIntegridad.parentesisBalanceados(atendido.getCodigo());
            System.out.println("enqueue/dequeue -> " + atendido.getCliente()
                    + " | codigo: " + atendido.getCodigo()
                    + " | estado: " + atendido.getEstado()
                    + " | integridad: " + (valido ? "OK" : "ERROR - simbolos sin cerrar"));
        }
    }
}

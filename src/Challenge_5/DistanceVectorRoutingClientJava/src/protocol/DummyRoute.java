package protocol;

/**
 * Simple object which describes a route entry in the forwarding table.
 * Can be extended to include additional data.
 */
public class DummyRoute {
    public int nextHop;
    public int cost;
}

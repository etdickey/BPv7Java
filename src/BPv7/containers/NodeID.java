package BPv7.containers;

import java.util.Objects;

/**
 * Java "records" are so cool
 *
 * @param id Contains a node ID, which could be in a variety of formats
 * @implSpec CBOR: todo
 * @implNote pretending for now that it's just an int
 * (in reality each endpoint ID is an URI -- Uniform Resource Identifier)
 * (technically, node IDs don't have to be URIs, but that is implementation determined)
 */
public record NodeID(String id) {
    //null source ID
    private static final String NULL_SOURCE_ID = "dtn:none";

    //default generated
    public static NodeID getNullSourceID() { return new NodeID(NULL_SOURCE_ID); }

    //default generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeID nodeID)) return false;//I am confused but intellij suggested using a "pattern variable"
        return id == nodeID.id;
    }

    //default generated
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

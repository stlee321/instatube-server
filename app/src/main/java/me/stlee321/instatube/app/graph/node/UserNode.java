package me.stlee321.instatube.app.graph.node;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("User")
@Getter
@Setter
public class UserNode {
    @Id @GeneratedValue
    private Long id;
    @Property("handle")
    private String handle;
    public UserNode(String handle) {
        this.handle = handle;
    }
}

package me.stlee321.instatube.app.graph.node;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Post")
@Getter
@Setter
public class PostNode {
    @Id @GeneratedValue
    private Long id;
    @Property("postId")
    private String postId;

    public PostNode(String postId) {
        this.postId = postId;
    }
}

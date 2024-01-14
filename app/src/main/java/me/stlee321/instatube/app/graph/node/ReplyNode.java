package me.stlee321.instatube.app.graph.node;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Reply")
@Getter
@Setter
public class ReplyNode {
    @Id @GeneratedValue
    private Long id;
    @Property("replyId")
    private String replyId;

    public ReplyNode(String replyId) {
        this.replyId = replyId;
    }
}

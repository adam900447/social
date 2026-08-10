package adam.brooks.social.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "groups")
public class Group {

    @Id
    private String id;

    private String name;
    private String description;

    private String ownerId;
    private List<String> memberIds = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}

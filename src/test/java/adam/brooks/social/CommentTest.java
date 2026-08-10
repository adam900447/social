package adam.brooks.social.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = new Comment();
    }

    @Test
    void id_defaultsToNull() {
        assertNull(comment.getId());
    }

    @Test
    void postId_defaultsToNull() {
        assertNull(comment.getPostId());
    }

    @Test
    void authorId_defaultsToNull() {
        assertNull(comment.getAuthorId());
    }

    @Test
    void authorName_defaultsToNull() {
        assertNull(comment.getAuthorName());
    }

    @Test
    void content_defaultsToNull() {
        assertNull(comment.getContent());
    }

    @Test
    void createdAt_defaultsToApproximatelyNow() {
        assertNotNull(comment.getCreatedAt());
        assertTrue(comment.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(comment.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5)));
    }

    @Test
    void settersAndGetters_workCorrectly() {
        LocalDateTime createdTime = LocalDateTime.of(2026, 1, 1, 9, 30);

        comment.setId("comment123");
        comment.setPostId("post456");
        comment.setAuthorId("user1");
        comment.setAuthorName("Jane Doe");
        comment.setContent("This looks great!");
        comment.setCreatedAt(createdTime);

        assertEquals("comment123", comment.getId());
        assertEquals("post456", comment.getPostId());
        assertEquals("user1", comment.getAuthorId());
        assertEquals("Jane Doe", comment.getAuthorName());
        assertEquals("This looks great!", comment.getContent());
        assertEquals(createdTime, comment.getCreatedAt());
    }

    @Test
    void content_canBeSetToEmptyString() {
        comment.setContent("");
        assertEquals("", comment.getContent());
    }

    @Test
    void content_canBeSetToNull() {
        comment.setContent(null);
        assertNull(comment.getContent());
    }

    @Test
    void equals_and_hashCode_matchForSameFieldValues() {
        LocalDateTime fixedTime = LocalDateTime.of(2026, 1, 1, 12, 0);

        Comment a = new Comment();
        a.setId("1");
        a.setPostId("post1");
        a.setAuthorId("author1");
        a.setAuthorName("Jane Doe");
        a.setContent("Nice post");
        a.setCreatedAt(fixedTime);

        Comment b = new Comment();
        b.setId("1");
        b.setPostId("post1");
        b.setAuthorId("author1");
        b.setAuthorName("Jane Doe");
        b.setContent("Nice post");
        b.setCreatedAt(fixedTime);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_returnsFalseWhenContentDiffers() {
        Comment a = new Comment();
        a.setId("1");
        a.setContent("First comment");

        Comment b = new Comment();
        b.setId("1");
        b.setContent("Different comment");

        assertNotEquals(a, b);
    }

    @Test
    void equals_returnsFalseWhenComparedToNullOrOtherType() {
        comment.setId("1");
        assertNotEquals(null, comment);
        assertNotEquals("some string", comment);
    }

    @Test
    void toString_containsFieldValues() {
        comment.setId("comment123");
        comment.setContent("Great work!");

        String result = comment.toString();

        assertTrue(result.contains("comment123"));
        assertTrue(result.contains("Great work!"));
    }
}
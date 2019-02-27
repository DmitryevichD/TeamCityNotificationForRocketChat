package rocketnotifications;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author D.Dmitrievich
 */
@Data
@Builder
public class Message {
    private String channel;
    private String avatar;
    private String alias;
    private String text;
    private String emoji;
    private List<Attachment> attachments;
}

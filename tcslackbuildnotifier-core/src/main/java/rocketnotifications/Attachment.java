package rocketnotifications;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Attachment for rocket notification
 */
@Data
public class Attachment {
    private String fallback;
    private String text;
    private String pretext;
    private String color;
    private String title;
    private String titleLink;
    private List<Field> fields = new ArrayList<>();

    public Attachment(String fallback, String text, String pretext, String color) {
        this.fallback = fallback;
        this.text = text;
        this.pretext = pretext;
        this.color = color;
        this.fields = new ArrayList<Field>();
    }

    void addField(String title, String value, boolean isShort) {
        this.fields.add(new Field(title, value, isShort));
    }
}

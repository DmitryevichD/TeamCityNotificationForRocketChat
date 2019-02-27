package rocketnotifications;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class Field {
    private String title;
    private String value;
    private Boolean isShort;
}

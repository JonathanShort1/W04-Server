/**
 * This Enum is for the http response status codes implemented.
 */

public enum HTTP_STATUS {
    SUCCESS(200),
    FILE_NOT_FOUND(404),
    BAD_REQUEST(400);

    private int status;

    HTTP_STATUS(int status) {
        this.status = status;
    }
}

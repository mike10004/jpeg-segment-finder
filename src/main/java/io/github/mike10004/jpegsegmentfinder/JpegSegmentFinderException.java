package io.github.mike10004.jpegsegmentfinder;

/**
 * Exception class thrown if something goes awry when examining a JPEG input stream.
 */
@SuppressWarnings("unused")
public class JpegSegmentFinderException extends RuntimeException {
    public JpegSegmentFinderException() {
    }

    public JpegSegmentFinderException(String message) {
        super(message);
    }

    public JpegSegmentFinderException(String message, Throwable cause) {
        super(message, cause);
    }

    public JpegSegmentFinderException(Throwable cause) {
        super(cause);
    }
}

package io.github.mike10004.jpegsegmentfinder;

import com.drew.imaging.jpeg.JpegSegmentType;

import java.util.Objects;

/**
 * Class that represents the specification of a file segment.
 */
public class JpegSegmentSpec {

    /**
     * Segment type.
     */
    public final JpegSegmentType type;

    /**
     * Offset from the start of a file where the segment header begins.
     */
    public final long headerOffset;

    /**
     * Offset from the start of a file where the segment content begins.
     */
    public final long contentOffset;

    /**
     * Length of the segment content.
     */
    public final long contentLength;

    /**
     * Constructs a new instance.
     * @param type segment type
     * @param headerOffset offset from the start of a file where the segment header begins
     * @param contentOffset offset from the start of a file where the segment content begins
     * @param contentLength segment content length
     */
    public JpegSegmentSpec(JpegSegmentType type, long headerOffset, long contentOffset, long contentLength) {
        this.type = Objects.requireNonNull(type);
        this.contentOffset = contentOffset;
        this.headerOffset = headerOffset;
        this.contentLength = contentLength;
    }

    /**
     * Computes the full length of the statement, from the start of the header to the end of the
     * segment content.
     * @return the full length of the segment
     */
    public long fullLength() {
        // we should check this for overflow, though it's exceedingly unlikely
        return (contentOffset - headerOffset) + contentLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JpegSegmentSpec that = (JpegSegmentSpec) o;

        if (contentOffset != that.contentOffset) return false;
        if (headerOffset != that.headerOffset) return false;
        if (contentLength != that.contentLength) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (contentOffset ^ (contentOffset >>> 32));
        result = 31 * result + (int) (headerOffset ^ (headerOffset >>> 32));
        result = 31 * result + (int) (contentLength ^ (contentLength >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "JpegSegmentSpec{" +
                "" + type +
                ", contentOffset=" + contentOffset +
                ", headerOffset=" + headerOffset +
                ", contentLength=" + contentLength +
                ", fullLength=" + fullLength() +
                '}';
    }
}

package io.github.mike10004.jpegsegmentfinder;

/**
 * Class that represents the specification of a file segment.
 */
public class JpegSegmentSpec {

    /**
     * Segment byte marker. These are described in various specification documents.
     * See http://dev.exiv2.org/projects/exiv2/wiki/The_Metadata_in_JPEG_files for
     * some good reading.
     */
    public final byte marker;

    /**
     * Offset (from the start of a file) where the segment header begins.
     */
    public final long headerOffset;

    /**
     * Offset (from the start of a file) where the segment content begins.
     */
    public final long contentOffset;

    /**
     * Length of the segment content.
     */
    public final long contentLength;

    /**
     * Constructs a new instance.
     * @param marker the metadata type marker (see {@code com.drew.imaging.jpeg.JpegSegmentType}
     * @param headerOffset offset from the start of a file where the segment header begins
     * @param contentOffset offset from the start of a file where the segment content begins
     * @param contentLength segment content length
     */
    public JpegSegmentSpec(byte marker, long headerOffset, long contentOffset, long contentLength) {
        this.marker = marker;
        this.contentOffset = contentOffset;
        this.headerOffset = headerOffset;
        this.contentLength = contentLength;
    }

    /**
     * Computes the full length of the statement, from the start of the header
     * to the end of the segment content.
     * @return the full length of the segment
     */
    public long fullLength() {
        // we should check this for overflow, though it's exceedingly unlikely
        return (contentOffset - headerOffset) + contentLength;
    }


    @Override
    public String toString() {
        return "JpegSegmentSpec{" +
                "" + String.format("0x%02X", marker) +
                "; contentOffset=" + contentOffset +
                ", headerOffset=" + headerOffset +
                ", contentLength=" + contentLength +
                ", fullLength=" + fullLength() +
                '}';
    }

    /**
     * Computes the header length. The header length is the length of the
     * interval of bytes between the header start and the content start position.
     * @return the header length
     */
    public long headerLength() {
        return contentOffset - headerOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JpegSegmentSpec that = (JpegSegmentSpec) o;

        if (marker != that.marker) return false;
        if (headerOffset != that.headerOffset) return false;
        if (contentOffset != that.contentOffset) return false;
        return contentLength == that.contentLength;
    }

    @Override
    public int hashCode() {
        int result = (int) marker;
        result = 31 * result + (int) (headerOffset ^ (headerOffset >>> 32));
        result = 31 * result + (int) (contentOffset ^ (contentOffset >>> 32));
        result = 31 * result + (int) (contentLength ^ (contentLength >>> 32));
        return result;
    }
}

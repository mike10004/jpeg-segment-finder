package io.github.mike10004.jpegiptcreader;



import com.drew.imaging.jpeg.JpegSegmentType;

import java.util.Objects;

public class JpegSegmentSpec {

    public final JpegSegmentType type;
    public final long contentOffset;
    public final long headerOffset;
    public final long contentLength;

    public JpegSegmentSpec(JpegSegmentType type, long headerOffset, long contentOffset, long contentLength) {
        this.type = Objects.requireNonNull(type);
        this.contentOffset = contentOffset;
        this.headerOffset = headerOffset;
        this.contentLength = contentLength;
    }

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

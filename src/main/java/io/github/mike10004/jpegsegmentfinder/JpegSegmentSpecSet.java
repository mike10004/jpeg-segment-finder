package io.github.mike10004.jpegsegmentfinder;



import com.drew.imaging.jpeg.JpegSegmentType;

import java.util.ArrayList;
import java.util.List;

public interface JpegSegmentSpecSet {

    void addSegment(JpegSegmentType type, long startPosition, long contentStartPosition, long contentLength);

    static JpegSegmentSpecSet create() {
        return new JpegSegmentSpecSet() {

            private final List<JpegSegmentSpec> segments = new ArrayList<>();

            @Override
            public void addSegment(JpegSegmentType type, long startPosition, long contentStartPosition, long contentLength) {
                segments.add(new JpegSegmentSpec(type, startPosition, contentStartPosition, contentLength));
            }

            @Override
            public List<JpegSegmentSpec> getSegments() {
                return new ArrayList<>(segments);
            }
        };
    }

    List<JpegSegmentSpec> getSegments();

}
